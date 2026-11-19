# ====== GITHUB OIDC PROVIDER (for CI/CD) ======
# This allows GitHub Actions to assume IAM roles without storing AWS credentials

resource "aws_iam_openid_connect_provider" "github" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
  url             = "https://token.actions.githubusercontent.com"

  tags = {
    Name = "${var.project}-github-oidc"
  }
}

# ====== IDENTITÉ GITHUB ACCEPTÉE (claim "sub" du jeton OIDC) ======
# GitHub envoie un "sub" avec les identifiants numériques du propriétaire et du dépôt :
#   repo:cgueye1@200596202/solimus-vefa-front@1411667368:ref:refs/heads/prod-aws
# Ce format protège contre l'usurpation (dépôt supprimé puis recréé sous le même nom).
# L'ancien format (sans identifiants) reste accepté par compatibilité.
# Seule la branche prod-aws peut déployer.

locals {
  github_owner_id      = "200596202"
  github_front_repo_id = "1411667368"
  github_back_repo_id  = "1411631636"

  github_back_sub = [
    "repo:${var.github_back_repo}:ref:refs/heads/prod-aws",
    "repo:cgueye1@${local.github_owner_id}/solimus-vefa-last@${local.github_back_repo_id}:ref:refs/heads/prod-aws",
  ]

  github_front_sub = [
    "repo:${var.github_front_repo}:ref:refs/heads/prod-aws",
    "repo:cgueye1@${local.github_owner_id}/solimus-vefa-front@${local.github_front_repo_id}:ref:refs/heads/prod-aws",
  ]
}

# ====== IAM ROLE FOR GITHUB ACTIONS (BACKEND) ======

resource "aws_iam_role" "github_backend_role" {
  name = "${var.project}-github-backend-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = local.github_back_sub
          }
        }
      }
    ]
  })

  tags = {
    Name = "${var.project}-github-backend-role"
  }
}

# ====== IAM ROLE FOR GITHUB ACTIONS (FRONTEND) ======

resource "aws_iam_role" "github_frontend_role" {
  name = "${var.project}-github-frontend-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github.arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = local.github_front_sub
          }
        }
      }
    ]
  })

  tags = {
    Name = "${var.project}-github-frontend-role"
  }
}

# ====== POLICY FOR GITHUB BACKEND (Deploy to EC2/ECS) ======

resource "aws_iam_role_policy" "github_backend_policy" {
  name = "${var.project}-github-backend-policy"
  role = aws_iam_role.github_backend_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::${var.files_bucket_name}-${var.account_id}",
          "arn:aws:s3:::${var.files_bucket_name}-${var.account_id}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "ssm:SendCommand",
          "ssm:GetCommandInvocation"
        ]
        Resource = [
          "arn:aws:ec2:${var.aws_region}:${var.account_id}:instance/${aws_instance.backend.id}",
          "arn:aws:ssm:${var.aws_region}:${var.account_id}:document/AWS-RunShellScript"
        ]
      }
    ]
  })
}

# ====== POLICY FOR GITHUB FRONTEND (Deploy to S3 + CloudFront) ======

resource "aws_iam_role_policy" "github_frontend_policy" {
  name = "${var.project}-github-frontend-policy"
  role = aws_iam_role.github_frontend_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::${var.frontend_bucket_name}-${var.account_id}",
          "arn:aws:s3:::${var.frontend_bucket_name}-${var.account_id}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "cloudfront:CreateInvalidation",
          "cloudfront:GetDistribution"
        ]
        Resource = "*"
      }
    ]
  })
}

# ====== OUTPUTS FOR GITHUB VARIABLES ======

output "github_oidc_provider_arn" {
  description = "GitHub OIDC Provider ARN"
  value       = aws_iam_openid_connect_provider.github.arn
}

output "github_backend_role_arn" {
  description = "GitHub Backend IAM Role ARN"
  value       = aws_iam_role.github_backend_role.arn
}

output "github_backend_role_name" {
  description = "GitHub Backend IAM Role Name"
  value       = aws_iam_role.github_backend_role.name
}

output "github_frontend_role_arn" {
  description = "GitHub Frontend IAM Role ARN"
  value       = aws_iam_role.github_frontend_role.arn
}

output "github_frontend_role_name" {
  description = "GitHub Frontend IAM Role Name"
  value       = aws_iam_role.github_frontend_role.name
}

output "github_backend_setup_instructions" {
  description = "Variables GitHub (onglet Variables) du dépôt back"
  value = format(
    "Variables GitHub du dépôt %s :\n\nAWS_ROLE_ARN=%s\nAWS_REGION=%s\nEC2_INSTANCE_ID=%s",
    var.github_back_repo,
    aws_iam_role.github_backend_role.arn,
    var.aws_region,
    aws_instance.backend.id
  )
}

output "github_frontend_setup_instructions" {
  description = "Variables GitHub (onglet Variables) du dépôt front"
  value = format(
    "Variables GitHub du dépôt %s :\n\nAWS_ROLE_ARN=%s\nAWS_REGION=%s\nFRONT_BUCKET=%s\nCLOUDFRONT_ID=%s",
    var.github_front_repo,
    aws_iam_role.github_frontend_role.arn,
    var.aws_region,
    aws_s3_bucket.frontend.id,
    aws_cloudfront_distribution.frontend.id
  )
}