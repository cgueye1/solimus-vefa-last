# ====== CONSOLIDATED OUTPUTS FOR SUMMARY ======
# (Other outputs are defined in their respective resource files)
# This file contains only the deployment summary

# ====== SUMMARY OUTPUT ======

output "deployment_summary" {
  description = "Summary of deployment"
  value = format(
    <<EOT
╔════════════════════════════════════════════════════════════════════╗
║                   SOLIMUS VEFA DEPLOYMENT SUMMARY                  ║
╠════════════════════════════════════════════════════════════════════╣
║ Project: %s (Environment: %s)
║ Region: %s
║
║ BACKEND (EC2):
║   Instance ID: %s
║   Public IP: %s
║   API Domain: https://%s
║
║ DATABASE (RDS MySQL):
║   Endpoint: %s
║   Database: %s
║   User: %s
║
║ STORAGE (S3 + CloudFront):
║   Files Bucket: %s
║   Frontend Bucket: %s
║
║ DNS:
║   Route 53 Zone ID: %s
║   Add these NS records to OVH for vefa.solimus.sn:
║   %s
║
║ NEXT STEPS:
║   1. Run 'terraform init' (if not done already)
║   2. Verify outputs: 'terraform output'
║   3. Add Route 53 NS records to OVH
║   4. Configure GitHub secrets with IAM role ARNs
║   5. Deploy frontend and backend via GitHub Actions
╚════════════════════════════════════════════════════════════════════╝
EOT
    ,
    var.project,
    var.environment,
    var.aws_region,
    aws_instance.backend.id,
    aws_eip.backend.public_ip,
    var.api_domain,
    aws_db_instance.mysql.endpoint,
    aws_db_instance.mysql.db_name,
    aws_db_instance.mysql.username,
    aws_s3_bucket.files.id,
    aws_s3_bucket.frontend.id,
    aws_route53_zone.main.zone_id,
    join("\n   ", formatlist("%s IN NS %s", [
      "vefa", "vefa", "vefa", "vefa"
    ], aws_route53_zone.main.name_servers))
  )
}
