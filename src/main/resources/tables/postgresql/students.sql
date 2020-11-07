CREATE TABLE IF NOT EXISTS subscribes_students (
  id serial PRIMARY KEY,
  messenger_id varchar(512) NOT NULL,
  messenger_type integer NOT NULL,
  schedule_name varchar(64) NOT NULL
);