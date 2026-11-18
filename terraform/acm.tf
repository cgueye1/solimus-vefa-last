# ====== ACM CERTIFICATE FOR CLOUDFRONT ======
# CloudFront requires ACM certificates in us-east-1 region

resource "aws_acm_certificate" "frontend" {
  provider            = aws.us_east_1
  domain_name         = var.domain
  validation_method   = "DNS"
  subject_alternative_names = [
    "*.${var.domain}"
  ]

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "${var.project}-frontend-cert"
  }
}

# DNS validation records for ACM certificate
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.frontend.domain_validation_options : dvo.domain => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = aws_route53_zone.main.zone_id
}

# Wait for certificate validation
resource "aws_acm_certificate_validation" "frontend" {
  provider        = aws.us_east_1
  certificate_arn = aws_acm_certificate.frontend.arn

  timeouts {
    create = "5m"
  }

  depends_on = [aws_route53_record.cert_validation]
}
