# ====== S3 BUCKET FOR FILE UPLOADS ======

resource "aws_s3_bucket" "files" {
  bucket = "${var.files_bucket_name}-${var.account_id}"

  tags = {
    Name = "${var.project}-files-bucket"
  }
}

# ====== S3 BUCKET VERSIONING (for safety) ======

resource "aws_s3_bucket_versioning" "files" {
  bucket = aws_s3_bucket.files.id

  versioning_configuration {
    status = "Enabled"
  }
}

# ====== S3 BUCKET ENCRYPTION ======

resource "aws_s3_bucket_server_side_encryption_configuration" "files" {
  bucket = aws_s3_bucket.files.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# ====== S3 BUCKET PUBLIC ACCESS BLOCK ======

resource "aws_s3_bucket_public_access_block" "files" {
  bucket = aws_s3_bucket.files.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ====== S3 BUCKET CORS CONFIGURATION ======

resource "aws_s3_bucket_cors_configuration" "files" {
  bucket = aws_s3_bucket.files.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD", "PUT", "POST"]
    allowed_origins = [
      "https://${var.domain}",
      "https://${var.frontend_domain}",
      "https://${var.api_domain}"
    ]
    expose_headers  = ["x-amz-version-id"]
    max_age_seconds = 3000
  }
}

# ====== S3 BUCKET LIFECYCLE POLICY (clean up old versions) ======

resource "aws_s3_bucket_lifecycle_configuration" "files" {
  bucket = aws_s3_bucket.files.id

  rule {
    id     = "delete-old-versions"
    status = "Enabled"

    noncurrent_version_expiration {
      noncurrent_days = 90
    }
  }

  rule {
    id     = "delete-incomplete-uploads"
    status = "Enabled"

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}

# ====== S3 BUCKET FOR FRONTEND (Static Website) ======

resource "aws_s3_bucket" "frontend" {
  bucket = "${var.frontend_bucket_name}-${var.account_id}"

  tags = {
    Name = "${var.project}-frontend-bucket"
  }
}

# ====== FRONTEND S3 PUBLIC ACCESS BLOCK ======

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ====== FRONTEND S3 VERSIONING ======

resource "aws_s3_bucket_versioning" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  versioning_configuration {
    status = "Enabled"
  }
}

# ====== S3 BUCKET POLICY FOR CLOUDFRONT (Frontend) ======

resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontAccess"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.frontend.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = aws_cloudfront_distribution.frontend.arn
          }
        }
      }
    ]
  })
}

# ====== CLOUDFRONT DISTRIBUTION FOR FILES BUCKET ======

resource "aws_cloudfront_distribution" "files" {
  enabled      = true
  comment      = "${var.project} files distribution"
  price_class  = "PriceClass_200"

  origin {
    domain_name = aws_s3_bucket.files.bucket_regional_domain_name
    origin_id   = "S3Files"

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.files.cloudfront_access_identity_path
    }
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3Files"

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    default_ttl            = var.cloudfront_ttl
    min_ttl                = var.cloudfront_min_ttl
    max_ttl                = var.cloudfront_max_ttl
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = {
    Name = "${var.project}-files-cf"
  }
}

# ====== CLOUDFRONT ORIGIN ACCESS IDENTITY FOR FILES ======

resource "aws_cloudfront_origin_access_identity" "files" {
  comment = "${var.project} OAI for files"
}

# ====== CLOUDFRONT DISTRIBUTION FOR FRONTEND ======

resource "aws_cloudfront_distribution" "frontend" {
  enabled      = true
  comment      = "${var.project} frontend distribution"
  price_class  = "PriceClass_200"

  origin {
    domain_name = aws_s3_bucket.frontend.bucket_regional_domain_name
    origin_id   = "S3Frontend"

    s3_origin_config {
      origin_access_identity = aws_cloudfront_origin_access_identity.frontend.cloudfront_access_identity_path
    }
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3Frontend"

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    default_ttl            = 3600
    min_ttl                = 0
    max_ttl                = 86400
  }

  # For SPA routing - serve index.html for missing files
  custom_error_response {
    error_code            = 404
    response_code         = 200
    response_page_path    = "/index.html"
    error_caching_min_ttl = 300
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = {
    Name = "${var.project}-frontend-cf"
  }
}

# ====== CLOUDFRONT ORIGIN ACCESS IDENTITY FOR FRONTEND ======

resource "aws_cloudfront_origin_access_identity" "frontend" {
  comment = "${var.project} OAI for frontend"
}

# ====== OUTPUTS ======

output "s3_files_bucket_name" {
  description = "S3 files bucket name"
  value       = aws_s3_bucket.files.id
}

output "s3_files_bucket_arn" {
  description = "S3 files bucket ARN"
  value       = aws_s3_bucket.files.arn
}

output "s3_frontend_bucket_name" {
  description = "S3 frontend bucket name"
  value       = aws_s3_bucket.frontend.id
}

output "s3_frontend_bucket_arn" {
  description = "S3 frontend bucket ARN"
  value       = aws_s3_bucket.frontend.arn
}

output "cloudfront_files_domain_name" {
  description = "CloudFront domain name for files"
  value       = aws_cloudfront_distribution.files.domain_name
}

output "cloudfront_frontend_domain_name" {
  description = "CloudFront domain name for frontend"
  value       = aws_cloudfront_distribution.frontend.domain_name
}
