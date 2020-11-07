CREATE TABLE IF NOT EXISTS `subscribes_teachers` (
  `id` IDENTITY PRIMARY KEY,
  `messenger_id` varchar(512) NOT NULL,
  `messenger_type` INT NOT NULL,
  `first_name` varchar(32) NOT NULL,
  `last_name` varchar(128) NOT NULL,
  `patronymic` varchar(32) NOT NULL
);