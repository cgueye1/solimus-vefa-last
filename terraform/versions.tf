terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # State Terraform stocké dans S3, verrouillage natif (sans DynamoDB)
  backend "s3" {
    bucket       = "solimus-vefa-tfstate-374784204755"
    key          = "prod/terraform.tfstate"
    region       = "eu-west-3"
    use_lockfile = true
    encrypt      = true
  }
}