package com.ayue.sp.db.po;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserCashoutExample {
    /**
         * This field was generated by MyBatis Generator. This field corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        protected String orderByClause;
        /**
         * This field was generated by MyBatis Generator. This field corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        protected boolean distinct;
        /**
         * This field was generated by MyBatis Generator. This field corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        protected List<Criteria> oredCriteria;

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public UserCashoutExample() {
                oredCriteria = new ArrayList<Criteria>();
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void setOrderByClause(String orderByClause) {
                this.orderByClause = orderByClause;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public String getOrderByClause() {
                return orderByClause;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void setDistinct(boolean distinct) {
                this.distinct = distinct;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public boolean isDistinct() {
                return distinct;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public List<Criteria> getOredCriteria() {
                return oredCriteria;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void or(Criteria criteria) {
                oredCriteria.add(criteria);
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public Criteria or() {
                Criteria criteria = createCriteriaInternal();
                oredCriteria.add(criteria);
                return criteria;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public Criteria createCriteria() {
                Criteria criteria = createCriteriaInternal();
                if (oredCriteria.size() == 0) {
                        oredCriteria.add(criteria);
                }
                return criteria;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        protected Criteria createCriteriaInternal() {
                Criteria criteria = new Criteria();
                return criteria;
        }

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public void clear() {
                oredCriteria.clear();
                orderByClause = null;
                distinct = false;
        }

        /**
         * This class was generated by MyBatis Generator. This class corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        protected abstract static class GeneratedCriteria {
                protected List<Criterion> criteria;

                protected GeneratedCriteria() {
                        super();
                        criteria = new ArrayList<Criterion>();
                }

                public boolean isValid() {
                        return criteria.size() > 0;
                }

                public List<Criterion> getAllCriteria() {
                        return criteria;
                }

                public List<Criterion> getCriteria() {
                        return criteria;
                }

                protected void addCriterion(String condition) {
                        if (condition == null) {
                                throw new RuntimeException("Value for condition cannot be null");
                        }
                        criteria.add(new Criterion(condition));
                }

                protected void addCriterion(String condition, Object value, String property) {
                        if (value == null) {
                                throw new RuntimeException("Value for " + property + " cannot be null");
                        }
                        criteria.add(new Criterion(condition, value));
                }

                protected void addCriterion(String condition, Object value1, Object value2, String property) {
                        if (value1 == null || value2 == null) {
                                throw new RuntimeException("Between values for " + property + " cannot be null");
                        }
                        criteria.add(new Criterion(condition, value1, value2));
                }

                public Criteria andPartnerTradeNoIsNull() {
                        addCriterion("partner_trade_no is null");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoIsNotNull() {
                        addCriterion("partner_trade_no is not null");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoEqualTo(String value) {
                        addCriterion("partner_trade_no =", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoNotEqualTo(String value) {
                        addCriterion("partner_trade_no <>", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoGreaterThan(String value) {
                        addCriterion("partner_trade_no >", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoGreaterThanOrEqualTo(String value) {
                        addCriterion("partner_trade_no >=", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoLessThan(String value) {
                        addCriterion("partner_trade_no <", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoLessThanOrEqualTo(String value) {
                        addCriterion("partner_trade_no <=", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoLike(String value) {
                        addCriterion("partner_trade_no like", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoNotLike(String value) {
                        addCriterion("partner_trade_no not like", value, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoIn(List<String> values) {
                        addCriterion("partner_trade_no in", values, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoNotIn(List<String> values) {
                        addCriterion("partner_trade_no not in", values, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoBetween(String value1, String value2) {
                        addCriterion("partner_trade_no between", value1, value2, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andPartnerTradeNoNotBetween(String value1, String value2) {
                        addCriterion("partner_trade_no not between", value1, value2, "partnerTradeNo");
                        return (Criteria) this;
                }

                public Criteria andUserIdIsNull() {
                        addCriterion("user_id is null");
                        return (Criteria) this;
                }

                public Criteria andUserIdIsNotNull() {
                        addCriterion("user_id is not null");
                        return (Criteria) this;
                }

                public Criteria andUserIdEqualTo(Integer value) {
                        addCriterion("user_id =", value, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdNotEqualTo(Integer value) {
                        addCriterion("user_id <>", value, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdGreaterThan(Integer value) {
                        addCriterion("user_id >", value, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdGreaterThanOrEqualTo(Integer value) {
                        addCriterion("user_id >=", value, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdLessThan(Integer value) {
                        addCriterion("user_id <", value, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdLessThanOrEqualTo(Integer value) {
                        addCriterion("user_id <=", value, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdIn(List<Integer> values) {
                        addCriterion("user_id in", values, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdNotIn(List<Integer> values) {
                        addCriterion("user_id not in", values, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdBetween(Integer value1, Integer value2) {
                        addCriterion("user_id between", value1, value2, "userId");
                        return (Criteria) this;
                }

                public Criteria andUserIdNotBetween(Integer value1, Integer value2) {
                        addCriterion("user_id not between", value1, value2, "userId");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountIsNull() {
                        addCriterion("money_count is null");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountIsNotNull() {
                        addCriterion("money_count is not null");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountEqualTo(Integer value) {
                        addCriterion("money_count =", value, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountNotEqualTo(Integer value) {
                        addCriterion("money_count <>", value, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountGreaterThan(Integer value) {
                        addCriterion("money_count >", value, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountGreaterThanOrEqualTo(Integer value) {
                        addCriterion("money_count >=", value, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountLessThan(Integer value) {
                        addCriterion("money_count <", value, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountLessThanOrEqualTo(Integer value) {
                        addCriterion("money_count <=", value, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountIn(List<Integer> values) {
                        addCriterion("money_count in", values, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountNotIn(List<Integer> values) {
                        addCriterion("money_count not in", values, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountBetween(Integer value1, Integer value2) {
                        addCriterion("money_count between", value1, value2, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andMoneyCountNotBetween(Integer value1, Integer value2) {
                        addCriterion("money_count not between", value1, value2, "moneyCount");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessIsNull() {
                        addCriterion("is_success is null");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessIsNotNull() {
                        addCriterion("is_success is not null");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessEqualTo(Boolean value) {
                        addCriterion("is_success =", value, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessNotEqualTo(Boolean value) {
                        addCriterion("is_success <>", value, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessGreaterThan(Boolean value) {
                        addCriterion("is_success >", value, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessGreaterThanOrEqualTo(Boolean value) {
                        addCriterion("is_success >=", value, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessLessThan(Boolean value) {
                        addCriterion("is_success <", value, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessLessThanOrEqualTo(Boolean value) {
                        addCriterion("is_success <=", value, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessIn(List<Boolean> values) {
                        addCriterion("is_success in", values, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessNotIn(List<Boolean> values) {
                        addCriterion("is_success not in", values, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessBetween(Boolean value1, Boolean value2) {
                        addCriterion("is_success between", value1, value2, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andIsSuccessNotBetween(Boolean value1, Boolean value2) {
                        addCriterion("is_success not between", value1, value2, "isSuccess");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeIsNull() {
                        addCriterion("create_time is null");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeIsNotNull() {
                        addCriterion("create_time is not null");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeEqualTo(Date value) {
                        addCriterion("create_time =", value, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeNotEqualTo(Date value) {
                        addCriterion("create_time <>", value, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeGreaterThan(Date value) {
                        addCriterion("create_time >", value, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
                        addCriterion("create_time >=", value, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeLessThan(Date value) {
                        addCriterion("create_time <", value, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
                        addCriterion("create_time <=", value, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeIn(List<Date> values) {
                        addCriterion("create_time in", values, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeNotIn(List<Date> values) {
                        addCriterion("create_time not in", values, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeBetween(Date value1, Date value2) {
                        addCriterion("create_time between", value1, value2, "createTime");
                        return (Criteria) this;
                }

                public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
                        addCriterion("create_time not between", value1, value2, "createTime");
                        return (Criteria) this;
                }
        }

        /**
         * This class was generated by MyBatis Generator. This class corresponds to the database table user_cashout
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        public static class Criterion {
                private String condition;
                private Object value;
                private Object secondValue;
                private boolean noValue;
                private boolean singleValue;
                private boolean betweenValue;
                private boolean listValue;
                private String typeHandler;

                public String getCondition() {
                        return condition;
                }

                public Object getValue() {
                        return value;
                }

                public Object getSecondValue() {
                        return secondValue;
                }

                public boolean isNoValue() {
                        return noValue;
                }

                public boolean isSingleValue() {
                        return singleValue;
                }

                public boolean isBetweenValue() {
                        return betweenValue;
                }

                public boolean isListValue() {
                        return listValue;
                }

                public String getTypeHandler() {
                        return typeHandler;
                }

                protected Criterion(String condition) {
                        super();
                        this.condition = condition;
                        this.typeHandler = null;
                        this.noValue = true;
                }

                protected Criterion(String condition, Object value, String typeHandler) {
                        super();
                        this.condition = condition;
                        this.value = value;
                        this.typeHandler = typeHandler;
                        if (value instanceof List<?>) {
                                this.listValue = true;
                        } else {
                                this.singleValue = true;
                        }
                }

                protected Criterion(String condition, Object value) {
                        this(condition, value, null);
                }

                protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
                        super();
                        this.condition = condition;
                        this.value = value;
                        this.secondValue = secondValue;
                        this.typeHandler = typeHandler;
                        this.betweenValue = true;
                }

                protected Criterion(String condition, Object value, Object secondValue) {
                        this(condition, value, secondValue, null);
                }
        }

/**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table user_cashout
     *
     * @mbg.generated do_not_delete_during_merge Wed Aug 26 19:54:29 CST 2020
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }
}