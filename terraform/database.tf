# ====== RANDOM PASSWORD GENERATION ======

resource "random_password" "db_password" {
  length           = 24
  special          = true
  override_special = "!#%^*-_=+"
}

# ====== SSM PARAMETER STORE FOR DB PASSWORD ======

resource "aws_ssm_parameter" "db_password" {
  name        = "/${var.project}/db/password"
  description = "Database password for ${var.project}"
  type        = "SecureString"
  value       = random_password.db_password.result
  overwrite   = true

  tags = {
    Name = "${var.project}-db-password"
  }
}

# ====== DB SUBNET GROUP (Required for RDS in VPC) ======

resource "aws_db_subnet_group" "main" {
  name       = "${var.project}-db-subnet-group"
  subnet_ids = [aws_subnet.public.id, aws_subnet.private.id]

  tags = {
    Name = "${var.project}-db-subnet-group"
  }
}

# ====== RDS MYSQL INSTANCE ======

resource "aws_db_instance" "mysql" {
  identifier           = "${var.project}-db"
  allocated_storage    = var.db_storage
  storage_type         = "gp2"
  engine               = var.db_engine
  engine_version       = var.db_engine_version
  instance_class       = var.db_instance_class
  db_name              = var.db_name
  username             = var.db_username
  password             = random_password.db_password.result
  parameter_group_name = aws_db_parameter_group.mysql.name

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  multi_az               = false # Cost optimization - no multi-AZ
  publicly_accessible    = true  # Allow access from EC2 via security group
  storage_encrypted      = false # Enable for production if needed
  copy_tags_to_snapshot  = true

  # Backup Configuration
  backup_retention_period = var.db_backup_retention
  backup_window           = "03:00-04:00"         # UTC
  maintenance_window      = "mon:04:00-mon:05:00" # UTC

  # Disable automated minor version upgrades for stability
  auto_minor_version_upgrade = false

  # Deletion protection for production
  deletion_protection = true

  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.project}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"

  tags = {
    Name = "${var.project}-mysql"
  }

  depends_on = [
    aws_security_group.rds,
    aws_db_subnet_group.main
  ]
}

# ====== RDS PARAMETER GROUP (for MySQL) ======

resource "aws_db_parameter_group" "mysql" {
  name   = "${var.project}-mysql-params"
  family = "mysql${replace(var.db_engine_version, ".", "")}"

  # Connection and performance parameters
  parameter {
    name  = "max_connections"
    value = "1000"
  }

  parameter {
    name  = "slow_query_log"
    value = "1"
  }

  parameter {
    name  = "long_query_time"
    value = "2"
  }

  # Character set for Unicode support
  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }

  tags = {
    Name = "${var.project}-mysql-params"
  }
}

# ====== OUTPUTS ======

output "db_endpoint" {
  description = "RDS database endpoint"
  value       = aws_db_instance.mysql.endpoint
}

output "db_host" {
  description = "RDS database host"
  value       = aws_db_instance.mysql.address
}

output "db_port" {
  description = "RDS database port"
  value       = aws_db_instance.mysql.port
}

output "db_name" {
  description = "RDS database name"
  value       = aws_db_instance.mysql.db_name
}

output "db_username" {
  description = "RDS database username"
  value       = aws_db_instance.mysql.username
  sensitive   = true
}

output "db_password" {
  description = "RDS database password (stored in SSM Parameter Store)"
  value       = "Stored in SSM Parameter Store at /${var.project}/db/password"
}
