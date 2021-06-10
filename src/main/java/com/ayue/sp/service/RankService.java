package com.ayue.sp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayue.sp.db.cache.dao.RankZSetCDao;
import com.ayue.sp.db.dao.UserMapper;

/**
 * 2020年9月2日
 *
 * @author ayue
 */
@Service
public class RankService {
        @Autowired
        private UserMapper userMapper;
        @Autowired
        private RankZSetCDao rankZSetCDao;

        public List<Integer> getAllRankUserIds(int page) {
                return rankZSetCDao.getAllRanks(page);
        }

        public List<Integer> getWeekRankUserIds(int page) {
                return rankZSetCDao.getWeekRanks(page);
        }

        public List<Integer> getDayRankUserIds(int page) {
                return rankZSetCDao.getDayRanks(page);
        }

        public int getUserAllRank(int userId) {
                return rankZSetCDao.getUserAllRank(userId);
        }

        public int getUserWeekRank(int userId) {
                return rankZSetCDao.getUserWeekRank(userId);
        }

        public int getUserDayRank(int userId) {
                return rankZSetCDao.getUserDayRank(userId);
        }
        public int getEnergyBeanRank(int userId) {
                return rankZSetCDao.getEnergyBean(userId);
        }

        public List<Integer> getDayRankCount(){
                return rankZSetCDao.getDayRanks();
        }
        public List<Integer> getWeekRankCount(){
                return rankZSetCDao.getWeekRanks();
        }
        public List<Integer> getAllRankCount(){
                return rankZSetCDao.getAllRanks();
        }
}
