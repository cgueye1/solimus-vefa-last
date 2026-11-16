# ====== This file is intentionally minimal ======
# Frontend infrastructure (S3 + CloudFront) is already defined in storage.tf
# This file is kept for organization and future expansions

# Note: The main frontend resources are:
# - aws_s3_bucket.frontend (in storage.tf)
# - aws_cloudfront_distribution.frontend (in storage.tf)
# - aws_iam_role.github_frontend_role (in github.tf)

# Any additional frontend-specific configurations can be added here

# Example: If you need to serve the frontend from Route 53 with a custom domain
# You would add an alias record here (currently handled in dns.tf)
