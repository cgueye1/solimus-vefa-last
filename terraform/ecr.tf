# ====== ECR : REGISTRE D'IMAGES DU BACK-END ======

resource "aws_ecr_repository" "backend" {
  name                 = "${var.project}-backend"
  image_tag_mutability = "MUTABLE" # le tag "latest" est réécrit à chaque déploiement

  image_scanning_configuration {
    scan_on_push = true # analyse de vulnérabilités gratuite
  }

  tags = {
    Name = "${var.project}-backend-ecr"
  }
}

# Ne garder que les 5 dernières images (coût de stockage minimal)
resource "aws_ecr_lifecycle_policy" "backend" {
  repository = aws_ecr_repository.backend.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Garder les 5 dernieres images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 5
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# L'EC2 peut télécharger les images
resource "aws_iam_role_policy_attachment" "backend_ecr_read" {
  role       = aws_iam_role.backend_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# GitHub Actions (back) : pousser l'image et déclencher le déploiement sur l'EC2
resource "aws_iam_role_policy" "github_backend_deploy" {
  name = "${var.project}-github-backend-deploy"
  role = aws_iam_role.github_backend_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "EcrLogin"
        Effect   = "Allow"
        Action   = ["ecr:GetAuthorizationToken"]
        Resource = "*"
      },
      {
        Sid    = "EcrPush"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:BatchGetImage",
          "ecr:GetDownloadUrlForLayer",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:PutImage"
        ]
        Resource = aws_ecr_repository.backend.arn
      },
      {
        Sid    = "SsmDeployOnBackendOnly"
        Effect = "Allow"
        Action = ["ssm:SendCommand"]
        Resource = [
          aws_instance.backend.arn,
          "arn:aws:ssm:${var.aws_region}::document/AWS-RunShellScript"
        ]
      },
      {
        Sid      = "SsmReadResult"
        Effect   = "Allow"
        Action   = ["ssm:GetCommandInvocation", "ssm:ListCommandInvocations"]
        Resource = "*"
      }
    ]
  })
}

output "ecr_repository_url" {
  description = "URL du dépôt ECR du back-end (variable GitHub ECR_REPOSITORY_URL)"
  value       = aws_ecr_repository.backend.repository_url
}