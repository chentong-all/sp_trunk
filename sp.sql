/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50714
Source Host           : localhost:3306
Source Database       : sp

Target Server Type    : MYSQL
Target Server Version : 50714
File Encoding         : 65001

Date: 2020-09-11 10:39:24
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for answer
-- ----------------------------
DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `question_id` int(11) DEFAULT NULL,
  `content` varchar(2048) DEFAULT NULL COMMENT '内容',
  `picture_url` varchar(2048) DEFAULT NULL COMMENT '图片URL(多个图片之间用逗号分隔)',
  `agree_count` int(11) DEFAULT '0' COMMENT '赞同(赞)数量',
  `comment_count` int(11) DEFAULT '0' COMMENT '评论数量',
  `forward_count` int(11) DEFAULT '0' COMMENT '转发数量',
  `click_count` int(11) DEFAULT '0' COMMENT '点击数量',
  `ticket_count` int(11) DEFAULT '0' COMMENT '得票数',
  `city_address` varchar(255) DEFAULT NULL COMMENT '城市地址',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_question_id` (`question_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='回答数据表';

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` int(11) NOT NULL,
  `answer_id` int(11) DEFAULT NULL COMMENT '回答id',
  `user_id` int(11) DEFAULT NULL,
  `content` varchar(1024) DEFAULT NULL COMMENT '内容',
  `agree_count` int(11) DEFAULT '0' COMMENT '点赞数量',
  `reply_count` int(11) DEFAULT '0' COMMENT '回复数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_answer_id` (`answer_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='评论数据表';

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
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `is_anonymous` bit(1) DEFAULT b'0' COMMENT '是否匿名',
  `type` tinyint(11) DEFAULT NULL COMMENT '类型（0为普通问题，1为快问）',
  `summary` varchar(1024) DEFAULT NULL COMMENT '概述',
  `content` varchar(2048) DEFAULT NULL COMMENT '内容',
  `picture_url` varchar(2048) DEFAULT NULL COMMENT '图片URL(多个图片之间用逗号分隔)',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别（1男，0女）',
  `birth_year` int(11) DEFAULT NULL COMMENT '出生年份',
  `relation` tinyint(4) DEFAULT NULL COMMENT '亲属关系',
  `labels` varchar(255) DEFAULT NULL COMMENT '学习标签id（用逗号分隔)',
  `crowd_subject_id` int(11) DEFAULT NULL COMMENT '人群专题id',
  `region_subject_id` int(11) DEFAULT NULL COMMENT '地区专题id',
  `other_subject_id` int(11) DEFAULT NULL COMMENT '其他专题id',
  `reward_ticket` int(11) DEFAULT '0' COMMENT '悬赏票数',
  `answer_count` int(11) DEFAULT '0' COMMENT '回答数量',
  `read_count` int(11) DEFAULT '0' COMMENT '阅读数量',
  `collection_count` int(11) DEFAULT '0' COMMENT '收藏数量',
  `forward_count` int(11) DEFAULT '0' COMMENT '转发数量',
  `best_answer_id` int(11) DEFAULT NULL COMMENT '最佳回答id',
  `city_address` varchar(255) DEFAULT NULL COMMENT '城市地址',
  `last_answer_time` datetime DEFAULT NULL COMMENT '最新回答时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='问题数据表';

-- ----------------------------
-- Table structure for question_link_record
-- ----------------------------
DROP TABLE IF EXISTS `question_link_record`;
CREATE TABLE `question_link_record` (
  `id` int(11) NOT NULL,
  `father_id` int(11) DEFAULT NULL COMMENT '问题的父id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_father_id` (`father_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='问题父子关系表';

-- ----------------------------
-- Table structure for reply
-- ----------------------------
DROP TABLE IF EXISTS `reply`;
CREATE TABLE `reply` (
  `id` int(11) NOT NULL,
  `comment_id` int(11) DEFAULT NULL COMMENT '评论id',
  `user_id` int(11) DEFAULT NULL,
  `target_user_id` int(11) DEFAULT NULL COMMENT '回复目标用户id',
  `content` varchar(1024) DEFAULT NULL COMMENT '内容',
  `agree_count` int(11) DEFAULT '0' COMMENT '点赞数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_comment_id` (`comment_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='回复数据表';

-- ----------------------------
-- Table structure for subject
-- ----------------------------
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject` (
  `id` int(11) NOT NULL,
  `type` tinyint(4) DEFAULT NULL COMMENT '类型（0健康，1技能，2地区,3人群）',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '封面',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `is_system` bit(1) DEFAULT b'0' COMMENT '是否系统',
  `admini_user_id` int(11) DEFAULT NULL COMMENT '管理员用户id',
  `member_count` int(11) DEFAULT '0' COMMENT '成员数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admini_user_id` (`admini_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='专题表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `appId` varchar(255) DEFAULT NULL,
  `gender` tinyint(4) DEFAULT '1' COMMENT '性别（1男，0女）',
  `name` varchar(255) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `notice` varchar(255) DEFAULT NULL COMMENT '签名',
  `profile` varchar(255) DEFAULT NULL COMMENT '个人简介',
  `labels` varchar(255) DEFAULT NULL COMMENT '标签id（用逗号分隔）',
  `history_rank` varchar(1024) DEFAULT NULL COMMENT '历史周排行（逗号分隔）',
  `history_ticket` varchar(1024) DEFAULT NULL COMMENT '历史周票数',
  `day_ticket` int(11) DEFAULT '0' COMMENT '日得票数',
  `week_ticket` int(11) DEFAULT '0' COMMENT '周的票数',
  `all_ticket` int(11) DEFAULT '0' COMMENT '总得票数',
  `ticket` int(11) DEFAULT '0' COMMENT '票数',
  `money` int(11) DEFAULT '0' COMMENT '余额',
  `pay_count` int(11) DEFAULT '0' COMMENT '总支付金额',
  `attestation_url` varchar(2048) DEFAULT NULL COMMENT '医师认证照片url(逗号分隔)',
  `like_count` int(11) DEFAULT '0' COMMENT '点赞数量',
  `forward_count` int(11) DEFAULT '0' COMMENT '转发数量',
  `is_system` bit(1) DEFAULT b'0' COMMENT '是否系统账号',
  `city_address` varchar(255) DEFAULT NULL COMMENT '城市地址',
  `day_vote_count` int(11) DEFAULT '0' COMMENT '日投票数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_appId` (`appId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户数据表';

-- ----------------------------
-- Table structure for user2subject
-- ----------------------------
DROP TABLE IF EXISTS `user2subject`;
CREATE TABLE `user2subject` (
  `user_id` int(11) NOT NULL,
  `subject_id` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`,`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户加入专题表';

-- ----------------------------
-- Table structure for user2visited
-- ----------------------------
DROP TABLE IF EXISTS `user2visited`;
CREATE TABLE `user2visited` (
  `question_id` int(11) NOT NULL,
  `target_user_id` int(11) NOT NULL COMMENT '被邀请用户id',
  `user_id` int(11) DEFAULT NULL COMMENT '邀请者',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`question_id`,`target_user_id`),
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='邀请回答表';

-- ----------------------------
-- Table structure for user_answer_agree
-- ----------------------------
DROP TABLE IF EXISTS `user_answer_agree`;
CREATE TABLE `user_answer_agree` (
  `user_id` int(11) NOT NULL,
  `answer_id` int(11) NOT NULL COMMENT '回答id',
  `create_time` datetime DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`user_id`,`answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户点赞回答表';

-- ----------------------------
-- Table structure for user_attention
-- ----------------------------
DROP TABLE IF EXISTS `user_attention`;
CREATE TABLE `user_attention` (
  `user_id` int(11) NOT NULL,
  `attention_user_id` int(11) NOT NULL COMMENT '关注的用户id',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`,`attention_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='玩家关注玩家关系表';

-- ----------------------------
-- Table structure for user_browse
-- ----------------------------
DROP TABLE IF EXISTS `user_browse`;
CREATE TABLE `user_browse` (
  `user_id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`,`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户浏览历史';

-- ----------------------------
-- Table structure for user_cashout
-- ----------------------------
DROP TABLE IF EXISTS `user_cashout`;
CREATE TABLE `user_cashout` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `money_count` int(11) DEFAULT NULL COMMENT '提现数量',
  `is_success` bit(1) DEFAULT NULL COMMENT '是否成功',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户提现记录表';

-- ----------------------------
-- Table structure for user_comment_agree
-- ----------------------------
DROP TABLE IF EXISTS `user_comment_agree`;
CREATE TABLE `user_comment_agree` (
  `user_id` int(11) NOT NULL,
  `comment_id` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`user_id`,`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户点赞评论表';

-- ----------------------------
-- Table structure for user_convert
-- ----------------------------
DROP TABLE IF EXISTS `user_convert`;
CREATE TABLE `user_convert` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `ticket_count` int(11) DEFAULT NULL COMMENT '兑换数量',
  `is_success` bit(1) DEFAULT NULL COMMENT '是否成功',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户票兑换余额表';

-- ----------------------------
-- Table structure for user_news
-- ----------------------------
DROP TABLE IF EXISTS `user_news`;
CREATE TABLE `user_news` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `type` tinyint(4) DEFAULT NULL COMMENT '消息类型(0问题被回答，1回答被采纳，2有人投票，3被邀请回答,4回答被评论，5评论被回复，6被其他用户关注)',
  `content` varchar(255) DEFAULT NULL COMMENT '消息内容',
  `param` varchar(255) DEFAULT NULL COMMENT '冗余参数',
  `is_read` bit(1) DEFAULT NULL COMMENT '是否已读',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户消息表';

-- ----------------------------
-- Table structure for user_question_collection
-- ----------------------------
DROP TABLE IF EXISTS `user_question_collection`;
CREATE TABLE `user_question_collection` (
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `question_id` int(11) NOT NULL COMMENT '问题id',
  `create_time` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`user_id`,`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户收藏问题表';

-- ----------------------------
-- Table structure for user_recharge
-- ----------------------------
DROP TABLE IF EXISTS `user_recharge`;
CREATE TABLE `user_recharge` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `money_count` int(11) DEFAULT NULL COMMENT '充值数量',
  `is_success` bit(1) DEFAULT NULL COMMENT '是否成功',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户充值记录表';

-- ----------------------------
-- Table structure for user_reply_agree
-- ----------------------------
DROP TABLE IF EXISTS `user_reply_agree`;
CREATE TABLE `user_reply_agree` (
  `user_id` int(11) NOT NULL,
  `reply_id` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`,`reply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户点赞回复表';

-- ----------------------------
-- Table structure for user_tipoffs
-- ----------------------------
DROP TABLE IF EXISTS `user_tipoffs`;
CREATE TABLE `user_tipoffs` (
  `target_user_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT '举报人',
  `type` tinyint(4) DEFAULT NULL COMMENT '举报类型',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `picture_url` varchar(1024) DEFAULT NULL COMMENT '图片url',
  `create_time` datetime DEFAULT NULL COMMENT '举报时间',
  PRIMARY KEY (`target_user_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户举报回答';

-- ----------------------------
-- Table structure for user_vote
-- ----------------------------
DROP TABLE IF EXISTS `user_vote`;
CREATE TABLE `user_vote` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT '投票用户id',
  `target_user_id` int(11) NOT NULL COMMENT '获得票用户id',
  `answer_id` int(11) DEFAULT NULL COMMENT '得票的回答id',
  `count` int(11) DEFAULT NULL COMMENT '票数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_target_user_id` (`target_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户投票历史';
