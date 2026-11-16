# 01 - Compte et budget AWS

## Objectif
Créer ou vérifier votre compte AWS et mettre en place des limites de dépense.

## Étapes

### 1. Créer/Vérifier le compte AWS

- [ ] Accéder à https://console.aws.amazon.com
- [ ] Compte existant: **374784204755** (cheikh-admin)
- [ ] Si nouveau compte:
  - Création complète: https://aws.amazon.com
  - Accepter les conditions d'utilisation
  - Ajouter une carte bancaire
  - Vérifier l'identité (email + téléphone)

### 2. Activer MFA (Multi-Factor Authentication)

- [ ] Console AWS → Account → Security Credentials
- [ ] "Multi-Factor Authentication"
- [ ] Add MFA device → Virtual MFA device (Authenticator)
- [ ] Scanner le QR code
- [ ] Entrer deux codes successifs

### 3. IAM Identity Center (pour SSO)

- [ ] AWS Console → AWS IAM Identity Center
- [ ] Enable
- [ ] Create organization (if needed)
- [ ] Users → Add user
  - Email: cheikh@...*
  - Username: cheikh-admin
  - Confirm
- [ ] Groups → Create group "Developers"
- [ ] Assign user to group
- [ ] Permission sets: Attach "AdministratorAccess"
- [ ] AWS accounts → Configure permission sets
- [ ] Get login URL: https://...awsapps.com/start

### 4. Budget et alertes

- [ ] AWS Billing → Budgets → Create budget
  - Type: Cost
  - Budget amount: 100 USD/month
  - Actions: Alert at 80% ($80)
  - Email: cheikh@solimus.sn

- [ ] Billing → Billing Alerts
  - Enable: "Receive CloudWatch Alarms"
  - Threshold: $50/month

### 5. (Optionnel) Limite de crédit libre

Nouveau compte? Généralement 300$ de crédit pendant 12 mois:

- [ ] Billing → Promotion: Vérifier montant disponible
- [ ] AWS Free Tier: https://aws.amazon.com/fr/free/
  - EC2 (t3): 750 heures/mois ✓
  - RDS: 750 heures/mois ✓
  - S3: 5 GB ✓

## ✅ Validation

- [ ] Compte AWS accessible
- [ ] MFA activé pour utilisateur root
- [ ] IAM Identity Center fonctionnel
- [ ] Budget/alertes configurés
- [ ] Console: https://console.aws.amazon.com affiche "Logged in"

## Prochaine étape
→ [02-outils-et-connexion.md](02-outils-et-connexion.md)
