package com.ayue.sp.db.po;

import java.util.Date;

public class EnergyBean {

    private Integer id;

    private Integer userId;

    private Integer type;

    private Integer number;

    private Integer energyBean;

    private Date createTime;

    private Integer luckyDrawStatus;

    public Integer getLuckyDrawStatus() {
        return luckyDrawStatus;
    }

    public void setLuckyDrawStatus(Integer luckyDrawStatus) {
        this.luckyDrawStatus = luckyDrawStatus;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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
