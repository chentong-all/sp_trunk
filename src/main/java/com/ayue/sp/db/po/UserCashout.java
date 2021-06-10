package com.ayue.sp.db.po;

import java.util.Date;

public class UserCashout {

        /**
         * This field was generated by MyBatis Generator. This field corresponds to the database column user_cashout.partner_trade_no
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        private String partnerTradeNo;
        /**
         * This field was generated by MyBatis Generator. This field corresponds to the database column user_cashout.user_id
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        private Integer userId;
        /**
         * This field was generated by MyBatis Generator. This field corresponds to the database column user_cashout.money_count
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        private Integer moneyCount;
        /**
         * This field was generated by MyBatis Generator. This field corresponds to the database column user_cashout.is_success
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        private Boolean isSuccess;
        /**
         * This field was generated by MyBatis Generator. This field corresponds to the database column user_cashout.create_time
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        private Date createTime;

        /**
         * This method was generated by MyBatis Generator. This method returns the value of the database column user_cashout.partner_trade_no
         * @return  the value of user_cashout.partner_trade_no
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public String getPartnerTradeNo() {
                return partnerTradeNo;
        }

        /**
         * This method was generated by MyBatis Generator. This method sets the value of the database column user_cashout.partner_trade_no
         * @param partnerTradeNo  the value for user_cashout.partner_trade_no
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void setPartnerTradeNo(String partnerTradeNo) {
                this.partnerTradeNo = partnerTradeNo;
        }

        /**
         * This method was generated by MyBatis Generator. This method returns the value of the database column user_cashout.user_id
         * @return  the value of user_cashout.user_id
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public Integer getUserId() {
                return userId;
        }

        /**
         * This method was generated by MyBatis Generator. This method sets the value of the database column user_cashout.user_id
         * @param userId  the value for user_cashout.user_id
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void setUserId(Integer userId) {
                this.userId = userId;
        }

        /**
         * This method was generated by MyBatis Generator. This method returns the value of the database column user_cashout.money_count
         * @return  the value of user_cashout.money_count
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public Integer getMoneyCount() {
                return moneyCount;
        }

        /**
         * This method was generated by MyBatis Generator. This method sets the value of the database column user_cashout.money_count
         * @param moneyCount  the value for user_cashout.money_count
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void setMoneyCount(Integer moneyCount) {
                this.moneyCount = moneyCount;
        }

        /**
         * This method was generated by MyBatis Generator. This method returns the value of the database column user_cashout.is_success
         * @return  the value of user_cashout.is_success
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public Boolean getIsSuccess() {
                return isSuccess;
        }

        /**
         * This method was generated by MyBatis Generator. This method sets the value of the database column user_cashout.is_success
         * @param isSuccess  the value for user_cashout.is_success
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void setIsSuccess(Boolean isSuccess) {
                this.isSuccess = isSuccess;
        }

        /**
         * This method was generated by MyBatis Generator. This method returns the value of the database column user_cashout.create_time
         * @return  the value of user_cashout.create_time
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public Date getCreateTime() {
                return createTime;
        }

        /**
         * This method was generated by MyBatis Generator. This method sets the value of the database column user_cashout.create_time
         * @param createTime  the value for user_cashout.create_time
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void setCreateTime(Date createTime) {
                this.createTime = createTime;
        }
}