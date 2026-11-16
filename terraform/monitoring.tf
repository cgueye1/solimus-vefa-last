# ====== CLOUDWATCH LOG GROUP FOR BACKEND ======

resource "aws_cloudwatch_log_group" "backend" {
  name              = "/aws/ec2/${var.project}"
  retention_in_days = 30

  tags = {
    Name = "${var.project}-backend-logs"
  }
}

# ====== CLOUDWATCH ALARMS ======

# CPU Utilization Alarm
resource "aws_cloudwatch_metric_alarm" "cpu_utilization" {
  count               = var.enable_monitoring ? 1 : 0
  alarm_name          = "${var.project}-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "Alert when CPU exceeds 80%"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alarms[0].arn] : []

  dimensions = {
    InstanceId = aws_instance.backend.id
  }

  tags = {
    Name = "${var.project}-cpu-alarm"
  }
}

# Database Connections Alarm
resource "aws_cloudwatch_metric_alarm" "database_connections" {
  count               = var.enable_monitoring ? 1 : 0
  alarm_name          = "${var.project}-high-db-connections"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "Alert when DB connections exceed 80"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alarms[0].arn] : []

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.mysql.id
  }

  tags = {
    Name = "${var.project}-db-connections-alarm"
  }
}

# RDS Storage Space Alarm
resource "aws_cloudwatch_metric_alarm" "database_storage" {
  count               = var.enable_monitoring ? 1 : 0
  alarm_name          = "${var.project}-low-db-storage"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = "300"
  statistic           = "Average"
  threshold           = "2147483648" # 2GB
  alarm_description   = "Alert when DB free storage is below 2GB"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alarms[0].arn] : []

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.mysql.id
  }

  tags = {
    Name = "${var.project}-db-storage-alarm"
  }
}

# EC2 Status Checks Alarm
resource "aws_cloudwatch_metric_alarm" "instance_status" {
  count               = var.enable_monitoring ? 1 : 0
  alarm_name          = "${var.project}-instance-status-check-failed"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "2"
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = "60"
  statistic           = "Sum"
  threshold           = "1"
  alarm_description   = "Alert when EC2 instance status check fails"
  alarm_actions       = var.alarm_email != "" ? [aws_sns_topic.alarms[0].arn] : []

  dimensions = {
    InstanceId = aws_instance.backend.id
  }

  tags = {
    Name = "${var.project}-instance-status-alarm"
  }
}

# ====== SNS TOPIC FOR ALARMS ======

resource "aws_sns_topic" "alarms" {
  count = var.alarm_email != "" ? 1 : 0
  name  = "${var.project}-alarms"

  tags = {
    Name = "${var.project}-alarms-topic"
  }
}

resource "aws_sns_topic_subscription" "alarms" {
  count     = var.alarm_email != "" ? 1 : 0
  topic_arn = aws_sns_topic.alarms[0].arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

# ====== OUTPUTS ======

output "cloudwatch_log_group_name" {
  description = "CloudWatch log group name"
  value       = aws_cloudwatch_log_group.backend.name
}

output "cloudwatch_log_group_arn" {
  description = "CloudWatch log group ARN"
  value       = aws_cloudwatch_log_group.backend.arn
}
