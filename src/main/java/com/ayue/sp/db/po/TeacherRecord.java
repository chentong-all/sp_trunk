package com.ayue.sp.db.po;

import java.util.Date;

public class TeacherRecord {


        private Integer id;

        private Integer userId;

        private Integer teacherId;

        private Integer ticket;

        private Integer comment;

        private Date createTime;

        private Integer isAccept;

        private String acceptContent;

        private Date updateTime;

        private Integer day;

        private Integer isUpdateTicket;

        private Integer teacherSex;

        private Integer teacherAge;

        private String teacherContent;

        private Integer isChoose;

        public Integer getIsChoose() {
                return isChoose;
        }

        public void setIsChoose(Integer isChoose) {
                this.isChoose = isChoose;
        }

        public Integer getTeacherSex() {
                return teacherSex;
        }

        public void setTeacherSex(Integer teacherSex) {
                this.teacherSex = teacherSex;
        }

        public Integer getTeacherAge() {
                return teacherAge;
        }

        public void setTeacherAge(Integer teacherAge) {
                this.teacherAge = teacherAge;
        }

        public String getTeacherContent() {
                return teacherContent;
        }

        public void setTeacherContent(String teacherContent) {
                this.teacherContent = teacherContent;
        }

        public Integer getIsUpdateTicket() {
                return isUpdateTicket;
        }

        public void setIsUpdateTicket(Integer isUpdateTicket) {
                this.isUpdateTicket = isUpdateTicket;
        }

        public Date getUpdateTime() {
                return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
                this.updateTime = updateTime;
        }

        public Integer getDay() {
                return day;
        }

        public void setDay(Integer day) {
                this.day = day;
        }

        public String getAcceptContent() {
                return acceptContent;
        }

        public void setAcceptContent(String acceptContent) {
                this.acceptContent = acceptContent;
        }

        public Integer getIsAccept() {
                return isAccept;
        }

        public void setIsAccept(Integer isAccept) {
                this.isAccept = isAccept;
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

        public Integer getComment() {
                return comment;
        }

        public void setComment(Integer comment) {
                this.comment = comment;
        }

        public Integer getTeacherId() {
                return teacherId;
        }

        public void setTeacherId(Integer teacherId) {
                this.teacherId = teacherId;
        }
}