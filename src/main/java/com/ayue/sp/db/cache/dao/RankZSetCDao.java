package com.ayue.sp.db.cache.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.core.redisCache.dao.AbstractZSetCDao;

/**
 * 2020年9月2日
 *
 * @author ayue
 */
@Repository
public class RankZSetCDao extends AbstractZSetCDao<Integer> {
        private final static String key = "rank";

        public RankZSetCDao() {
                super(Integer.class);
        }

        private String getAllRankKey() {
                return key + ":" + "all";
        }

        private String getWeekRankKey() {
                return key + ":" + "week";
        }

        private String getDayRankKey() {
                return key + ":" + "day";
        }
        private String getEnergyBeanRankKey() {
                return key + ":" + "energyBean";
        }

        public void addAllTicket(int userId, int ticketCount) {
                this.zadd(this.getAllRankKey(), userId, ticketCount);
        }

        public void addWeekTicket(int userId, int ticketCount) {
                this.zadd(this.getWeekRankKey(), userId, ticketCount);
        }

        public void addDayTicket(int userId, int ticketCount) {
                this.zadd(this.getDayRankKey(), userId, ticketCount);
        }
        public void addEnergyBean(int userId, int energyBean) {
                this.zadd(this.getEnergyBeanRankKey(), userId, energyBean);
        }

        public List<Integer> getAllRanks(int page) {
                long start = (page - 1) * IConstants.PAGE_SIZE;
                long end = (page * IConstants.PAGE_SIZE)-1;
                return this.zrangeDesc(this.getAllRankKey(), start, end);
        }

        public List<Integer> getWeekRanks(int page) {
                long start = (page - 1) * IConstants.PAGE_SIZE;
                long end = (page * IConstants.PAGE_SIZE)-1;
                return this.zrangeDesc(this.getWeekRankKey(), start, end);
        }

        public List<Integer> getDayRanks(int page) {
                long start = (page - 1) * IConstants.PAGE_SIZE;
                long end = (page * IConstants.PAGE_SIZE)-1;
                return this.zrangeDesc(this.getDayRankKey(), start, end);
        }
        public List<Integer> getEnergyBeans(int page) {
                long start = (page - 1) * IConstants.PAGE_SIZE;
                long end = (page * IConstants.PAGE_SIZE)-1;
                return this.zrangeDesc(this.getEnergyBeanRankKey(), start, end);
        }

        public List<Integer> getAllRanks() {
                return this.zrangeDesc(this.getAllRankKey(), 0, Long.MAX_VALUE);
        }

        public List<Integer> getWeekRanks() {
                return this.zrangeDesc(this.getWeekRankKey(), 0, Long.MAX_VALUE);
        }

        public List<Integer> getDayRanks() {
                return this.zrangeDesc(this.getDayRankKey(), 0, Long.MAX_VALUE);
        }
        public List<Integer> getEnergyBeans() {
                return this.zrangeDesc(this.getEnergyBeanRankKey(), 0, Long.MAX_VALUE);
        }

        public int getUserAllRank(int userId) {
                int rank = this.getDescRank(this.getAllRankKey(), userId) == null ? -1 : this.getDescRank(this.getAllRankKey(), userId).intValue();
                return rank + 1;
        }

        public int getUserWeekRank(int userId) {
                int rank = this.getDescRank(this.getWeekRankKey(), userId) == null ? -1 : this.getDescRank(this.getWeekRankKey(), userId).intValue();
                return rank + 1;
        }

        public int getUserDayRank(int userId) {
                int rank = this.getDescRank(this.getDayRankKey(), userId) == null ? -1 : this.getDescRank(this.getDayRankKey(), userId).intValue();
                return rank + 1;
        }
        public int getEnergyBean(int userId) {
                int rank = this.getDescRank(this.getEnergyBeanRankKey(), userId) == null ? -1 : this.getDescRank(this.getEnergyBeanRankKey(), userId).intValue();
                return rank + 1;
        }

        public void resetWeekRank() {
                List<Integer> weekRanks = this.zrange(this.getWeekRankKey(), 0, Long.MAX_VALUE);
                if (!Formula.isEmptyCollection(weekRanks)) {
                        this.zremList(this.getWeekRankKey(), weekRanks);
                }
        }

        public void resetDayRank() {
                List<Integer> dayRanks = this.zrange(this.getDayRankKey(), 0, Long.MAX_VALUE);
                if (!Formula.isEmptyCollection(dayRanks)) {
                        this.zremList(this.getDayRankKey(), dayRanks);
                }
        }
        public void resetDayEnergyBean() {
                List<Integer> dayEnergyBean = this.zrange(this.getEnergyBeanRankKey(), 0, Long.MAX_VALUE);
                if (!Formula.isEmptyCollection(dayEnergyBean)) {
                        this.zremList(this.getEnergyBeanRankKey(), dayEnergyBean);
                }
        }

}
