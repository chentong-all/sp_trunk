package com.ayue.sp.service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.ayue.sp.db.dao.*;
import com.ayue.sp.db.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.cache.dao.RankZSetCDao;
import com.ayue.sp.tools.idgenerator.IdGeneratorTools;
import com.ayue.sp.tools.lock.LockTools;
import com.ayue.sp.tools.lock.lockObject.UserLockKey;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 2020年8月20日
 *
 * @author ayue
 */
@Service
public class UserService {
        @Autowired
        private UserMapper userMapper;
        @Autowired
        private UserAfterMapper userAfterMapper;
        @Autowired
        private LockTools lockTools;
        @Autowired
        private IdGeneratorTools idGeneratorTools;
        @Autowired
        private UserAttentionMapper userAttentionMapper;
        @Autowired
        private RankZSetCDao rankZSetCDao;
        @Autowired
        private UserTipoffsMapper userTipoffsMapper;
        @Autowired
        private UserVoteMapper userVoteMapper;
        @Autowired
        private User2visitedMapper user2visitedMapper;
        @Autowired
        private UserLoginRecordMapper userLoginRecordMapper;
        @Autowired
        private EnergyBeanMapper energyBeanMapper;
        @Autowired
        private TeacherMapper teacherMapper;
        @Autowired
        private TeacherRecordMapper teacherRecordMapper;

        public void lockUser(int userId) {
                UserLockKey lockKey = new UserLockKey(userId);
                Lock lock = lockTools.getLock(lockKey);
                lock.lock();
        }

        public void unlockUser(int userId) {
                UserLockKey lockKey = new UserLockKey(userId);
                Lock lock = lockTools.getLock(lockKey);
                lock.unlock();
        }

        public void lockUsers(List<Integer> userIds) {
                Collections.sort(userIds);
                for (Integer userId : userIds) {
                        UserLockKey lockKey = new UserLockKey(userId);
                        Lock lock = lockTools.getLock(lockKey);
                        lock.lock();
                }
        }

        public void unlockUsers(List<Integer> userIds) {
                Collections.sort(userIds);
                for (Integer userId : userIds) {
                        UserLockKey lockKey = new UserLockKey(userId);
                        Lock lock = lockTools.getLock(lockKey);
                        lock.unlock();
                }
        }

        public User getUser(String openId) {
                UserExample example = new UserExample();
                example.createCriteria().andOpenidEqualTo(openId);
                List<User> users = userMapper.selectByExample(example);
                if (!Formula.isEmptyCollection(users)) {
                        return users.get(0);
                }
                return null;
        }

        public List<User> getAllUser() {
                return userMapper.selectByExample(null);
        }
        public List<User> getAllUserByTicket() {
                UserExample userExample = new UserExample();
                userExample.setOrderByClause("week_ticket");
                return userMapper.selectByExample(userExample);
        }

        public User getUser(int userId) {
                return userMapper.selectByPrimaryKey(userId);
        }
        public Teacher getTeacher(int userId) {
                return teacherMapper.selectByPrimaryKey(userId);
        }
        public List<Teacher> getTeacher() {
                return teacherMapper.selectByPrimaryKeyAll();
        }
        public void addTeacher(int userId, int ticket, int dayTicket, int day) {
                Teacher teacher = new Teacher();
                teacher.setUserId(userId);
                teacher.setTicket(ticket);
                teacher.setDayTicket(dayTicket);
                teacher.setDay(day);
                teacherMapper.insert(teacher);
        }
        public void addTeacherRecord(int userId, int ticket, int day) {
                TeacherRecord teacherRecord = new TeacherRecord();
                teacherRecord.setTeacherId(userId);
                teacherRecord.setTicket(ticket);
                teacherRecord.setDay(day);
                teacherRecord.setIsUpdateTicket(1);
                teacherRecordMapper.insertSelective(teacherRecord);
        }
        public TeacherRecord addTeacherRecord(int userId, int targetUserId, int ticket, int day) {
                TeacherRecord teacherRecord = new TeacherRecord();
                teacherRecord.setUserId(userId);
                teacherRecord.setTeacherId(targetUserId);
                teacherRecord.setTicket(ticket);
                teacherRecord.setDay(day);
                teacherRecordMapper.insertSelective(teacherRecord);
            return teacherRecordMapper.selectByPrimaryKey(teacherRecord.getId());
        }

        public void addTeacherRecord(int userId, int targetUserId, String acceptContent) {
                TeacherRecord teacherRecord = new TeacherRecord();
                teacherRecord.setUserId(userId);
                teacherRecord.setTeacherId(targetUserId);
                teacherRecord.setAcceptContent(acceptContent);
                teacherRecordMapper.insertSelective(teacherRecord);
        }
        public TeacherRecord getTeacherRecord(int userId, int targetUserId, Date createTime){
            return teacherRecordMapper.getTeacherRecord(userId,targetUserId,createTime);
        }
        public TeacherRecord getTeacherRecords(int userId, int targetUserId, Date updateTime){
            return teacherRecordMapper.getTeacherRecords(userId,targetUserId,updateTime);
        }
        public List<TeacherRecord> getTeacherRecord(List<Integer> users,int userId){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andTeacherIdIn(users).andIsAcceptEqualTo(1).andUserIdEqualTo(userId);
                List<TeacherRecord> teacherRecords = teacherRecordMapper.selectByExample(example);
                long dateTime=System.currentTimeMillis();
                List<TeacherRecord> teacherLists = new ArrayList<>();
                for (TeacherRecord teacher:teacherRecords) {
                        long time = teacher.getUpdateTime().getTime();
                        int day=(int)(dateTime-time)/86400000;
                        if (day<1){
                                teacherLists.add(teacher);
                        }
                }
                return teacherLists;
        }
        public List<TeacherRecord> getTeacherRecordByIds(List<Integer> users,int userId){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andTeacherIdIn(users);
                List<TeacherRecord> teacherRecordList = teacherRecordMapper.selectByExample(example);
                TeacherRecordExample example2 = new TeacherRecordExample();
                example2.createCriteria().andTeacherIdEqualTo(userId);
                List<TeacherRecord> teacherRecordList2 = teacherRecordMapper.selectByExample(example2);
                TeacherRecordExample examples = new TeacherRecordExample();
                examples.createCriteria().andUserIdIn(users);
                List<TeacherRecord> teacherRecordLists = teacherRecordMapper.selectByExample(examples);
                TeacherRecordExample examples2 = new TeacherRecordExample();
                examples2.createCriteria().andUserIdEqualTo(userId);
                List<TeacherRecord> teacherRecordLists2 = teacherRecordMapper.selectByExample(examples2);
                List<TeacherRecord> lists = new ArrayList<>();
                lists.addAll(teacherRecordList);
                lists.addAll(teacherRecordList2);
                lists.addAll(teacherRecordLists);
                lists.addAll(teacherRecordLists2);
            return lists;
        }
        public List<TeacherRecord> getTeacherRecordById(int targetUserId,int userId){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andTeacherIdEqualTo(targetUserId).andUserIdEqualTo(userId);
                List<TeacherRecord> teacherRecordList = teacherRecordMapper.selectByExample(example);
                TeacherRecordExample examples = new TeacherRecordExample();
                examples.createCriteria().andUserIdEqualTo(targetUserId).andTeacherIdEqualTo(userId);
                List<TeacherRecord> teacherRecordLists = teacherRecordMapper.selectByExample(examples);
                List<TeacherRecord> lists = new ArrayList<>();
                for (TeacherRecord teacher:teacherRecordList) {
                        if (teacher.getTeacherContent()!=null){
                                lists.add(teacher);
                        }
                }
                for (TeacherRecord teacher:teacherRecordLists) {
                        if (teacher.getTeacherContent()!=null){
                                lists.add(teacher);
                        }
                }
            return lists;
        }

        public List<TeacherRecord> getTeacherRecord(int userId,List<Integer> users){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andUserIdIn(users).andTeacherIdEqualTo(userId);
                List<TeacherRecord> teacherRecords = teacherRecordMapper.selectByExample(example);
                TeacherRecordExample examples = new TeacherRecordExample();
                examples.createCriteria().andTeacherIdIn(users).andUserIdEqualTo(userId);
                List<TeacherRecord> teacherRecord = teacherRecordMapper.selectByExample(examples);
                long dateTime=System.currentTimeMillis();
                List<TeacherRecord> teacherLists = new ArrayList<>();
                for (TeacherRecord teacher:teacherRecords) {
                        if (teacher.getUpdateTime()!=null){
                                long time = teacher.getUpdateTime().getTime();
                                int day=(int)(dateTime-time)/86400000;
                                if (day < teacher.getDay()){
                                        teacherLists.add(teacher);
                                }
                        }
                }
                for (TeacherRecord teacher:teacherRecord) {
                        if (teacher.getUpdateTime()!=null) {
                                long time = teacher.getUpdateTime().getTime();
                                int day = (int) (dateTime - time) / 86400000;
                                if (day < teacher.getDay()) {
                                        teacherLists.add(teacher);
                                }
                        }
                }
                return teacherLists;
        }
        public List<TeacherRecord> getTeacherRecordByList(int userId,List<Integer> users){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andUserIdIn(users).andTeacherIdEqualTo(userId);
                List<TeacherRecord> teacherRecords = teacherRecordMapper.selectByExample(example);
                List<TeacherRecord> teacherLists = new ArrayList<>();
                TeacherRecordExample exampleUser = new TeacherRecordExample();
                exampleUser.createCriteria().andTeacherIdIn(users).andUserIdEqualTo(userId);
                List<TeacherRecord> teacherRecord = teacherRecordMapper.selectByExample(exampleUser);
                teacherLists.addAll(teacherRecord);
                teacherLists.addAll(teacherRecords);
                return teacherLists;
        }

        public List<TeacherRecord> getTeacherRecord(){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andIsChooseEqualTo(1).andUpdateTimeIsNotNull();
                List<TeacherRecord> teacherRecords = teacherRecordMapper.selectByExample(example);
                List<TeacherRecord> teacherLists = new ArrayList<>();
                long dateTime=System.currentTimeMillis();
                    for (TeacherRecord t:teacherRecords) {
                        long time = t.getCreateTime().getTime();
                        int day=(int)(dateTime-time)/86400000;
                        if (day<4){
                            teacherLists.add(t);
                        }
                    }
                return teacherLists;
        }
        public List<TeacherRecord> getTeacherRecordByTime(int userId){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andTeacherIdEqualTo(userId).andUpdateTimeIsNotNull();
                List<TeacherRecord> teacherRecords = teacherRecordMapper.selectByExample(example);
                List<TeacherRecord> teacherLists = new ArrayList<>();
                TeacherRecordExample exampleUser = new TeacherRecordExample();
                exampleUser.createCriteria().andUserIdEqualTo(userId).andUpdateTimeIsNotNull();
                List<TeacherRecord> teacherRecord = teacherRecordMapper.selectByExample(exampleUser);
                teacherLists.addAll(teacherRecord);
                teacherLists.addAll(teacherRecords);
                return teacherLists;
        }
        public List<TeacherRecord> getTeacherRecordByTime(int userId, int targetUserId){
                TeacherRecordExample example = new TeacherRecordExample();
                example.createCriteria().andTeacherIdEqualTo(targetUserId).andUserIdEqualTo(userId).andUpdateTimeIsNotNull();
                return teacherRecordMapper.selectByExample(example);
        }
        public void ticketPrice(Teacher teacher, int ticket, int dayTicket, int day) {
                teacher.setTicket(ticket);
                teacher.setDayTicket(dayTicket);
                teacher.setDay(day);
                teacherMapper.updateByPrimaryKeySelective(teacher);
        }
        public void updateTeacher(Teacher teacher) {
                teacherMapper.updateByPrimaryKeySelective(teacher);
        }
        public void updateTeacherRecore(TeacherRecord teacherRecord){
            teacherRecordMapper.updateByPrimaryKeySelective(teacherRecord);
        }
        public void updateTeacherRecords(TeacherRecord teacherRecord){
            teacherRecordMapper.updateTeacherRecords(teacherRecord);
        }
        public void updateTeacherRecore(List<TeacherRecord> teacherRecord){
            teacherRecordMapper.updateTeacherRecord(teacherRecord);
        }
        public Integer getUserByEnergyBean(int userId) {
                return userMapper.selectByEnergyBean(userId);
        }

        public List<Integer> getUserLables(String labels){
                return userMapper.getUserLables(labels);
        }
        public List<Integer> getUserLablesByLables(String labels){
                return userMapper.getUserLablesByLables(labels);
        }

        public void updateUser(User user) {
                userMapper.updateByPrimaryKey(user);
        }

        public User initUser(String openId, String name, String avatar, String city) {
                User user = new User();
                user.setId(idGeneratorTools.getUserId());
                user.setOpenid(openId);
                user.setName(name);
                user.setAvatar(avatar);
                user.setCityAddress(city);
                user.setTicket(20);
                userMapper.insertSelective(user);
                return user;
        }

        public List<User> getUsers(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                UserExample example = new UserExample();
                example.createCriteria().andIdIn(userIds);
                return userMapper.selectByExample(example);
        }
        public List<User> getUsersHistory(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                UserExample example = new UserExample();
                example.createCriteria().andIdIn(userIds);
                example.setOrderByClause("week_ticket");
                return userMapper.selectByExample(example);
        }


        public List<User> getSystemUsers() {
                UserExample example = new UserExample();
                example.createCriteria().andIsSystemEqualTo(true);
                return userMapper.selectByExample(example);
        }

        public Map<Integer, User> getUserMap(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyMap();
                }
                List<User> users = this.getUsers(userIds);
                if (Formula.isEmptyCollection(users)) {
                        return Collections.emptyMap();
                }
                return Formula.list2map(users, u -> u.getId());
        }
        public List<User> getUserPage(List<Integer> userIds, int page){
                UserExample example = new UserExample();
                example.createCriteria().andIdIn(userIds);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();

        }
        public List<User> getUserLablesByPage(String Labels){
                UserExample example = new UserExample();
                example.createCriteria().andLabelsEqualTo(Labels);
                example.setOrderByClause("create_time desc");
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();

        }


        public int getUserLevel(User user) {
                return user.getLikeCount() < 5 ? 1 : (int) Math.sqrt(user.getLikeCount() - 1);
        }

        public void batchUpdateHistory(List<User> users) {
                List<User> updateUsers = new LinkedList<User>();
                for (User user : users) {
                        User record = new User();
                        record.setId(user.getId());
                        record.setHistoryRank(user.getHistoryRank());
                        record.setHistoryTicket(user.getHistoryTicket());
                        updateUsers.add(record);
                }
                userMapper.batchUpdateHistory(updateUsers);
        }

        public List<User> searchUsers(String searchContent, int page) {
                String[] searchContents = searchContent.split("");
                UserExample example = new UserExample();
                example.or().andNameLike("%" + searchContent + "%");
                for (String string : searchContents) {
                        example.or().andNameLike("%" + string + "%");
                }
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();
        }

        public List<UserAttention> getUserAttentions(int userId) {
                UserAttentionExample example = new UserAttentionExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userAttentionMapper.selectByExample(example);
        }
        public List<UserAttention> getUserAttentions(int userId,int page) {
                UserAttentionExample example = new UserAttentionExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserAttention>(userAttentionMapper.selectByExample(example)).getList();
        }
        public List<UserAttention> getUserAttentionsInfo(int userId) {
                UserAttentionExample example = new UserAttentionExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                return new PageInfo<UserAttention>(userAttentionMapper.selectByExample(example)).getList();
        }

        public Map<Integer, UserAttention> getUserAttentionMap(int userId) {
                List<UserAttention> userAttentions = this.getUserAttentions(userId);
                if (Formula.isEmptyCollection(userAttentions)) {
                        return Collections.emptyMap();
                }
                return Formula.list2map(userAttentions, u -> u.getAttentionUserId());
        }

        public UserAttention getUserAttention(int userId, int attentionUserId) {
                return userAttentionMapper.selectByPrimaryKey(userId, attentionUserId);
        }

        public void addUserAttention(int userId, int targetUserId) {
                UserAttention userAttention = new UserAttention();
                userAttention.setUserId(userId);
                userAttention.setAttentionUserId(targetUserId);
                userAttentionMapper.insert(userAttention);
        }
        public void deleteByPrimaryKey(int userId, int targetUserId){
                userAttentionMapper.deleteByPrimaryKey(userId,targetUserId);
        }

        public List<UserAttention> getUserFans(int userId) {
                UserAttentionExample example = new UserAttentionExample();
                example.createCriteria().andAttentionUserIdEqualTo(userId);
                return userAttentionMapper.selectByExample(example);
        }

        public void resetDayTicket() {
                userMapper.resetDayTicket();
        }
        public void updateRankEnergy() {
                List<User> updateUsers = new LinkedList<User>();
                UserExample userExample = new UserExample();
                Date date = new Date();
                Calendar calendarStart = Calendar.getInstance();
                calendarStart.setTime(date);
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                calendarStart.set(Calendar.MILLISECOND, 0);
                EnergyBeanExample example = new EnergyBeanExample();
                example.createCriteria().andStatusEqualTo(0).andEnergyBeanEqualTo(0).andCreateTimeGreaterThanOrEqualTo(calendarStart.getTime());
                example.setOrderByClause("energy_bean desc");
                List<EnergyBean> list = new PageInfo<EnergyBean>(energyBeanMapper.selectByRankExample(example)).getList();
                List<Integer> integers = Formula.listDistinct(list, l -> l.getUserId());
                userExample.createCriteria().andIdIn(integers);
                List<User> users = userMapper.selectByExample(userExample);
                Map<Integer, User> integerUserMap = Formula.list2map(users, u -> u.getId());
                int rank=1;
                for (EnergyBean energyBean:list) {
                    User user = integerUserMap.get(energyBean.getUserId());
                    user.setRankEnergyBean(rank);
                    updateUsers.add(user);
                    rank++;
                }
                userMapper.updateRank(updateUsers);
        }

        public void resetWeekTicket() {
                userMapper.resetWeekTicket();
        }

        public void addUserTicket(User user, int addTicket) {
                User record = new User();
                if (user.getAllTicket()+addTicket<=200){
                        record.setLevel(0);
                }
                if (user.getAllTicket()+addTicket>200 && user.getAllTicket()+addTicket<=10000){
                        record.setLevel(1);
                }
                if (user.getAllTicket()+addTicket>10000 && user.getAllTicket()+addTicket<=200000){
                        record.setLevel(2);
                }
                if (user.getAllTicket()+addTicket>200000){
                        record.setLevel(3);
                }
                record.setId(user.getId());
                record.setObtainTicket(user.getObtainTicket() + addTicket);
                record.setAllTicket(user.getAllTicket() + addTicket);
                record.setWeekTicket(user.getWeekTicket() + addTicket);
                record.setDayTicket(user.getDayTicket() + addTicket);
                userMapper.updateByPrimaryKeySelective(record);
                rankZSetCDao.addAllTicket(user.getId(), record.getAllTicket());
                rankZSetCDao.addWeekTicket(user.getId(), record.getWeekTicket());
                rankZSetCDao.addDayTicket(user.getId(), record.getDayTicket());
        }

        public void reduceUserTicket(User user, int reduceTicket) {
                User record = new User();
                record.setId(user.getId());
                record.setTicket(user.getTicket() - reduceTicket);
                record.setDayVoteCount(user.getDayVoteCount()+ reduceTicket);
                userMapper.updateByPrimaryKeySelective(record);
        }

        public int getMaxUserId() {
                return userMapper.getMaxUserId();
        }

        public void updateUserCity(int userId, String city) {
                User record = new User();
                record.setId(userId);
                record.setCityAddress(city);
                userMapper.updateByPrimaryKeySelective(record);
        }
        public void updateUserHistory(User user){
            user.setHistoryRank("0");
            user.setHistoryTicket("0");
            userMapper.updateByPrimaryKey(user);
        }

        public UserTipoffs getUserTipoffs(int targetUserId, int userId) {
                return userTipoffsMapper.selectByPrimaryKey(targetUserId, userId);
        }
        public UserTipoffs getUserTipoff(int targetUserId, int userId, byte type) {
                return userTipoffsMapper.selectByPrimary(targetUserId, userId, type);
        }


        public void addUserTipoffs(int targetUserId, int userId, byte type, String description, String pictureUrl) {
                UserTipoffs userTipoffs = new UserTipoffs();
                userTipoffs.setTargetUserId(targetUserId);
                userTipoffs.setUserId(userId);
                userTipoffs.setType(type);
                if (!Formula.isEmptyString(description)) {
                        userTipoffs.setDescription(description);
                }
                if (!Formula.isEmptyString(pictureUrl)) {
                        userTipoffs.setPictureUrl(pictureUrl);
                }
                userTipoffs.setIsDealed(false);
                userTipoffsMapper.insert(userTipoffs);
        }

        public void addUserVote(int userId, int targetUserId, int ticket, int answerId) {
                UserVote userVote = new UserVote();
                userVote.setId(idGeneratorTools.getUserVoteId());
                userVote.setUserId(userId);
                userVote.setTargetUserId(targetUserId);
                if(answerId!=0){
                        userVote.setAnswerId(answerId);
                }
                userVote.setCount(ticket);
                userVoteMapper.insert(userVote);
        }

        public void addUserVote(int userId, int targetUserId, int ticket) {
                UserVote userVote = new UserVote();
                userVote.setId(idGeneratorTools.getUserVoteId());
                userVote.setUserId(userId);
                userVote.setTargetUserId(targetUserId);
                userVote.setAnswerId(null);
                userVote.setCount(ticket);
                userVoteMapper.insert(userVote);
        }

        public List<UserVote> getUserVote(int userId, Date startTime, Date endTime) {
                UserVoteExample example = new UserVoteExample();
                example.createCriteria().andUserIdEqualTo(userId).andCreateTimeGreaterThanOrEqualTo(startTime).andCreateTimeLessThanOrEqualTo(endTime).andCountNotEqualTo(0);
                return userVoteMapper.selectByExample(example);
        }

        public List<UserVote> getUserVoteByTargetUserId(int targetUserId, Date startTime, Date endTime) {
                UserVoteExample example = new UserVoteExample();
                example.createCriteria().andTargetUserIdEqualTo(targetUserId).andCreateTimeGreaterThanOrEqualTo(startTime).andCreateTimeLessThanOrEqualTo(endTime);
                return userVoteMapper.selectByExample(example);
        }
        public List<EnergyBean> getUserByEnergyBeanByUserId(int userId, Date startTime, Date endTime) {
                EnergyBeanExample example = new EnergyBeanExample();
                example.createCriteria().andUserIdEqualTo(userId).andCreateTimeGreaterThanOrEqualTo(startTime).andCreateTimeLessThanOrEqualTo(endTime).andTypeEqualTo(1);
                return energyBeanMapper.selectByExample(example);
        }

        public void addUserForwardCount(int userId) {
                userMapper.addForwardCount(userId);
        }

        public List<User2visited> getUser2visited(int questionId, int targetUserId){
                return user2visitedMapper.getUser2visited(questionId,targetUserId);
        }
        public User2visited getUser2visitedByQuestion(int questionId, int targetUserId, int userId, int type){
                return user2visitedMapper.getUser2visitedByQuestion(questionId,targetUserId,userId, type);
        }
        public void addUser2visited(int questionId, int userId, int targetUserId, int type) {
                User2visited user2visited = new User2visited();
                user2visited.setQuestionId(questionId);
                user2visited.setUserId(userId);
                user2visited.setTargetUserId(targetUserId);
                user2visited.setIsFinish(true);
                user2visited.setType(type);
                user2visitedMapper.insert(user2visited);
        }

        public void addUser2visiteds(int questionId, int userId, List<Integer> targetUserIds, int type) {
                List<User2visited> user2visiteds = new LinkedList<User2visited>();
                for (Integer targetUserId : targetUserIds) {
                        User2visited user2visited = new User2visited();
                        user2visited.setQuestionId(questionId);
                        user2visited.setUserId(userId);
                        user2visited.setTargetUserId(targetUserId);
                        user2visited.setType(type);
                        user2visiteds.add(user2visited);
                }
                if (user2visiteds.size()>0) {
                        user2visitedMapper.batchInsert(user2visiteds);
                }
        }

        public void updateUser2visitedFinish(int questionId, int targetUserId) {
                User2visited record = new User2visited();
                record.setQuestionId(questionId);
                record.setTargetUserId(targetUserId);
                record.setIsFinish(true);
                user2visitedMapper.updateByPrimaryKeySelective(record);
        }

        public List<User2visited> getUser2visiteds(int userId) {
                User2visitedExample example = new User2visitedExample();
                example.createCriteria().andUserIdEqualTo(userId).andIsFinishEqualTo(false);
                return user2visitedMapper.selectByExample(example);
        }

        public List<User2visited> getAllUser2visiteds(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)) {
                        return Collections.emptyList();
                }
                User2visitedExample example = new User2visitedExample();
                example.createCriteria().andQuestionIdIn(questionIds);
                return user2visitedMapper.selectByExample(example);
        }

        public Map<Integer, List<User2visited>> getAllUser2visitedMap(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)) {
                        return Collections.emptyMap();
                }
                List<User2visited> user2visiteds = this.getAllUser2visiteds(questionIds);
                if (Formula.isEmptyCollection(user2visiteds)) {
                        return Collections.emptyMap();
                }
                return Formula.listGrouping(user2visiteds, u -> u.getQuestionId());
        }

        public Map<Integer, User2visited> getUser2visitedMap(int userId) {
                List<User2visited> user2visiteds = this.getUser2visiteds(userId);
                if (Formula.isEmptyCollection(user2visiteds)) {
                        return Collections.emptyMap();
                }
                return Formula.list2map(user2visiteds, u -> u.getTargetUserId());
        }

        public List<User> getUserList(int page) {
                UserExample example = new UserExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();
        }
        public List<User> getUserListByAll(int page) {
                UserExample example = new UserExample();
                example.setOrderByClause("all_ticket desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();
        }
        public List<User> getUserListByWeek(int page) {
                UserExample example = new UserExample();
                example.setOrderByClause("week_ticket desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();
        }
        public List<User> getUserListByTicket(int page) {
                UserExample example = new UserExample();
                example.setOrderByClause("day_ticket desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();
        }
        public List<User> getUserLists(int page) {
                UserExample example = new UserExample();
                example.createCriteria().andTeacherEqualTo(1);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();
        }
        public List<User> getUserLists() {
                UserExample example = new UserExample();
                example.createCriteria().andTeacherEqualTo(1);
                example.setOrderByClause("create_time desc");
                return userMapper.selectByExample(example);
        }
        public List<User> getUserTeacherList(int page) {
                UserExample example = new UserExample();
                example.setOrderByClause("create_time desc");
                example.createCriteria().andIdentityTo(1);
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<User>(userMapper.selectByExample(example)).getList();
        }

        public List<User> getUserByName(String name) {
                UserExample example = new UserExample();
                example.or().andNameLike("%" + name + "%");
                List<User> users = userMapper.selectByExample(example);
                if (Formula.isEmptyCollection(users)) {
                        return Collections.emptyList();
                }
                return users;
        }

        public void closeUser(int userId) {
                User record = new User();
                record.setId(userId);
                record.setStatus(IConstants.USER_STATUS_1);
                userMapper.updateByPrimaryKeySelective(record);
        }

        public void openUser(int userId) {
                User record = new User();
                record.setId(userId);
                record.setStatus(IConstants.USER_STATUS_0);
                userMapper.updateByPrimaryKeySelective(record);
        }

        public void addUserLoginRecord(int userId) {
                UserLoginRecord userLoginRecord = new UserLoginRecord();
                userLoginRecord.setUserId(userId);
                userLoginRecord.setDuration(0L);
                userLoginRecordMapper.insert(userLoginRecord);
        }

        public void updateUserLoginDuration(int userId) {
                UserLoginRecordExample example = new UserLoginRecordExample();
                example.createCriteria().andUserIdEqualTo(userId);
                List<UserLoginRecord> userLoginRecords = userLoginRecordMapper.selectByExample(example);
                for (UserLoginRecord userLoginRecord : userLoginRecords) {
                        if (userLoginRecord.getDuration() == 0) {
                                userLoginRecord.setDuration(System.currentTimeMillis() - userLoginRecord.getLoginTime().getTime());
                                userLoginRecordMapper.updateByPrimaryKey(userLoginRecord);
                        }
                }
        }

        public List<UserLoginRecord> getUserLoginRecords(int userId, int page) {
                UserLoginRecordExample example = new UserLoginRecordExample();
                example.createCriteria().andUserIdEqualTo(userId);
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserLoginRecord>(userLoginRecordMapper.selectByExample(example)).getList();
        }

        public List<UserLoginRecord> getUserLoginRecords(int userId) {
                UserLoginRecordExample example = new UserLoginRecordExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userLoginRecordMapper.selectByExample(example);
        }

        public List<UserVote> getUserVoteByUserId(int userId) {
                UserVoteExample example = new UserVoteExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userVoteMapper.selectByExample(example);
        }
        public List<UserVote> getUserVoteByUserIds(List<Integer> userIds,int page){
                UserVoteExample example = new UserVoteExample();
                example.or().andUserIdIn(userIds);
                example.or().andTargetUserIdIn(userIds);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserVote>(userVoteMapper.selectByExample(example)).getList();
        }
        public List<UserVote> getUserVoteList(List<Integer> userIds){
                UserVoteExample example = new UserVoteExample();
                example.or().andUserIdIn(userIds);
                example.or().andTargetUserIdIn(userIds);
                example.setOrderByClause("create_time desc");
                return new PageInfo<UserVote>(userVoteMapper.selectByExample(example)).getList();

        }

        public List<UserVote> getUserVotes(int page) {
                UserVoteExample example = new UserVoteExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserVote>(userVoteMapper.selectByExample(example)).getList();
        }
        public Integer getUserVoteCount(){
                return userVoteMapper.getUserVoteCount();
        }

        public List<UserVote> getUserVoteByTargetUserId(int userId) {
                UserVoteExample example = new UserVoteExample();
                example.createCriteria().andTargetUserIdEqualTo(userId);
                return userVoteMapper.selectByExample(example);
        }

        public void addUserQuestionCount(User user) {
                User record = new User();
                record.setId(user.getId());
                record.setQuestionCount(user.getQuestionCount() + 1);
                userMapper.updateByPrimaryKeySelective(record);
        }
        public void updateUserTicket(int dicket,int userId){
                userMapper.updateUserTicket(dicket,userId);
        }

        public void addUserAnswerCount(User user) {
                User record = new User();
                record.setId(user.getId());
                record.setAnswerCount(user.getAnswerCount() + 1);
                userMapper.updateByPrimaryKeySelective(record);
        }

        public void addUserCommentCount(User user) {
                User record = new User();
                record.setId(user.getId());
                record.setCommentCount(user.getCommentCount() + 1);
                userMapper.updateByPrimaryKeySelective(record);
        }

        public void addUserReplyCount(User user) {
                User record = new User();
                record.setId(user.getId());
                record.setReplyCount(user.getReplyCount() + 1);
                userMapper.updateByPrimaryKeySelective(record);
        }

        public List<UserTipoffs> getUserTipoffs(int page) {
                UserTipoffsExample example = new UserTipoffsExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserTipoffs>(userTipoffsMapper.selectByExample(example)).getList();
        }
        public void dealedTipoff(int userId,int targetUserId){
                userTipoffsMapper.dealedTipoff(userId,targetUserId);
        }
        public Integer getUserTipoffsCount(){
                return userTipoffsMapper.getUserTipoffsCount();
        }

        public int getUserCount() {
                return (int) userMapper.countByExample(null);
        }

        public int getTodayUserCount() {
                UserExample example = new UserExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                return (int) userMapper.countByExample(example);
        }
        public int getTodayUserCounts() {
            UserLoginRecordExample userLoginRecordExample = new UserLoginRecordExample();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            userLoginRecordExample.createCriteria().andLoginTimeGreaterThan(calendar.getTime());
            List<UserLoginRecord> userLoginRecords = userLoginRecordMapper.selectByExample(userLoginRecordExample);
            Map<Object, UserLoginRecord> objectUserLoginRecordMap = Formula.list2map(userLoginRecords, u -> u.getUserId());
            return objectUserLoginRecordMap.size();
        }

        public int getTicketCount() {
                List<UserVote> userVotes = userVoteMapper.selectByExample(null);
                return userVotes.size();
        }

        public int getTodayTicketCount() {
                UserVoteExample example = new UserVoteExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                List<UserVote> userVotes = userVoteMapper.selectByExample(example);
                int allTicket = 0;
                for (UserVote userVote : userVotes) {
                        allTicket += userVote.getCount();
                }
                return allTicket;
        }
        public List<User> getUserName(List<Integer> collect){
                return userMapper.getUserName(collect);
        }
        public UserAfter getUserLogin(String userName,String password){
                return userAfterMapper.getUserLogin(userName,password);
        }

}
