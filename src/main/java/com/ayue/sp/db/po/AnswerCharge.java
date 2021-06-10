package com.ayue.sp.db.po;


import java.util.Date;

public class AnswerCharge {

    private Integer answerId;

    private Integer userId;

    private Date createTime;

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AnswerCharge{" +
                "answerId=" + answerId +
                ", userId=" + userId +
                ", createTime=" + createTime +
                '}';
    }
}
