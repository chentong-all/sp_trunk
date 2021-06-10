package com.ayue.sp.tools.task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ayue.sp.db.po.TeacherRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.cache.dao.RankZSetCDao;
import com.ayue.sp.db.po.User;
import com.ayue.sp.service.RankService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.online.OnlineUser;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年8月24日
 *
 * @author ayue
 */
@Repository
public class TimerTaskHandler implements IMinuteTaskExecuter, IDayStartTaskExecuter, IWeekStartTaskExecuter {
        private Logger logger = Logger.getLogger(this.getClass());

        @Autowired
        private OnlineUserTool onlineUserTool;
        @Autowired
        private UserService userService;
        @Autowired
        private RankService rankService;
        @Autowired
        private RankZSetCDao rankZSetCDao;

        @Override
        public void minuteTaskExecute(int times) {
                logger.info("minite task working");
                Map<Integer, OnlineUser> map = onlineUserTool.getAllOnlineUserMap();
                if (!Formula.isEmptyMap(map)) {
                        Map<Integer, OnlineUser> onlineUserMap = new HashMap<Integer, OnlineUser>(map);
                        long now = System.currentTimeMillis();
                        for (OnlineUser onlineUser : onlineUserMap.values()) {
                                if ((now - onlineUser.getLastRequestTime()) > IConstants.OFF_LINE_TIME_MAX) {
                                        onlineUserTool.removeOnlineUser(onlineUser.getUserId());
                                        userService.updateUserLoginDuration(onlineUser.getUserId());
                                }
                        }
                }
        }

        @Override
        public void dayStartTaskExecute(int times) {
                userService.resetDayTicket();
                rankZSetCDao.resetDayRank();
                userService.updateRankEnergy();


        }

        @Override
        public void weekStartExecute(int times) {
                this.updateUserHistoryRank();
                userService.resetWeekTicket();
                rankZSetCDao.resetWeekRank();
        }

        private void updateUserHistoryRank() {
                List<User> users = userService.getAllUserByTicket();
                int rank=users.size();
                for (User user : users) {
                        if (user.getHistoryRank() == null) {
                                user.setHistoryRank(rank + " ");
                        } else {
                                user.setHistoryRank(user.getHistoryRank() + "," + rank);
                        }
                        if (user.getHistoryTicket() == null) {
                                user.setHistoryTicket(user.getWeekTicket() + " ");
                        } else {
                                user.setHistoryTicket(user.getHistoryTicket() + "," + user.getWeekTicket());
                        }
                        rank--;
                }
                userService.batchUpdateHistory(users);
        }

}
