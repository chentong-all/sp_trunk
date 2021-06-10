/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50714
Source Host           : localhost:3306
Source Database       : sp

Target Server Type    : MYSQL
Target Server Version : 50714
File Encoding         : 65001

Date: 2020-09-11 10:42:00
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for id_generator_recorder
-- ----------------------------
DROP TABLE IF EXISTS `id_generator_recorder`;
CREATE TABLE `id_generator_recorder` (
  `id` varchar(255) NOT NULL,
  `target_value` int(11) NOT NULL COMMENT '记录的当前目标值',
  `step_size` int(11) NOT NULL COMMENT '当前值增长的步长',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='id生成记录表，每个id对应一种需要生成id的类型';

-- ----------------------------
-- Records of id_generator_recorder
-- ----------------------------
INSERT INTO `id_generator_recorder` VALUES ('Answer', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('Comment', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('Question', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('Reply', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('Subject', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('User', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('UserCashout', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('UserConvert', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('UserNews', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('UserRecharge', '1', '100');
INSERT INTO `id_generator_recorder` VALUES ('UserVote', '1', '100');
