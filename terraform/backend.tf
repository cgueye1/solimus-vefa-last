# ====== IAM ROLE FOR EC2 (to access S3, SSM, CloudWatch) ======

resource "aws_iam_role" "backend_ec2_role" {
  name = "${var.project}-backend-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.project}-backend-ec2-role"
  }
}

# ====== IAM POLICY FOR S3 ACCESS ======

resource "aws_iam_role_policy" "backend_s3_access" {
  name = "${var.project}-backend-s3-access"
  role = aws_iam_role.backend_ec2_role.id

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
          aws_s3_bucket.files.arn,
          "${aws_s3_bucket.files.arn}/*"
        ]
      }
    ]
  })
}

# ====== IAM POLICY FOR CLOUDWATCH LOGS ======

resource "aws_iam_role_policy" "backend_cloudwatch_logs" {
  name = "${var.project}-backend-cloudwatch-logs"
  role = aws_iam_role.backend_ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Resource = "arn:aws:logs:${var.aws_region}:${var.account_id}:log-group:/aws/ec2/${var.project}*"
      }
    ]
  })
}

# ====== IAM POLICY FOR SSM SESSION MANAGER ======

resource "aws_iam_role_policy_attachment" "backend_ssm_policy" {
  role       = aws_iam_role.backend_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# ====== IAM INSTANCE PROFILE ======

resource "aws_iam_instance_profile" "backend_ec2_profile" {
  name = "${var.project}-backend-ec2-profile"
  role = aws_iam_role.backend_ec2_role.name
}

# ====== EC2 INSTANCE FOR BACKEND ======

resource "aws_instance" "backend" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.backend_instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.backend.id]

  iam_instance_profile        = aws_iam_instance_profile.backend_ec2_profile.name
  associate_public_ip_address = true
  monitoring                  = true

  user_data = base64encode(templatefile("${path.module}/templates/user_data.sh.tftpl", {
    project         = var.project
    db_host         = aws_db_instance.mysql.address
    db_port         = tostring(aws_db_instance.mysql.port)
    db_name         = var.db_name
    db_username     = var.db_username
    db_password     = random_password.db_password.result
    s3_bucket       = aws_s3_bucket.files.id
    aws_region      = var.aws_region
    backend_port    = tostring(var.backend_port)
    backend_image   = var.backend_image
    frontend_url    = "https://${var.domain}"
    api_url         = "https://${var.api_domain}"
    api_domain      = var.api_domain
    frontend_domain = var.frontend_domain
    spring_profiles = "prod"
  }))

  tags = {
    Name = "${var.project}-backend-ec2"
  }

  depends_on = [
    aws_db_instance.mysql,
    aws_security_group.backend
  ]

  lifecycle {
    create_before_destroy = true
  }
}

# ====== ELASTIC IP FOR EC2 ======

resource "aws_eip" "backend" {
  instance = aws_instance.backend.id
  domain   = "vpc"

  tags = {
    Name = "${var.project}-eip"
  }

  depends_on = [aws_internet_gateway.main]
}

# ====== DATA SOURCE: UBUNTU AMI ======

data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "state"
    values = ["available"]
  }
}

# ====== OUTPUTS ======

output "backend_instance_id" {
  description = "Backend EC2 instance ID"
  value       = aws_instance.backend.id
}

output "backend_public_ip" {
  description = "Backend EC2 public IP"
  value       = aws_eip.backend.public_ip
}

output "backend_private_ip" {
  description = "Backend EC2 private IP"
  value       = aws_instance.backend.private_ip
}

output "backend_dns_name" {
  description = "Backend EC2 DNS name"
  value       = aws_instance.backend.public_dns
}

output "backend_iam_role_arn" {
  description = "IAM role ARN for backend EC2"
  value       = aws_iam_role.backend_ec2_role.arn
}
