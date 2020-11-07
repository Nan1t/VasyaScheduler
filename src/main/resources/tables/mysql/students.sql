CREATE TABLE IF NOT EXISTS `subscribes_students` (
  `id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `messenger_id` varchar(512) NOT NULL,
  `messenger_type` INT NOT NULL,
  `schedule_name` varchar(64) NOT NULL
);