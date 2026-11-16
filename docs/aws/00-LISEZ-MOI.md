# Guide de déploiement AWS - Solimus Vefa

**Statut:** Production Ready  
**Région:** eu-west-3 (Paris)  
**Coût estimé:** ~$40-50/mois  
**Date de mise à jour:** $(date)

## 📋 Vue d'ensemble

Ce guide décrit comment déployer l'application Solimus Vefa sur AWS de manière automatisée et peu coûteuse, en suivant l'architecture du guide "Terraform AWS à bas coût".

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Route 53 (DNS)                       │
│           vefa.solimus.sn (subdomain only)             │
└────────┬────────────────────────────────┬───────────────┘
         │                                │
    ┌────▼────────────┐            ┌─────▼────────┐
    │  EC2 t3.small   │            │  S3 Frontend  │
    │  + Docker       │            │  + CloudFront │
    │  + Caddy        │            └───────────────┘
    │  + Backend App  │
    └────┬────────────┘
         │
    ┌────▼─────────┐
    │ RDS MySQL 8.4│
    │   t4g.micro  │
    └──────────────┘

    ┌──────────────────┐
    │ S3 Files Bucket  │
    │  + CloudFront    │
    └──────────────────┘
```

### Branches et environnements

| Branche | Environnement | Déploiement | Où |
|---------|---------------|-------------|-----|
| `develop` | DEV | Manuel (Dokploy) | Dokploy (vos serveurs) |
| `main` | PROD | Automatique (CI/CD) | AWS |

## 🚀 Ordre des étapes (À FAIRE)

**Prérequis:** AWS CLI, Terraform, et accès AWS SSO configuré

### Phase 1: Préparation AWS (30 min)
- [ ] [01-compte-aws.md](01-compte-aws.md) - Compte et budget AWS

### Phase 2: Outils et connexion (15 min)
- [ ] [02-outils-et-connexion.md](02-outils-et-connexion.md) - CLI + Terraform

### Phase 3: État Terraform (10 min)
- [ ] [03-state-terraform.md](03-state-terraform.md) - S3 backend

### Phase 4: DNS (OVH) (15 min)
- [ ] [04-dns-ovh-sous-domaine.md](04-dns-ovh-sous-domaine.md) - Délégation

### Phase 5: Déploiement initial (20 min)
- [ ] [05-premier-deploiement.md](05-premier-deploiement.md) - Terraform apply

### Phase 6: GitHub Actions (15 min)
- [ ] [06-github-actions.md](06-github-actions.md) - CI/CD setup

### Phase 7: Migration MinIO→S3 (optionnel, 30 min)
- [ ] [07-migration-minio-vers-s3.md](07-migration-minio-vers-s3.md) - Si fichiers existants

### Phase 8: Exploitation (15 min)
- [ ] [08-exploitation-et-couts.md](08-exploitation-et-couts.md) - Monitoring

### Phase 9: Dépannage (au besoin)
- [ ] [09-depannage.md](09-depannage.md) - Erreurs fréquentes

## 💰 Coûts estimés

| Service | Type | Coût mensuel |
|---------|------|-------------|
| EC2 t3.small (750h) | Compute | ~$7 |
| RDS MySQL t4g.micro | Database (20 GB) | ~$18 |
| S3 (10 GB fichiers + 0.5 GB frontend) | Storage | ~$0.25 |
| Route 53 (zone + requêtes) | DNS | ~$0.50 |
| CloudFront (1 TB transfer) | CDN | ~$15 |
| **Total** | | **~$41/mois** |

⚠️ **Note:** Premiers 12 mois = **gratuit** (AWS Free Tier covers EC2 + RDS)

## 📝 Informations requises

Avant de commencer, rassemblez ces informations :

| Item | Valeur | Source |
|------|--------|--------|
| ID Compte AWS | 374784204755 | AWS Console → Account |
| Région | eu-west-3 | ✓ Configurée |
| Domaine principal | solimus.sn | ✓ OVH |
| Sous-domaine | vefa.solimus.sn | ✓ Configuré |
| Domaine API | api.vefa.solimus.sn | ✓ Configuré |
| Email (alertes) | cheikh@... | À renseigner |
| DB password | _____ | Générer avec `openssl rand -base64 32` |
| Front Git repo | cgueye1/solimus-vefa-front | ✓ Configuré |
| Back Git repo | cgueye1/solimus-vefa-last | ✓ Configuré |

## ⚡ Commandes rapides

Une fois configuré, les commandes principales sont :

```bash
# Depuis le dossier terraform/
cd terraform/

# Voir ce qui va changer
terraform plan

# Déployer les ressources
terraform apply

# Voir les outputs (clés importantes)
terraform output

# Détruire tout (ATTENTION!)
terraform destroy
```

## ✅ Validation finale

Après chaque étape, vérifiez :

- [ ] Terraform valide: `terraform validate` ✓
- [ ] Pas d'erreurs de syntaxe: `terraform fmt -check` ✓
- [ ] Secrets non commités: `.gitignore` contient `terraform.tfvars` ✓
- [ ] AWS credentials en SSO: `aws sts get-caller-identity` ✓
- [ ] Application healthy: `curl https://api.vefa.solimus.sn/actuator/health` ✓

## ❓ Besoin d'aide?

- Erreur Terraform? → Voir [09-depannage.md](09-depannage.md)
- Problème AWS? → Voir logs CloudWatch
- DNS ne fonctionne pas? → Vérifier OVH NS records

## 🔐 Sécurité

⚠️ **Points critiques:**

1. **Jamais commiter secrets:**
   - `terraform.tfvars` (DB password) ✓ .gitignore
   - `.env` (Dokploy) ✓ .gitignore
   - Clés AWS → IAM roles + OIDC (GitHub Actions)

2. **Instance EC2:**
   - SSH ouvert à tous (⚠️ à restreindre en production!)
   - Accès par Session Manager (SSM) préféré
   - Pas de clés AWS: IAM role only

3. **Base de données:**
   - Accessible uniquement depuis EC2 (security group)
   - Backup automatique 7 jours
   - Pas de multi-AZ (coût optimisé)

4. **S3 + Files:**
   - Public access bloqué ✓
   - CORS configuré pour `vefa.solimus.sn` ✓
   - Signed URLs pour téléchargement ✓

## 📞 Support et questions

Pour questions spécifiques au déploiement AWS:
- Consulter l'étape correspondante dans `docs/aws/`
- Vérifier `09-depannage.md` pour problèmes courants
- Logs application: CloudWatch `/aws/ec2/solimus-vefa`

---

**Prêt?** Commencez par [01-compte-aws.md](01-compte-aws.md) →
