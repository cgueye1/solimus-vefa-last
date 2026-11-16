#!/bin/bash
# Bootstrap script for Terraform S3 backend state
# This script creates the S3 bucket and DynamoDB table for Terraform state management
# Run this ONCE before running terraform init

set -e

# Configuration
ACCOUNT_ID="374784204755"
REGION="eu-west-3"
PROJECT="solimus-vefa"
STATE_BUCKET="${PROJECT}-tfstate-${ACCOUNT_ID}"
LOCK_TABLE="${PROJECT}-tflock"

echo "====== Terraform Backend Bootstrap ======"
echo "Account ID: $ACCOUNT_ID"
echo "Region: $REGION"
echo "State Bucket: $STATE_BUCKET"
echo "Lock Table: $LOCK_TABLE"
echo

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "ERROR: AWS CLI is not installed"
    exit 1
fi

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo "ERROR: AWS credentials not configured"
    echo "Run: aws login --profile solimus-vefa"
    echo "Or set AWS_PROFILE environment variable: export AWS_PROFILE=solimus-vefa"
    exit 1
fi

# Create S3 bucket for state
echo "Creating S3 bucket for Terraform state..."
if aws s3api head-bucket --bucket "$STATE_BUCKET" 2>/dev/null; then
    echo "✓ Bucket already exists: $STATE_BUCKET"
else
    echo "Creating bucket: $STATE_BUCKET"
    aws s3api create-bucket \
        --bucket "$STATE_BUCKET" \
        --region "$REGION" \
        --create-bucket-configuration LocationConstraint="$REGION" || true
    
    # Wait for bucket to be created
    sleep 2
fi

# Enable versioning on S3 bucket
echo "Enabling versioning on S3 bucket..."
aws s3api put-bucket-versioning \
    --bucket "$STATE_BUCKET" \
    --versioning-configuration Status=Enabled

# Enable encryption on S3 bucket
echo "Enabling server-side encryption on S3 bucket..."
aws s3api put-bucket-encryption \
    --bucket "$STATE_BUCKET" \
    --server-side-encryption-configuration '{
        "Rules": [
            {
                "ApplyServerSideEncryptionByDefault": {
                    "SSEAlgorithm": "AES256"
                }
            }
        ]
    }'

# Block public access to S3 bucket
echo "Blocking public access to S3 bucket..."
aws s3api put-public-access-block \
    --bucket "$STATE_BUCKET" \
    --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

# Create DynamoDB table for state locking
echo "Creating DynamoDB table for state locking..."
if aws dynamodb describe-table --table-name "$LOCK_TABLE" --region "$REGION" 2>/dev/null; then
    echo "✓ Table already exists: $LOCK_TABLE"
else
    echo "Creating table: $LOCK_TABLE"
    aws dynamodb create-table \
        --table-name "$LOCK_TABLE" \
        --attribute-definitions AttributeName=LockID,AttributeType=S \
        --key-schema AttributeName=LockID,KeyType=HASH \
        --billing-mode PAY_PER_REQUEST \
        --region "$REGION" || true
    
    # Wait for table to be created
    sleep 5
fi

echo
echo "✅ Bootstrap completed successfully!"
echo
echo "Next steps:"
echo "1. Update your terraform backend config with:"
echo "   bucket         = \"$STATE_BUCKET\""
echo "   key            = \"solimus-vefa/terraform.tfstate\""
echo "   region         = \"$REGION\""
echo "   encrypt        = true"
echo "   dynamodb_table = \"$LOCK_TABLE\""
echo
echo "2. Run: terraform init"
echo "   When prompted, confirm the backend migration"
echo
echo "3. Create terraform.tfvars from terraform.tfvars.example"
echo "4. Run: terraform plan"
echo "5. Run: terraform apply"
