-- Create Database
CREATE DATABASE IF NOT EXISTS padesserversigner;
USE padesserversigner;

CREATE TABLE `user` (
  `credential_id` varchar(45) NOT NULL,
  `phone_number` varchar(45) DEFAULT NULL,
  `pin` varchar(45) DEFAULT NULL,
  `token` varchar(5000) DEFAULT NULL,
  `signing_date` varchar(45) DEFAULT NULL,
  `signing_level` varchar(45) DEFAULT NULL,
  `num_signatures` int DEFAULT NULL,
  `document` blob,
  `certificates` blob,
  `signature_cc` varchar(500) DEFAULT NULL,
  `reason` varchar(45) DEFAULT NULL,
  `location` varchar(45) DEFAULT NULL,
  `contact_info` varchar(45) DEFAULT NULL,
  `image` blob,
  `xaxis` int DEFAULT NULL,
  `yaxis` int DEFAULT NULL,
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `image_text` varchar(45) DEFAULT NULL,
  `sad` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`credential_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci