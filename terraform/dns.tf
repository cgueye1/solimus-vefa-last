# ====== ROUTE 53 HOSTED ZONE FOR SUBDOMAIN ======
# This creates the delegation zone for vefa.solimus.sn
# Parent domain (solimus.sn) remains at OVH

resource "aws_route53_zone" "main" {
  name = var.domain

  tags = {
    Name = "${var.project}-zone"
  }
}

# ====== ROUTE 53 RECORDS FOR API ======

resource "aws_route53_record" "api" {
  zone_id = aws_route53_zone.main.zone_id
  name    = var.api_domain
  type    = "A"
  ttl     = 300
  records = [aws_eip.backend.public_ip]
}

# ====== ROUTE 53 RECORDS FOR MAIN DOMAIN (via CloudFront) ======

resource "aws_route53_record" "main_a" {
  zone_id = aws_route53_zone.main.zone_id
  name    = var.domain
  type    = "A"
  alias {
    name                   = aws_cloudfront_distribution.frontend.domain_name
    zone_id                = aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "main_aaaa" {
  zone_id = aws_route53_zone.main.zone_id
  name    = var.domain
  type    = "AAAA"
  alias {
    name                   = aws_cloudfront_distribution.frontend.domain_name
    zone_id                = aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}

# ====== NS RECORDS (For delegation from OVH) ======
# These NS records should be added to OVH for the subdomain vefa.solimus.sn

output "route53_zone_id" {
  description = "Route 53 hosted zone ID"
  value       = aws_route53_zone.main.zone_id
}

output "route53_nameservers" {
  description = "Route 53 nameservers to add to OVH"
  value       = aws_route53_zone.main.name_servers
}

output "route53_ns_records" {
  description = "NS records to add to OVH (solimus.sn zone)"
  value = format(
    "Add these 4 NS records to OVH for subdomain 'vefa':\n%s",
    join("\n", [
      "vefa 300 NS ${aws_route53_zone.main.name_servers[0]}",
      "vefa 300 NS ${aws_route53_zone.main.name_servers[1]}",
      "vefa 300 NS ${aws_route53_zone.main.name_servers[2]}",
      "vefa 300 NS ${aws_route53_zone.main.name_servers[3]}"
    ])
  )
}

output "api_dns_name" {
  description = "DNS name for API domain"
  value       = aws_route53_record.api.name
}

# L'ancien enregistrement "main" (vers l'EC2) devient "main_a" (vers CloudFront)
moved {
  from = aws_route53_record.main
  to   = aws_route53_record.main_a
}