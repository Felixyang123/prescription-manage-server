-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prescription_id` bigint NOT NULL,
  `patient_id` varchar(50) NOT NULL,
  `pharmacy_id` bigint NOT NULL,
  `status` int NOT NULL DEFAULT '0',
  `requested_drugs` json DEFAULT NULL,
  `dispensed_drugs` json DEFAULT NULL,
  `failure_reason` varchar(255) DEFAULT NULL,
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_prescription_id` (`prescription_id`),
  KEY `idx_patient_id` (`patient_id`),
  KEY `idx_pharmacy_id` (`pharmacy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for drug
-- ----------------------------
DROP TABLE IF EXISTS `drug`;
CREATE TABLE `drug` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `manufacturer` varchar(100) NOT NULL,
  `batch_number` varchar(50) NOT NULL,
  `expiry_date` datetime NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `locked_stock` int NOT NULL DEFAULT '0',
  `allocated_stock` int NOT NULL DEFAULT '0',
  `current_stock` int NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for pharmacy
-- ----------------------------
DROP TABLE IF EXISTS `pharmacy`;
CREATE TABLE `pharmacy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `address` varchar(200) NOT NULL,
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for pharmacy_drug
-- ----------------------------
DROP TABLE IF EXISTS `pharmacy_drug`;
CREATE TABLE `pharmacy_drug` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pharmacy_id` bigint NOT NULL,
  `drug_id` bigint NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `locked_stock` int NOT NULL DEFAULT '0' ,
  `allocated_stock` int NOT NULL DEFAULT '0',
  `current_stock` int NOT NULL DEFAULT '0',
  `version` int NOT NULL DEFAULT '0',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pharmacy_id` (`pharmacy_id`),
  KEY `idx_drug_id` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for prescription
-- ----------------------------
DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patient_id` varchar(50) NOT NULL,
  `pharmacy_id` bigint NOT NULL,
  `status` int NOT NULL DEFAULT '0',
  `create_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_patient_id` (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Table structure for prescription_item
-- ----------------------------
DROP TABLE IF EXISTS `prescription_item`;
CREATE TABLE `prescription_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prescription_id` bigint NOT NULL,
  `drug_id` bigint NOT NULL,
  `dosage` varchar(50) NOT NULL,
  `quantity` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_prescription_id` (`prescription_id`),
  KEY `idx_drug_id` (`drug_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;