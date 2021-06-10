package com.ayue.sp.core;

/**
 * 2020年8月20日
 *
 * @author ayue
 */
public interface IConstants {
        // ------------------------------用户------------------------------------
        /**
         * 离线时长
         */
        long OFF_LINE_TIME_MAX = 2 * 60 * 1000;
        /**
         * 用户状态--正常
         */
        byte USER_STATUS_0 = 0;
        /**
         * 用户状态--封号
         */
        byte USER_STATUS_1 = 1;

        // ------------------------------聊天------------------------------------
        /**
         * 消息最大数量
         */
        int MSG_MAX_COUNT = 100;
        /**
         * 消息保存最大天数
         */
        long MSG_MAX_TIME = 10 * 24 * 60 * 60 * 1000;
        /**
         * 聊天ID类型-聊天
         */
        byte ID_TYPE_CHAT = 0;
        /**
         * 聊天黑名单上限
         */
        int BLACK_LIST_MAX_COUNT = 100;
        // ------------------------------问题------------------------------------
        /**
         * 问题类型-普通问题
         */
        byte QUESTION_TYPE_0 = 0;
        /**
         * 问题类型-快问
         */
        byte QUESTION_TYPE_1 = 1;
        // ------------------------------性别------------------------------------
        /**
         * 性别-女
         */
        byte GENDER_0 = 0;
        /**
         * 性别-男
         */
        byte GENDER_1 = 1;
        // ------------------------------专题------------------------------------
        /**
         * 专题类型-健康
         */
        byte SUBJECT_TYPE_0 = 0;
        /**
         * 专题类型-地区
         */
        byte SUBJECT_TYPE_1 = 1;
        /**
         * 专题状态--正常
         */
        byte SUBJECT_STATUS_0 = 0;
        /**
         * 专题状态--封号
         */
        byte SUBJECT_STATUS_1 = 1;
        // ------------------------------消息------------------------------------
        /**
         * 消息类型-问题被回答
         */
        byte NEWS_TYPE_0 = 0;
        /**
         * 消息类型-回答被采纳
         */
        byte NEWS_TYPE_1 = 1;
        /**
         * 消息类型-有人投票
         */
        byte NEWS_TYPE_2 = 2;
        /**
         * 消息类型-被邀请回答
         */
        byte NEWS_TYPE_3 = 3;
        /**
         * 消息类型-回答被评论
         */
        byte NEWS_TYPE_4 = 4;
        /**
         * 消息类型-评论被回复
         */
        byte NEWS_TYPE_5 = 5;
        /**
         * 消息类型-被其他用户关注
         */
        byte NEWS_TYPE_6 = 6;
        /**
         * 消息类型-系统消息
         */
        byte NEWS_TYPE_7 = 7;
        /**
         * 消息类型-被邀请提问
         */
        byte NEWS_TYPE_8 = 8;
        // ------------------------------页数------------------------------------
        /**
         * 问答页数上限
         */
        int PAGE_SIZE = 20;
        // ------------------------------亲属关系------------------------------------
        /**
         * 亲属关系-本人
         */
        byte RELATION_0 = 0;
        /**
         * 亲属关系-子女
         */
        byte RELATION_1 = 1;
        /**
         * 亲属关系-父母
         */
        byte RELATION_2 = 2;
        /**
         * 亲属关系-配偶
         */
        byte RELATION_3 = 3;
        /**
         * 亲属关系-亲属
         */
        byte RELATION_4 = 4;
        /**
         * 亲属关系-朋友
         */
        byte RELATION_5 = 5;
        /**
         * 亲属关系-其他
         */
        byte RELATION_6 = 6;

        // ------------------------------举报---------------------------------------------------
        /**
         * 举报类型-垃圾广告
         */
        byte TIPOFF_0 = 0;
        /**
         * 举报类型-色情低俗
         */
        byte TIPOFF_1 = 1;
        /**
         * 举报类型-攻击谩骂
         */
        byte TIPOFF_2 = 2;
        /**
         * 举报类型-诈骗信息
         */
        byte TIPOFF_3 = 3;
        /**
         * 举报类型-其他问题
         */
        byte TIPOFF_4 = 4;

        // ------------------------------支付---------------------------------------------------
        /**
         * 充值比率
         */
        byte RECHARGE_RATE = 10;
        /**
         * 余额转换比率（初级）
         */
        double CONVERT_RATE_0 = 0.05;
        /**
         * 余额转换比率（中级）
         */
        double CONVERT_RATE_1 = 0.06;
        /**
         * 余额转换比率（高级）
         */
        double CONVERT_RATE_2 = 0.07;
        /**
         * 余额转换比率（特级）
         */
        double CONVERT_RATE_3 = 0.08;

        /**
         * 支付状态-待支付
         */
        byte RECHARGE_STATUS_0 = 0;
        /**
         * 支付状态-支付成功
         */
        byte RECHARGE_STATUS_1 = 1;
        /**
         * 支付状态-支付失败
         */
        byte RECHARGE_STATUS_2 = 2;

        /**
         * appId
         */
        String APP_ID = "wxb5590162df93b162";
        /**
         * 公众号的appSecret
         */
        String WX_APPSECRET = "6b003a7ca9d7a219a19f80fe11b01966";
        /**
         * 支付appId
         */
        String PAY_APP_ID = "wx2111293138fb9713";
        /**
         * 微信支付商户号
         */
        String MCH_ID ="1244356902";
        /**
         * 商户API密钥key
         */
        String WX_PARTNERKEY ="62a86702ce684c918d3a188759d8b218";
        /**
         * 微信的统一下单地址
         */
        String WX_PAYURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        /**
         * 微信的订单
         */
        String WX_PAYORDERURL = "https://api.mch.weixin.qq.com/pay/orderquery";
        /**
         * 微信的提现地址
         */
        String WX_CASHOUTURL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
        /**
         * 微信的转发token
         */
        String WX_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
        /**
         * 微信的转发票
         */
        String WX_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
        /**
         * 微信的模板凭证
         */
        String WX_TOKEN_Message_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
        /**
         * 微信的模板消息发送
         */
        String WX_Message_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";
        /**
         * 微信的模板id
         */
        String WX_Message_ID = "-tJMKfrxi8qz88exFkIhM4furD80D-RCddpNWWjVL3g";

        /**
         * 微信的模板类型:用户发给不在线的聊天
         */
        Integer MESSAGE_TYPE_CHAT = 1;
        /**
         * 微信的模板类型:问题被回答
         */
        Integer MESSAGE_TYPE_QUESTION = 2;
        /**
         * 微信的模板类型:有人投票
         */
        Integer MESSAGE_TYPE_TICKET = 3;
        /**
         * 微信的模板类型:系统通知
         */
        Integer MESSAGE_TYPE_SYSTEM = 4;
        /**
         * 微信的模板类型:付费咨询
         */
        Integer MESSAGE_TYPE_PRICE = 5;
        /**
         * 微信的模板类型:拒绝咨询
         */
        Integer MESSAGE_TYPE_ACCEPT = 6;

        /**
         * 能量豆类型：提问
         */
        Integer ENERGY_BEAN_1 = 1;
        /**
         * 能量豆类型：回答
         */
        Integer ENERGY_BEAN_2 = 2;
        /**
         * 能量豆类型：点赞
         */
        Integer ENERGY_BEAN_3 = 3;
        /**
         * 能量豆类型：评论
         */
        Integer ENERGY_BEAN_4 = 4;
        /**
         * 能量豆类型：回复
         */
        Integer ENERGY_BEAN_5 = 5;
        /**
         * 能量豆类型：邀请提问
         */
        Integer ENERGY_BEAN_6 = 6;
        /**
         * 能量豆类型：邀请回答
         */
        Integer ENERGY_BEAN_7 = 7;
        /**
         * 能量豆类型：聊天
         */
        Integer ENERGY_BEAN_8 = 8;
        /**
         * 能量豆类型：投票
         */
        Integer ENERGY_BEAN_9 = 9;
        /**
         * 能量豆类型：抽奖
         */
        Integer ENERGY_BEAN_10 = 10;
        /**
         * 能量豆类型：签到
         */
        Integer ENERGY_BEAN_11 = 11;

        /**
         * 邀请类型：提问
         */
        Integer QUESTION_TYPE = 0;
        /**
         * 邀请类型：回答
         */
        Integer ANSWER_TYPE = 1;

        /**
         * 发布消息类型：全部
         */
        Integer USER_NEWS_TYPE = 0;
        /**
         * 发布消息类型：认证老师
         */
        Integer TEACHER_NEWS_TYPE = 1;


}
