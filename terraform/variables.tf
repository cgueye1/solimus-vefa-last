# ====== GENERAL VARIABLES ======

variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "eu-west-3"
}

variable "project" {
  description = "Project name (prefix for all resources)"
  type        = string
  default     = "solimus-vefa"
}

variable "environment" {
  description = "Environment name (dev/prod)"
  type        = string
  default     = "prod"

  validation {
    condition     = contains(["dev", "prod"], var.environment)
    error_message = "Environment must be 'dev' or 'prod'"
  }
}

variable "account_id" {
  description = "AWS Account ID"
  type        = string
  sensitive   = true
}

# ====== DOMAIN & NETWORKING ======

variable "domain" {
  description = "Main domain name for the application (e.g., vefa.solimus.sn)"
  type        = string
  default     = "vefa.solimus.sn"
}

variable "api_domain" {
  description = "API domain name (e.g., api.vefa.solimus.sn)"
  type        = string
  default     = "api.vefa.solimus.sn"
}

variable "frontend_domain" {
  description = "Frontend domain name (e.g., vefa.solimus.sn)"
  type        = string
  default     = "vefa.solimus.sn"
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for public subnet"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR block for private subnet"
  type        = string
  default     = "10.0.2.0/24"
}

variable "private_subnet_2_cidr" {
  description = "CIDR block for second private subnet (different AZ, required for RDS)"
  type        = string
  default     = "10.0.3.0/24"
}

# ====== DATABASE ======

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "vefa"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "admin"
}

variable "db_engine" {
  description = "Database engine"
  type        = string
  default     = "mysql"
}

variable "db_engine_version" {
  description = "Database engine version"
  type        = string
  default     = "8.4"
}

variable "db_instance_class" {
  description = "Database instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_storage" {
  description = "Database storage in GB"
  type        = number
  default     = 20
}

variable "db_backup_retention" {
  description = "Database backup retention days"
  type        = number
  default     = 7
}

# ====== EC2 / BACKEND ======

variable "backend_instance_type" {
  description = "EC2 instance type for backend"
  type        = string
  default     = "t3.small"
}

variable "backend_port" {
  description = "Backend application port"
  type        = number
  default     = 8086
}

variable "container_registry" {
  description = "Docker container registry (ECR repository URL)"
  type        = string
  default     = ""
}

variable "backend_image" {
  description = "Docker image for backend"
  type        = string
  default     = "solimus-vefa:latest"
}

# ====== S3 / STORAGE ======

variable "files_bucket_name" {
  description = "S3 bucket for file uploads"
  type        = string
  default     = "solimus-vefa-files"
}

variable "frontend_bucket_name" {
  description = "S3 bucket for frontend static files"
  type        = string
  default     = "solimus-vefa-frontend"
}

# ====== CLOUDFRONT ======

variable "cloudfront_ttl" {
  description = "CloudFront default TTL (seconds)"
  type        = number
  default     = 3600
}

variable "cloudfront_min_ttl" {
  description = "CloudFront min TTL (seconds)"
  type        = number
  default     = 0
}

variable "cloudfront_max_ttl" {
  description = "CloudFront max TTL (seconds)"
  type        = number
  default     = 86400
}

# ====== GITHUB ======

variable "github_back_repo" {
  description = "GitHub backend repository (owner/repo)"
  type        = string
  default     = "cgueye1/solimus-vefa-last"
}

variable "github_front_repo" {
  description = "GitHub frontend repository (owner/repo)"
  type        = string
  default     = "cgueye1/solimus-vefa-front"
}

variable "github_oidc_provider_arn" {
  description = "GitHub OIDC provider ARN (auto-created or existing)"
  type        = string
  default     = ""
}

# ====== MONITORING ======

variable "enable_monitoring" {
  description = "Enable CloudWatch monitoring and alarms"
  type        = bool
  default     = true
}

variable "alarm_email" {
  description = "Email for SNS alarm notifications"
  type        = string
  default     = ""
}

# ====== OPTIONAL: TAGS ======

variable "tags" {
  description = "Additional tags for all resources"
  type        = map(string)
  default     = {}
}
