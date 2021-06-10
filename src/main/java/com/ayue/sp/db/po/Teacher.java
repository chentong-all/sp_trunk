package com.ayue.sp.db.po;

import java.util.Date;

public class Teacher {


        private Integer id;

        private Integer userId;

        private Integer ticket;

        private Date createTime;

        private Date updateTime;

        private Integer comment;

        private Integer dayTicket;

        private Integer day;

        public Integer getDayTicket() {
                return dayTicket;
        }

        public void setDayTicket(Integer dayTicket) {
                this.dayTicket = dayTicket;
        }

        public Integer getDay() {
                return day;
        }

        public void setDay(Integer day) {
                this.day = day;
        }

        public Integer getId() {
                return id;
        }

        public void setId(Integer id) {
                this.id = id;
        }

        public Integer getUserId() {
                return userId;
        }

        public void setUserId(Integer userId) {
                this.userId = userId;
        }

        public Integer getTicket() {
                return ticket;
        }

        public void setTicket(Integer ticket) {
                this.ticket = ticket;
        }

        public Date getCreateTime() {
                return createTime;
        }

        public void setCreateTime(Date createTime) {
                this.createTime = createTime;
        }

        public Date getUpdateTime() {
                return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
                this.updateTime = updateTime;
        }

        public Integer getComment() {
                return comment;
        }

        public void setComment(Integer comment) {
                this.comment = comment;
        }
}