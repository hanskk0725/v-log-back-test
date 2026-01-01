-- v-log DDL
-- 실행 전 기존 테이블 삭제

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `follows`;
DROP TABLE IF EXISTS `likes`;
DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `tag_maps`;
DROP TABLE IF EXISTS `tags`;
DROP TABLE IF EXISTS `posts`;
DROP TABLE IF EXISTS `blogs`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

-- 테이블 생성

CREATE TABLE `users` (
  `user_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `email` varchar(255) UNIQUE NOT NULL,
  `password` varchar(255) NOT NULL,
  `nickname` varchar(255) UNIQUE NOT NULL,
  `created_at` datetime,
  `updated_at` datetime
);

CREATE TABLE `blogs` (
  `blog_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `created_at` datetime,
  `updated_at` datetime
);

CREATE TABLE `posts` (
  `post_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `blog_id` bigint NOT NULL,
  `title` varchar(255),
  `content` MEDIUMTEXT,
  `view_count` int DEFAULT 0,
  `like_count` int DEFAULT 0,
  `created_at` datetime,
  `updated_at` datetime
);

CREATE TABLE `tags` (
  `tag_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `title` varchar(255) UNIQUE NOT NULL,
  `created_at` datetime,
  `updated_at` datetime
);

CREATE TABLE `tag_maps` (
  `tag_map_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  `created_at` datetime,
  `updated_at` datetime
);

CREATE TABLE `comments` (
  `comment_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `parent_id` bigint,
  `content` text,
  `created_at` datetime,
  `updated_at` datetime
);

CREATE TABLE `likes` (
  `like_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `created_at` datetime,
  `updated_at` datetime
);

CREATE TABLE `follows` (
  `follow_id` bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL,
  `following_id` bigint NOT NULL,
  `created_at` datetime,
  `updated_at` datetime
);

-- 인덱스 생성

CREATE UNIQUE INDEX `tag_maps_index_0` ON `tag_maps` (`post_id`, `tag_id`);

CREATE UNIQUE INDEX `likes_index_1` ON `likes` (`user_id`, `post_id`);

CREATE UNIQUE INDEX `follows_index_2` ON `follows` (`follower_id`, `following_id`);

-- 외래키 설정

ALTER TABLE `blogs` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `posts` ADD FOREIGN KEY (`blog_id`) REFERENCES `blogs` (`blog_id`);

ALTER TABLE `tag_maps` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`);

ALTER TABLE `tag_maps` ADD FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`);

ALTER TABLE `comments` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `comments` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`);

ALTER TABLE `comments` ADD FOREIGN KEY (`parent_id`) REFERENCES `comments` (`comment_id`);

ALTER TABLE `likes` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `likes` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`post_id`);

ALTER TABLE `follows` ADD FOREIGN KEY (`follower_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `follows` ADD FOREIGN KEY (`following_id`) REFERENCES `users` (`user_id`);
