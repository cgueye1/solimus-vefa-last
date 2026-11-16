
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `RECOVER_YOUR_DATA_info` (
  `READ_ME` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `call_for_charge` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` bigint DEFAULT NULL,
  `expected_date` varchar(255) DEFAULT NULL,
  `fee` double NOT NULL,
  `file` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `status` enum('PENDING','PAID','OVERDUE') DEFAULT NULL,
  `updated_at` bigint DEFAULT NULL,
  `charge_owner_id` bigint NOT NULL,
  `real_estate_property_id` bigint NOT NULL,
  `payment_date` bigint DEFAULT NULL,
  `work_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl87mp29f7th9x3p95jbqkdybl` (`charge_owner_id`),
  KEY `FKb70bvahxktislgd74fr81gb2j` (`real_estate_property_id`),
  KEY `FKq87k1qsqj5djyd34f9vfhbf5l` (`work_id`),
  CONSTRAINT `FKb70bvahxktislgd74fr81gb2j` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`),
  CONSTRAINT `FKl87mp29f7th9x3p95jbqkdybl` FOREIGN KEY (`charge_owner_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKq87k1qsqj5djyd34f9vfhbf5l` FOREIGN KEY (`work_id`) REFERENCES `works` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatmessage` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(255) DEFAULT NULL,
  `content` longtext,
  `recipient_id` varchar(255) DEFAULT NULL,
  `sender_id` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chatroom` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_id` varchar(255) DEFAULT NULL,
  `last_message` longtext,
  `recipient_id` varchar(255) DEFAULT NULL,
  `sender_id` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) DEFAULT NULL,
  `unread` int NOT NULL,
  `recipient_user_id` bigint DEFAULT NULL,
  `sender_user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3mqrw1h12m0pgct66h8p5l4kh` (`recipient_user_id`),
  KEY `FKuxs8yjajse4nn7b41rslxlw9` (`sender_user_id`),
  CONSTRAINT `FK3mqrw1h12m0pgct66h8p5l4kh` FOREIGN KEY (`recipient_user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKuxs8yjajse4nn7b41rslxlw9` FOREIGN KEY (`sender_user_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `commentaire` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auteur` varchar(255) DEFAULT NULL,
  `auteurimg` varchar(255) DEFAULT NULL,
  `contenu` varchar(255) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `idauteur` bigint DEFAULT NULL,
  `parent_id` bigint DEFAULT NULL,
  `property_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9oco0eohaidt04itifl34ivyb` (`parent_id`),
  KEY `FKnq3nuoeg2yh7l7h0vv1lenpe3` (`property_id`),
  CONSTRAINT `FK9oco0eohaidt04itifl34ivyb` FOREIGN KEY (`parent_id`) REFERENCES `commentaire` (`id`),
  CONSTRAINT `FKnq3nuoeg2yh7l7h0vv1lenpe3` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `companies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `logo` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `primary_color` varchar(255) DEFAULT NULL,
  `secondary_color` varchar(255) DEFAULT NULL,
  `owner_id` bigint NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `ninea` varchar(255) DEFAULT NULL,
  `registration_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_2xrywqtkk3tty82u66fs3s7gm` (`owner_id`),
  CONSTRAINT `FK1t9cgvywlfabcktibt935vpwj` FOREIGN KEY (`owner_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `construction_phase_indicators` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_updated` datetime(6) DEFAULT NULL,
  `phase_name` varchar(255) DEFAULT NULL,
  `progress_percentage` int NOT NULL,
  `real_estate_property_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc65igop22ucuohedo7ccu14m6` (`real_estate_property_id`),
  CONSTRAINT `FKc65igop22ucuohedo7ccu14m6` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `docs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `init_pdf` varchar(255) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `signed_pdf` varchar(255) DEFAULT NULL,
  `titre` varchar(255) DEFAULT NULL,
  `real_estate_property_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5k4ewloexymtslyprmrd2n474` (`real_estate_property_id`),
  CONSTRAINT `FK5k4ewloexymtslyprmrd2n474` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `init_pdf` varchar(255) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `signed_pdf` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `property_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKasvtxjurgufqo1q9d1x33qxl0` (`property_id`),
  CONSTRAINT `FKasvtxjurgufqo1q9d1x33qxl0` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fee` double NOT NULL,
  `name` enum('LOTFEE') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_35136vtrplkcgvmkyjik3sgga` (`name`),
  UNIQUE KEY `UK35136vtrplkcgvmkyjik3sgga` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `founds_histories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `payed` bit(1) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `type` int NOT NULL,
  `rental_invoice_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_3xryjk6wh92a5do51krkxvtg6` (`user_id`),
  KEY `FKth1eruhb958vpj3yfos5g5fcc` (`rental_invoice_id`),
  CONSTRAINT `FKrwv7pimmht14tyiwxgi72o04e` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKth1eruhb958vpj3yfos5g5fcc` FOREIGN KEY (`rental_invoice_id`) REFERENCES `rental_invoice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `information_sheets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `notary_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtjlwoseucvbkwc8mt640mmvd3` (`notary_id`),
  CONSTRAINT `FKtjlwoseucvbkwc8mt640mmvd3` FOREIGN KEY (`notary_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `invoice_number` varchar(255) DEFAULT NULL,
  `paid` bit(1) NOT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `subscription2_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8ugaqba377on4544dnvnh8d5r` (`subscription2_id`),
  KEY `FKqh2qspeqssr9lxfo5krbr7k4h` (`user_id`),
  CONSTRAINT `FK8ugaqba377on4544dnvnh8d5r` FOREIGN KEY (`subscription2_id`) REFERENCES `subscriptions2` (`id`),
  CONSTRAINT `FKqh2qspeqssr9lxfo5krbr7k4h` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_request_pictures` (
  `job_request_id` bigint NOT NULL,
  `pictures` varchar(255) DEFAULT NULL,
  KEY `FKpnp1n9ljtob6j7ygv8harn03k` (`job_request_id`),
  CONSTRAINT `FKpnp1n9ljtob6j7ygv8harn03k` FOREIGN KEY (`job_request_id`) REFERENCES `job_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) NOT NULL,
  `state` enum('PENDING','ACCEPTED','REJECTED','COMPLETED') NOT NULL,
  `job_id` bigint NOT NULL,
  `property_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `service_provider_id` bigint DEFAULT NULL,
  `devis` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7dxwwtipgch9coe7e69gx0tu` (`job_id`),
  KEY `FKt1ng01jityetxnaolivag946` (`property_id`),
  KEY `FK2cko0gs9tk7rxxf7ela6an9ds` (`user_id`),
  KEY `FKlfovokbi60vy69nh0ino4cg1f` (`service_provider_id`),
  CONSTRAINT `FK2cko0gs9tk7rxxf7ela6an9ds` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FK7dxwwtipgch9coe7e69gx0tu` FOREIGN KEY (`job_id`) REFERENCES `solimus_jobs` (`id`),
  CONSTRAINT `FKlfovokbi60vy69nh0ino4cg1f` FOREIGN KEY (`service_provider_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKt1ng01jityetxnaolivag946` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meet_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `date_time` datetime(6) DEFAULT NULL,
  `duration` int NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `creator_id` bigint DEFAULT NULL,
  `description` longtext,
  `real_estate_property_id` bigint NOT NULL,
  `is_online` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4embm2hxi1lqcw673o6eyd29s` (`creator_id`),
  CONSTRAINT `FK4embm2hxi1lqcw673o6eyd29s` FOREIGN KEY (`creator_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meet_class_participants` (
  `meet_id` bigint NOT NULL,
  `participants_id` bigint NOT NULL,
  KEY `FKekbq5ob0bgs1gftmot0wuit7w` (`participants_id`),
  KEY `FKkp7swubssjhh2cpv3k54f5fky` (`meet_id`),
  CONSTRAINT `FKekbq5ob0bgs1gftmot0wuit7w` FOREIGN KEY (`participants_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKkp7swubssjhh2cpv3k54f5fky` FOREIGN KEY (`meet_id`) REFERENCES `meet_class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meet_files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `meet_id` bigint NOT NULL,
  `meet_files` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm22ap879s679sjac6acrbxs6h` (`meet_id`),
  CONSTRAINT `FKm22ap879s679sjac6acrbxs6h` FOREIGN KEY (`meet_id`) REFERENCES `meet_class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_read_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) DEFAULT NULL,
  `message_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK61kitrtvwau0ebh5troe3tjco` (`message_id`,`user_id`),
  KEY `FKeuiotiu2datamo7dxu72a40rr` (`user_id`),
  CONSTRAINT `FK6v2gsesvjkhu2n73ye3qig78` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`),
  CONSTRAINT `FKeuiotiu2datamo7dxu72a40rr` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text,
  `sent_at` datetime(6) DEFAULT NULL,
  `property_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2tb12e2h815swkfw6pdh0pr6l` (`property_id`),
  KEY `FK7jdngq39k1jxh7g62k1u38tye` (`sender_id`),
  CONSTRAINT `FK2tb12e2h815swkfw6pdh0pr6l` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`),
  CONSTRAINT `FK7jdngq39k1jxh7g62k1u38tye` FOREIGN KEY (`sender_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` longtext,
  `status` bit(1) NOT NULL,
  `property_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKomyou4iq4q0x63qjp3ksh99yv` (`property_id`),
  KEY `FK6ryqimd5uu9tsvil1cg0k6212` (`user_id`),
  CONSTRAINT `FK6ryqimd5uu9tsvil1cg0k6212` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKomyou4iq4q0x63qjp3ksh99yv` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `owner_property_area` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `area_in_square_meters` double NOT NULL,
  `area_owner_id` bigint NOT NULL,
  `real_estate_property_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK609svk5ipjt52le3f58lgnm3y` (`area_owner_id`),
  KEY `FK8g5fmpp1rbwuwfdvl6f11y3hf` (`real_estate_property_id`),
  CONSTRAINT `FK609svk5ipjt52le3f58lgnm3y` FOREIGN KEY (`area_owner_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FK8g5fmpp1rbwuwfdvl6f11y3hf` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_calls` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `created_at` bigint DEFAULT NULL,
  `expected_date` varchar(255) DEFAULT NULL,
  `file` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `percentage` double NOT NULL,
  `status` enum('PENDING','PAID','OVERDUE') DEFAULT NULL,
  `updated_at` bigint DEFAULT NULL,
  `real_estate_property_id` bigint NOT NULL,
  `tom` double NOT NULL,
  `tva` double NOT NULL,
  `base_amount` double NOT NULL,
  `ref` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlmixihojq8fx0u0v4hqoodo3l` (`real_estate_property_id`),
  CONSTRAINT `FKlmixihojq8fx0u0v4hqoodo3l` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `installment_count` int NOT NULL,
  `paid` bit(1) NOT NULL,
  `plan_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8taahhbr9848iyc2db6htb75g` (`plan_id`),
  KEY `FKinmn5nj28nlai59yfh9siv3u8` (`user_id`),
  CONSTRAINT `FK8taahhbr9848iyc2db6htb75g` FOREIGN KEY (`plan_id`) REFERENCES `subscription_plan2` (`id`),
  CONSTRAINT `FKinmn5nj28nlai59yfh9siv3u8` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `progress_album` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `last_updated` datetime(6) DEFAULT NULL,
  `phase_name` varchar(255) DEFAULT NULL,
  `real_estate_property_id` bigint NOT NULL,
  `entrance` bit(1) NOT NULL,
  `archived` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbbul7x8si0vjg87vopoyil5yl` (`real_estate_property_id`),
  CONSTRAINT `FKbbul7x8si0vjg87vopoyil5yl` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `progress_album_pictures` (
  `progress_album_id` bigint NOT NULL,
  `pictures` varchar(255) DEFAULT NULL,
  KEY `FKe131murejfx3tmp8tnxn0lghb` (`progress_album_id`),
  CONSTRAINT `FKe131murejfx3tmp8tnxn0lghb` FOREIGN KEY (`progress_album_id`) REFERENCES `progress_album` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_name` varchar(255) DEFAULT NULL,
  `is_parent` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `property_user_mapping` (
  `property_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`property_id`,`user_id`),
  KEY `FKmvlm5klvmhbfrd7srm9lh46qd` (`user_id`),
  CONSTRAINT `FKmvlm5klvmhbfrd7srm9lh46qd` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKnaqksijhhe9qqrr76yups1l2p` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rapport` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `last_updated` datetime(6) DEFAULT NULL,
  `pdf` varchar(255) DEFAULT NULL,
  `titre` varchar(255) DEFAULT NULL,
  `real_estate_property_id` bigint NOT NULL,
  `archived` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhefxanyp6cn00au37ba3dgkcw` (`real_estate_property_id`),
  CONSTRAINT `FKhefxanyp6cn00au37ba3dgkcw` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rating` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` longtext,
  `score` int NOT NULL,
  `job_request_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqom8ayvurmssups4j14w987is` (`job_request_id`),
  KEY `FKfv6cdv31tijmvq3hkqigt6bv6` (`user_id`),
  CONSTRAINT `FKfv6cdv31tijmvq3hkqigt6bv6` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKqom8ayvurmssups4j14w987is` FOREIGN KEY (`job_request_id`) REFERENCES `job_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `real_estate_properties` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `area` double NOT NULL,
  `available` bit(1) NOT NULL,
  `description` longtext,
  `discount` double NOT NULL,
  `has_bicycle_storage` bit(1) NOT NULL,
  `has_elevator` bit(1) NOT NULL,
  `has_garden` bit(1) NOT NULL,
  `has_gym` bit(1) NOT NULL,
  `has_hall` bit(1) NOT NULL,
  `has_laundry_room` bit(1) NOT NULL,
  `has_parking` bit(1) NOT NULL,
  `has_playground` bit(1) NOT NULL,
  `has_security_service` bit(1) NOT NULL,
  `has_shared_terrace` bit(1) NOT NULL,
  `has_storage_rooms` bit(1) NOT NULL,
  `has_swimming_pool` bit(1) NOT NULL,
  `has_waste_disposal_area` bit(1) NOT NULL,
  `latitude` double DEFAULT NULL,
  `legal_status` varchar(255) DEFAULT NULL,
  `level` int NOT NULL,
  `longitude` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `number` varchar(255) DEFAULT NULL,
  `number_of_lots` int NOT NULL,
  `number_of_rooms` int NOT NULL,
  `plan` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `reservation_fee` double NOT NULL,
  `status` enum('AVAILABLE','RESERVED','SOLD') DEFAULT NULL,
  `timestamp` bigint DEFAULT NULL,
  `agency_id` bigint DEFAULT NULL,
  `bank_id` bigint DEFAULT NULL,
  `notary_id` bigint DEFAULT NULL,
  `parent_property_id` bigint DEFAULT NULL,
  `promoter_id` bigint NOT NULL,
  `property_type_id` bigint NOT NULL,
  `lot_recipient_id` bigint DEFAULT NULL,
  `fees_file` double NOT NULL,
  `co_owner` bit(1) NOT NULL,
  `lot_fees_paid` bit(1) NOT NULL,
  `lot_fee_id` bigint DEFAULT NULL,
  `budget` double NOT NULL,
  `allocate_date` datetime(6) DEFAULT NULL,
  `rental` bit(1) NOT NULL,
  `commentcount` int NOT NULL,
  `rental_date` datetime(6) DEFAULT NULL,
  `sold_at` datetime(6) DEFAULT NULL,
  `is_mezzanine` bit(1) NOT NULL,
  `subscription2_id` bigint DEFAULT NULL,
  `version` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2w816rmevf3d67lxndnxqj168` (`agency_id`),
  KEY `FK69gvx7y8vso27t11r99yovimx` (`bank_id`),
  KEY `FK18ta9403bgvsgsbktws83pwrd` (`notary_id`),
  KEY `FKiwhyho8fyutph16ok4ti4f4nd` (`parent_property_id`),
  KEY `FKqoayvifb5cinocy1twoy9wi2x` (`promoter_id`),
  KEY `FKtqaon0v1j5dftcl4etelcqbes` (`property_type_id`),
  KEY `FKfse5ylhotrtw03aherjd4ery5` (`lot_recipient_id`),
  KEY `FKbnu7cl3g0ec47in5odapcv4v1` (`lot_fee_id`),
  KEY `FKnmt37u28s5ggc1eju1b52oe6r` (`subscription2_id`),
  CONSTRAINT `FK18ta9403bgvsgsbktws83pwrd` FOREIGN KEY (`notary_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FK2w816rmevf3d67lxndnxqj168` FOREIGN KEY (`agency_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FK69gvx7y8vso27t11r99yovimx` FOREIGN KEY (`bank_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKbnu7cl3g0ec47in5odapcv4v1` FOREIGN KEY (`lot_fee_id`) REFERENCES `fee` (`id`),
  CONSTRAINT `FKfse5ylhotrtw03aherjd4ery5` FOREIGN KEY (`lot_recipient_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKiwhyho8fyutph16ok4ti4f4nd` FOREIGN KEY (`parent_property_id`) REFERENCES `real_estate_properties` (`id`),
  CONSTRAINT `FKnmt37u28s5ggc1eju1b52oe6r` FOREIGN KEY (`subscription2_id`) REFERENCES `subscriptions2` (`id`),
  CONSTRAINT `FKqoayvifb5cinocy1twoy9wi2x` FOREIGN KEY (`promoter_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKtqaon0v1j5dftcl4etelcqbes` FOREIGN KEY (`property_type_id`) REFERENCES `property_types` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `real_estate_property_pictures` (
  `real_estate_property_id` bigint NOT NULL,
  `pictures` varchar(255) DEFAULT NULL,
  KEY `FK39oi78uo382scjjglcwi74vct` (`real_estate_property_id`),
  CONSTRAINT `FK39oi78uo382scjjglcwi74vct` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rental_contracts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `archived` bit(1) NOT NULL,
  `archived_at` datetime(6) DEFAULT NULL,
  `contract_file` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `property_id` bigint NOT NULL,
  `recipient_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhqhvvdbq7mrrdggry1b2rlej1` (`property_id`),
  KEY `FKlmnijiwnrx5duo8e2uoeaosac` (`recipient_id`),
  CONSTRAINT `FKhqhvvdbq7mrrdggry1b2rlej1` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`),
  CONSTRAINT `FKlmnijiwnrx5duo8e2uoeaosac` FOREIGN KEY (`recipient_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rental_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `due_date` datetime(6) DEFAULT NULL,
  `paid` bit(1) NOT NULL,
  `payment_date` datetime(6) DEFAULT NULL,
  `property_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf3qtpca6t7qfweejwigynb74i` (`property_id`),
  CONSTRAINT `FKf3qtpca6t7qfweejwigynb74i` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` bigint DEFAULT NULL,
  `status` enum('PROSPECT','RESERVED') DEFAULT NULL,
  `property_id` bigint NOT NULL,
  `reservataire_id` bigint NOT NULL,
  `validated_at` datetime(6) DEFAULT NULL,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `rejected_at` datetime(6) DEFAULT NULL,
  `rejection_reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeoo9m5xdlo2jsbkaw70me4n95` (`property_id`),
  KEY `FK3kk7f545arechnlngh2t0h8pt` (`reservataire_id`),
  CONSTRAINT `FK3kk7f545arechnlngh2t0h8pt` FOREIGN KEY (`reservataire_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKeoo9m5xdlo2jsbkaw70me4n95` FOREIGN KEY (`property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `signatory_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `has_signed` bit(1) NOT NULL,
  `signature_notes` varchar(500) DEFAULT NULL,
  `signed_at` datetime(6) DEFAULT NULL,
  `document_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqxhhg915ai90lb9ns94m96368` (`document_id`),
  KEY `FKem6tc9qxffkb9e5vmfktjcv4x` (`user_id`),
  CONSTRAINT `FKem6tc9qxffkb9e5vmfktjcv4x` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKqxhhg915ai90lb9ns94m96368` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `signature_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_ip_address` varchar(255) NOT NULL,
  `document_title` varchar(255) DEFAULT NULL,
  `project_name` varchar(255) DEFAULT NULL,
  `signature_notes` varchar(500) DEFAULT NULL,
  `signed_at` datetime(6) NOT NULL,
  `signer_name` varchar(255) DEFAULT NULL,
  `signer_phone` varchar(255) DEFAULT NULL,
  `document_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `signatory_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_signature_audit_signed_at` (`signed_at`),
  KEY `idx_signature_audit_signer_phone` (`signer_phone`),
  KEY `idx_signature_audit_project` (`project_id`),
  KEY `FK913yeqd5ob6fhh86h91uj4o18` (`document_id`),
  KEY `FKm4etwwgetfq4ewg3y799nemae` (`signatory_id`),
  CONSTRAINT `FK71opr1cetc74uu7om30hm2d6x` FOREIGN KEY (`project_id`) REFERENCES `real_estate_properties` (`id`),
  CONSTRAINT `FK913yeqd5ob6fhh86h91uj4o18` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`),
  CONSTRAINT `FKm4etwwgetfq4ewg3y799nemae` FOREIGN KEY (`signatory_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `solimus_jobs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `icon` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `installment_count` int NOT NULL,
  `name` enum('PROMOTEUR_PLAN','NOTAIRE_PLAN','AGENCY_PLAN','BANK_PLAN','JOB_PROVIDER') NOT NULL,
  `total_cost` double NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7jy7m6trk57ouig2a3cxx7690` (`name`),
  UNIQUE KEY `UK7jy7m6trk57ouig2a3cxx7690` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_plan2` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `installment_count` int NOT NULL,
  `label` varchar(255) NOT NULL,
  `name` enum('PROMOTEUR','NOTAIRE','RESERVATAIRE','BANK','AGENCY','ADMIN','PROPRIETAIRE','SYNDIC','LOCATAIRE','PRESTATAIRE','TOM','COPROPRIETAIRE','TENANT') NOT NULL,
  `project_limit` int NOT NULL,
  `total_cost` double NOT NULL,
  `unlimited_projects` bit(1) NOT NULL,
  `yearly_discount_rate` double NOT NULL,
  `plan_type_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh30sk172ec00998if3ne34spb` (`plan_type_id`),
  CONSTRAINT `FKh30sk172ec00998if3ne34spb` FOREIGN KEY (`plan_type_id`) REFERENCES `subscription_plan_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscription_plan_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(255) NOT NULL,
  `level` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_q60b3o76wh38w6xai6mpywf6n` (`label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriptions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `date_invoice` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `installment_count` int NOT NULL,
  `paid_amount` double NOT NULL,
  `start_date` date DEFAULT NULL,
  `status` enum('PENDING','PAID','OVERDUE') DEFAULT NULL,
  `subscription_plan_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `renewed` bit(1) NOT NULL,
  `reference` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_38ysuqt5gkofoj2f717wyrien` (`reference`),
  KEY `FKiu8wnm0oh1p9ppfddpg4kcr2u` (`subscription_plan_id`),
  KEY `FKlt2txh1ylqhiyxuecu0ucy87b` (`user_id`),
  CONSTRAINT `FKiu8wnm0oh1p9ppfddpg4kcr2u` FOREIGN KEY (`subscription_plan_id`) REFERENCES `subscription_plan` (`id`),
  CONSTRAINT `FKlt2txh1ylqhiyxuecu0ucy87b` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriptions2` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `current_project_count` int NOT NULL,
  `date_invoice` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `installment_count` int NOT NULL,
  `paid_amount` double NOT NULL,
  `remaining_projects` int NOT NULL,
  `renewed` bit(1) NOT NULL,
  `start_date` date DEFAULT NULL,
  `status` enum('PENDING','PAID','OVERDUE') DEFAULT NULL,
  `subscription_plans_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKsk1cc2kjakiev0nm281wf4vu9` (`user_id`,`subscription_plans_id`),
  KEY `FKpald3a5dii9u32xt4jrjb0kqy` (`subscription_plans_id`),
  CONSTRAINT `FK7n420pjitdsowwt3my94n4c` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`),
  CONSTRAINT `FKpald3a5dii9u32xt4jrjb0kqy` FOREIGN KEY (`subscription_plans_id`) REFERENCES `subscription_plan2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address_lots` (
  `user_id` bigint NOT NULL,
  `lot` varchar(255) DEFAULT NULL,
  `property_id` bigint DEFAULT NULL,
  `area` double NOT NULL,
  KEY `FKqlt0pd0731nuwjbwek5lwdgn6` (`user_id`),
  CONSTRAINT `FKqlt0pd0731nuwjbwek5lwdgn6` FOREIGN KEY (`user_id`) REFERENCES `user_vefa1` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_vefa1` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activated` bit(1) NOT NULL,
  `adress` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `notifiable` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `prenom` varchar(255) DEFAULT NULL,
  `profil` tinyint DEFAULT NULL,
  `technical_sheet` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `funds` double NOT NULL,
  `solimus_jobs_id` bigint DEFAULT NULL,
  `note` double NOT NULL,
  `id_card` varchar(255) DEFAULT NULL,
  `photo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_bx2iv1nrrme7mjc8bupgcc1ti` (`telephone`),
  UNIQUE KEY `unique_telephone` (`telephone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `work_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr5tb33v3b2lfrh3kj0oh16lwu` (`work_id`),
  CONSTRAINT `FKr5tb33v3b2lfrh3kj0oh16lwu` FOREIGN KEY (`work_id`) REFERENCES `works` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `work_pictures` (
  `work_id` bigint NOT NULL,
  `picture_url` varchar(255) DEFAULT NULL,
  KEY `FKtfgt4tnquaxrctukm7hslk0b4` (`work_id`),
  CONSTRAINT `FKtfgt4tnquaxrctukm7hslk0b4` FOREIGN KEY (`work_id`) REFERENCES `works` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `works` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `delivery_date` date DEFAULT NULL,
  `description` text,
  `price` double NOT NULL,
  `start_date` date DEFAULT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED') DEFAULT NULL,
  `titre` varchar(255) DEFAULT NULL,
  `real_estate_property_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8q4kfbkdv51dqllvqf2tm0hex` (`real_estate_property_id`),
  CONSTRAINT `FK8q4kfbkdv51dqllvqf2tm0hex` FOREIGN KEY (`real_estate_property_id`) REFERENCES `real_estate_properties` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

