# adding nest credentials

# --- !Ups

ALTER TABLE User ADD COLUMN productId VARCHAR(50);
ALTER TABLE User ADD COLUMN productSecret VARCHAR(50);

# --- !Downs

ALTER TABLE User DROP COLUMN productId;
ALTER TABLE User DROP COLUMN productSecret;