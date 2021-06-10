package com.ayue.sp.db.po;

import java.util.Date;

public class SignIn {
    private Integer id;

    private Integer userId;

    private Integer day;

    private Integer energyBean;

    private Date createTime;

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

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getEnergyBean() {
        return energyBean;
    }

    public void setEnergyBean(Integer energyBean) {
        this.energyBean = energyBean;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
