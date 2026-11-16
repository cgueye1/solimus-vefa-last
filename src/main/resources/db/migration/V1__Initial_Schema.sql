-- Flyway Migration V1: Initial Schema
-- This migration creates the base schema for Solimus Vefa application
-- Status: BASELINE
-- Created by: Copilot
-- Description: Initial database schema

-- ====== USERS TABLE ======
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `email` varchar(255) NOT NULL UNIQUE,
  `enabled` bit(1) DEFAULT b'1',
  `first_name` varchar(255),
  `is_account_non_expired` bit(1) DEFAULT b'1',
  `is_account_non_locked` bit(1) DEFAULT b'1',
  `is_credentials_non_expired` bit(1) DEFAULT b'1',
  `last_name` varchar(255),
  `password` varchar(255),
  `phone` varchar(255),
  `picture_url` varchar(500),
  `role` varchar(255),
  `status` varchar(50),
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `username` varchar(255),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_status` (`status`),
  KEY `idx_user_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== PROPERTIES TABLE (Application Settings) ======
CREATE TABLE IF NOT EXISTS `properties` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `description` text,
  `key` varchar(255) NOT NULL UNIQUE,
  `type` varchar(50),
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `value` longtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_properties_key` (`key`),
  KEY `idx_properties_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== ALBUMS TABLE ======
CREATE TABLE IF NOT EXISTS `album` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint,
  `description` text,
  `is_active` bit(1) DEFAULT b'1',
  `is_visible` bit(1) DEFAULT b'1',
  `name` varchar(255) NOT NULL,
  `order_index` int,
  `thumbnail_url` varchar(500),
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint,
  PRIMARY KEY (`id`),
  KEY `idx_album_user_id` (`user_id`),
  KEY `idx_album_created_by` (`created_by`),
  KEY `idx_album_is_active` (`is_active`),
  CONSTRAINT `fk_album_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_album_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== MEDIAS/FILES TABLE ======
CREATE TABLE IF NOT EXISTS `media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `album_id` bigint,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint,
  `description` text,
  `file_name` varchar(500) NOT NULL,
  `file_size` bigint,
  `file_type` varchar(100),
  `file_url` varchar(1000),
  `height` int,
  `is_active` bit(1) DEFAULT b'1',
  `is_visible` bit(1) DEFAULT b'1',
  `mime_type` varchar(100),
  `original_name` varchar(500),
  `order_index` int,
  `storage_key` varchar(500),
  `thumbnail_url` varchar(1000),
  `title` varchar(255),
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint,
  `width` int,
  PRIMARY KEY (`id`),
  KEY `idx_media_album_id` (`album_id`),
  KEY `idx_media_user_id` (`user_id`),
  KEY `idx_media_created_by` (`created_by`),
  KEY `idx_media_file_name` (`file_name`),
  KEY `idx_media_created_at` (`created_at`),
  CONSTRAINT `fk_media_album_id` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_media_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_media_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== SUBSCRIPTIONS TABLE ======
CREATE TABLE IF NOT EXISTS `subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `currency` varchar(10),
  `end_date` datetime,
  `is_active` bit(1) DEFAULT b'1',
  `order_id` varchar(255),
  `price` decimal(10,2),
  `start_date` datetime,
  `status` varchar(50),
  `subscription_type` varchar(100),
  `transaction_id` varchar(255),
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint,
  PRIMARY KEY (`id`),
  KEY `idx_subscription_user_id` (`user_id`),
  KEY `idx_subscription_status` (`status`),
  KEY `idx_subscription_is_active` (`is_active`),
  CONSTRAINT `fk_subscription_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== PAYMENTS TABLE ======
CREATE TABLE IF NOT EXISTS `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(10,2),
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `currency` varchar(10),
  `description` text,
  `order_id` varchar(255),
  `payment_method` varchar(100),
  `reference_id` varchar(255),
  `status` varchar(50),
  `transaction_id` varchar(255) UNIQUE,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint,
  PRIMARY KEY (`id`),
  KEY `idx_payment_user_id` (`user_id`),
  KEY `idx_payment_status` (`status`),
  KEY `idx_payment_transaction_id` (`transaction_id`),
  KEY `idx_payment_order_id` (`order_id`),
  CONSTRAINT `fk_payment_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== NOTIFICATIONS TABLE ======
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` bit(1) DEFAULT b'0',
  `message` text,
  `notification_type` varchar(100),
  `related_id` bigint,
  `status` varchar(50),
  `title` varchar(255),
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` bigint,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user_id` (`user_id`),
  KEY `idx_notification_is_read` (`is_read`),
  KEY `idx_notification_created_at` (`created_at`),
  CONSTRAINT `fk_notification_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== AUDIT LOG TABLE ======
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(100),
  `changed_values` json,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `entity_id` bigint,
  `entity_type` varchar(255),
  `ip_address` varchar(45),
  `old_values` json,
  `request_id` varchar(255),
  `user_agent` varchar(500),
  `user_id` bigint,
  PRIMARY KEY (`id`),
  KEY `idx_audit_log_user_id` (`user_id`),
  KEY `idx_audit_log_created_at` (`created_at`),
  KEY `idx_audit_log_entity_type` (`entity_type`),
  CONSTRAINT `fk_audit_log_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== SESSIONS TABLE ======
CREATE TABLE IF NOT EXISTS `session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` datetime,
  `ip_address` varchar(45),
  `is_active` bit(1) DEFAULT b'1',
  `last_activity` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `session_token` varchar(500) UNIQUE,
  `user_agent` varchar(500),
  `user_id` bigint,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_token` (`session_token`),
  KEY `idx_session_user_id` (`user_id`),
  KEY `idx_session_is_active` (`is_active`),
  KEY `idx_session_created_at` (`created_at`),
  CONSTRAINT `fk_session_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====== INDEXES FOR PERFORMANCE ======
-- Additional indexes for frequently queried combinations
CREATE INDEX idx_subscription_user_start_date ON subscription(user_id, start_date);
CREATE INDEX idx_media_album_created_at ON media(album_id, created_at);
CREATE INDEX idx_payment_user_status_date ON payment(user_id, status, created_at);

-- ====== VERSION INFO ======
-- Schema Version: 1.0
-- Initial schema with core tables for:
-- - User management and authentication
-- - Album and media (file) management
-- - Subscription and payment handling
-- - Notifications and audit logging
-- - Session management
