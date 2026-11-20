#!/bin/bash
# Déploiement de PRODUCTION du back-end sur l'EC2.
#
# Lancé par GitHub Actions via SSM (en root), qui dépose d'abord dans /opt/solimus-vefa :
#   - docker-compose.yml  (copie de deploy/docker-compose.prod.yml)
#   - deploy-prod.sh      (ce script)
#
# Le serveur est entièrement reconstruit à partir de :
#   - Git (ce script, le docker-compose)  - ECR (l'image)  - SSM Parameter Store (le .env)
# Aucun fichier important ne vit uniquement sur le disque de l'EC2.
#
# Usage manuel sur l'EC2 : sudo bash /opt/solimus-vefa/deploy-prod.sh <URL_ECR> <TAG>
set -euo pipefail

ECR_URL="${1:?Usage: deploy-prod.sh <URL_ECR> <TAG>}"
TAG="${2:-latest}"
REGION="eu-west-3"
APP_DIR="/opt/solimus-vefa"
ENV_PARAM="/solimus-vefa/backend/env"      # .env de prod, chiffré dans SSM Parameter Store
API_DOMAIN="api.vefa.solimus.sn"
LOCAL_IMAGE="solimus-vefa:latest"          # nom utilisé dans docker-compose.yml
HEALTH_URL="http://127.0.0.1:8086/actuator/health"
REGISTRY="${ECR_URL%%/*}"

export DEBIAN_FRONTEND=noninteractive
mkdir -p "$APP_DIR"
cd "$APP_DIR"

# ---------------------------------------------------------------------------
# 1. Prérequis du serveur (installés automatiquement la première fois)
# ---------------------------------------------------------------------------
if ! docker compose version > /dev/null 2>&1; then
  echo "==> Installation de docker compose v2"
  apt-get update -qq
  apt-get install -y -qq docker-compose-v2
fi

if ! command -v caddy > /dev/null 2>&1; then
  echo "==> Installation de Caddy"
  apt-get install -y -qq debian-keyring debian-archive-keyring apt-transport-https curl gnupg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' \
    | gpg --dearmor --yes -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' \
    > /etc/apt/sources.list.d/caddy-stable.list
  apt-get update -qq
  apt-get install -y -qq caddy
fi

# Configuration de Caddy (HTTPS automatique pour l'API), rechargée seulement si elle change
printf '%s {\n    encode gzip\n    reverse_proxy 127.0.0.1:8086\n}\n' "$API_DOMAIN" > /tmp/Caddyfile.new
if ! cmp -s /tmp/Caddyfile.new /etc/caddy/Caddyfile; then
  echo "==> Configuration de Caddy"
  mv /tmp/Caddyfile.new /etc/caddy/Caddyfile
  caddy validate --config /etc/caddy/Caddyfile
  systemctl enable caddy > /dev/null 2>&1 || true
  systemctl restart caddy
else
  rm -f /tmp/Caddyfile.new
fi

# ---------------------------------------------------------------------------
# 2. Fichiers de l'application
# ---------------------------------------------------------------------------
if [ ! -f docker-compose.yml ]; then
  echo "ÉCHEC : $APP_DIR/docker-compose.yml absent (il est déposé par GitHub Actions)"
  exit 1
fi

echo "==> Récupération du .env depuis SSM Parameter Store ($ENV_PARAM)"
umask 077
aws ssm get-parameter --region "$REGION" --name "$ENV_PARAM" --with-decryption \
  --query "Parameter.Value" --output text > .env.new
if [ ! -s .env.new ]; then
  echo "ÉCHEC : le paramètre $ENV_PARAM est vide ou introuvable"
  rm -f .env.new
  exit 1
fi
mv .env.new .env
chmod 600 .env

# ---------------------------------------------------------------------------
# 3. Image et conteneur
# ---------------------------------------------------------------------------
echo "==> Connexion à ECR"
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRY"

echo "==> Téléchargement de l'image ${ECR_URL}:${TAG}"
docker pull "${ECR_URL}:${TAG}"
docker tag "${ECR_URL}:${TAG}" "$LOCAL_IMAGE"
docker logout "$REGISTRY" > /dev/null 2>&1 || true

echo "==> Démarrage du conteneur"
docker compose up -d --remove-orphans

# ---------------------------------------------------------------------------
# 4. Vérification
# ---------------------------------------------------------------------------
echo "==> Attente de l'application (3 minutes max)"
for i in $(seq 1 36); do
  if curl -sf "$HEALTH_URL" > /dev/null; then
    echo "Application UP (version ${TAG})"
    docker image prune -f > /dev/null
    exit 0
  fi
  sleep 5
done

echo "ÉCHEC : l'application ne répond pas après 180 secondes"
docker compose logs --tail=80 backend || true
exit 1
