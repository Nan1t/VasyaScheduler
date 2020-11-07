CREATE TABLE IF NOT EXISTS `subscribes_students` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `messenger_id` varchar(512) NOT NULL,
  `messenger_type` INTEGER NOT NULL,
  `schedule_name` varchar(64) NOT NULL
);