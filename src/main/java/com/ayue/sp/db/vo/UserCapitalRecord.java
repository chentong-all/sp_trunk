package com.ayue.sp.db.vo;

import java.util.Date;

import com.ayue.sp.db.po.UserCashout;
import com.ayue.sp.db.po.UserRecharge;

/**
 * 2020年10月7日
 *
 * @author ayue
 */
public class UserCapitalRecord implements Comparable<UserCapitalRecord> {
        private Date date;
        private int moneyCount;
        private byte type;// 0:提现，1：充值

        public UserCapitalRecord(UserCashout userCashout) {
                this.date = userCashout.getCreateTime();
                this.moneyCount = userCashout.getMoneyCount();
                this.type = 0;
        }

        public UserCapitalRecord(UserRecharge userRecharge) {
                this.date = userRecharge.getCreateTime();
                this.moneyCount = userRecharge.getMoneyCount();
                this.type = 1;
        }

        public Date getDate() {
                return date;
        }

        public int getMoneyCount() {
                return moneyCount;
        }

        public byte getType() {
                return type;
        }

        @Override
        public int compareTo(UserCapitalRecord other) {
                if (this.date.getTime() > other.date.getTime())
                        return -1;
                else
                        return 1;
        }
}
