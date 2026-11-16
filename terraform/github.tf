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
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_back_repo}:ref:refs/heads/prod-aws"
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
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_front_repo}:ref:refs/heads/prod-aws"
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

# ====== OUTPUTS FOR GITHUB SECRETS ======

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
  description = "Instructions for setting up GitHub backend"
  value = format(
    "Add these secrets to %s GitHub repository:\n\nROLE_ARN=%s\nAWS_REGION=%s",
    var.github_back_repo,
    aws_iam_role.github_backend_role.arn,
    var.aws_region
  )
}

output "github_frontend_setup_instructions" {
  description = "Instructions for setting up GitHub frontend"
  value = format(
    "Add these secrets to %s GitHub repository:\n\nROLE_ARN=%s\nS3_BUCKET=%s\nCLOUDFRONT_DISTRIBUTION_ID=%s\nAWS_REGION=%s",
    var.github_front_repo,
    aws_iam_role.github_frontend_role.arn,
    aws_s3_bucket.frontend.id,
    aws_cloudfront_distribution.frontend.id,
    var.aws_region
  )
}
