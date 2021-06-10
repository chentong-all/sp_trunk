package com.ayue.sp.service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ayue.sp.db.dao.CityMapper;
import com.ayue.sp.db.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.dao.SubjectMapper;
import com.ayue.sp.db.dao.User2subjectMapper;
import com.ayue.sp.tools.idgenerator.IdGeneratorTools;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 2020年9月1日
 *
 * @author ayue
 */
@Service
public class SubjectService {
        @Autowired
        private SubjectMapper subjectMapper;
        @Autowired
        private User2subjectMapper user2subjectMapper;
        @Autowired
        private IdGeneratorTools idGeneratorTools;
        @Autowired
        private CityMapper cityMapper;

        public Subject createSubject(byte type, String name, String avatar, String description, int userId) {
                Subject subject = new Subject();
                subject.setId(idGeneratorTools.getSubjectId());
                subject.setType(type);
                subject.setName(name);
                subject.setAvatar(avatar);
                subject.setDescription(description);
                subject.setIsSystem(false);
                subject.setAdminiUserId(userId);
                subject.setMemberCount(1);
                subject.setStatus(IConstants.SUBJECT_STATUS_0);
                subjectMapper.insert(subject);
                return subject;
        }

        public Subject createSystemSubject(byte type, String name, String avatar, String description) {
                Subject subject = new Subject();
                subject.setId(idGeneratorTools.getSubjectId());
                subject.setType(type);
                subject.setName(name);
                subject.setAvatar(avatar);
                subject.setDescription(description);
                subject.setIsSystem(true);
                subject.setAdminiUserId(-1);
                subject.setMemberCount(0);
                subject.setStatus(IConstants.SUBJECT_STATUS_0);
                subjectMapper.insert(subject);
                return subject;
        }

        public Subject updateSubjectInfo(String name, String avatar, String description, int subjectId) {
                Subject subject = new Subject();
                subject.setId(subjectId);
                if (!name.equals("")) {
                        subject.setName(name);
                }
                if (!avatar.equals("")) {
                        subject.setAvatar(avatar);
                }
                if (!description.equals("")) {
                        subject.setDescription(description);
                }
                subjectMapper.updateByPrimaryKeySelective(subject);
                return subject;
        }

        public void addSubjectMember(int subjectId, int userId) {
                User2subject user2subject = new User2subject();
                user2subject.setUserId(userId);
                user2subject.setSubjectId(subjectId);
                user2subjectMapper.insert(user2subject);
        }
        public void updateSubject(int subjectId){
                subjectMapper.updateSubject(subjectId);
        }
        public void updateSubjects(int subjectId){
                subjectMapper.updateSubjects(subjectId);
        }
        public void delSubjectMember(int subjectId, int userId){
                user2subjectMapper.delSubjectMember(subjectId,userId);
        }

        public Subject getSubject(int subjectId) {
                return subjectMapper.selectByPrimaryKey(subjectId);
        }

        public List<Subject> getSubjects(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds)){
                        return Collections.emptyList();}
                SubjectExample example = new SubjectExample();
                example.createCriteria().andIdIn(subjectIds);
                return subjectMapper.selectByExample(example);
        }

        public List<User2subject> getUser2subjectsBySubjectId(int subjectId, int userPage) {
                User2subjectExample example = new User2subjectExample();
                example.createCriteria().andSubjectIdEqualTo(subjectId);
                PageHelper.startPage(userPage, IConstants.PAGE_SIZE);
                return new PageInfo<User2subject>(user2subjectMapper.selectByExample(example)).getList();
        }

        public List<User2subject> getUser2subjectsBySubjectIds(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds))
                        return Collections.emptyList();
                User2subjectExample example = new User2subjectExample();
                example.createCriteria().andSubjectIdIn(subjectIds);
                return user2subjectMapper.selectByExample(example);
        }

        public Map<Integer, List<User2subject>> getUser2subjectMapBySubjectIds(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds))
                        return Collections.emptyMap();
                List<User2subject> user2subjects = this.getUser2subjectsBySubjectIds(subjectIds);
                if (Formula.isEmptyCollection(user2subjects))
                        return Collections.emptyMap();
                return Formula.listGrouping(user2subjects, u -> u.getSubjectId());
        }

        public List<User2subject> getUser2subjectsByUserId(int userId) {
                User2subjectExample example = new User2subjectExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return user2subjectMapper.selectByExample(example);
        }

        public Map<Integer, User2subject> getUser2subjectMapByUserId(int userId) {
                List<User2subject> user2subjects = this.getUser2subjectsByUserId(userId);
                if (Formula.isEmptyCollection(user2subjects)) {
                        return Collections.emptyMap();
                }
                return Formula.list2map(user2subjects, u -> u.getSubjectId());
        }

        public List<Subject> getSubjectByPage() {
                Byte status=0;
                SubjectExample example = new SubjectExample();
                example.createCriteria().andStatusEqualTo(status);
                return subjectMapper.selectByExample(example);
        }
        public List<Subject> getSubjectInfo(byte type) {
                Byte status=0;
                SubjectExample example = new SubjectExample();
                example.createCriteria().andTypeEqualTo(type).andStatusEqualTo(status);
                example.setOrderByClause("member_count desc");
                return new PageInfo<Subject>(subjectMapper.selectByExample(example)).getList();
        }
        public List<Subject> getSubjectType(byte type) {
                Byte status=0;
                SubjectExample example = new SubjectExample();
                example.createCriteria().andTypeEqualTo(type).andStatusEqualTo(status).andAdminiUserIdEqualTo(-1);
                example.setOrderByClause("create_time desc");
                return new PageInfo<Subject>(subjectMapper.selectByExample(example)).getList();
        }

        public User2subject getUser2subject(int userId, int subjectId) {
                return user2subjectMapper.selectByPrimaryKey(userId, subjectId);
        }

        public List<Subject> getSubjectByUserId(int userId, int page) {
                SubjectExample example = new SubjectExample();
                example.createCriteria().andAdminiUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Subject>(subjectMapper.selectByExample(example)).getList();
        }

        public List<Subject> getSubjectByUserId(int userId) {
                SubjectExample example = new SubjectExample();
                example.createCriteria().andAdminiUserIdEqualTo(userId);
                return subjectMapper.selectByExample(example);
        }

        public void closeSubject(int subjectId) {
                Subject record = new Subject();
                record.setId(subjectId);
                record.setStatus(IConstants.USER_STATUS_1);
                subjectMapper.updateByPrimaryKeySelective(record);
        }

        public void openSubject(int subjectId) {
                Subject record = new Subject();
                record.setId(subjectId);
                record.setStatus(IConstants.USER_STATUS_0);
                subjectMapper.updateByPrimaryKeySelective(record);
        }

        public List<Subject> getSubjectByName(String name) {
                SubjectExample example = new SubjectExample();
                example.or().andNameLike("%" + name + "%");
                List<Subject> subjects = subjectMapper.selectByExample(example);
                if (Formula.isEmptyCollection(subjects))
                        return Collections.emptyList();
                return subjects;
        }

        public int getSubjectCount() {
                return (int) subjectMapper.countByExample(null);
        }

        public int getTodaySubjectCount() {
                SubjectExample example = new SubjectExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                return (int) subjectMapper.countByExample(example);
        }

        public List<Subject> getSubjects(int page) {
                SubjectExample example = new SubjectExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Subject>(subjectMapper.selectByExample(example)).getList();
        }
        public List<Subject> getSubjects(int page,int isSystem) {
                SubjectExample example = new SubjectExample();
                example.createCriteria().andIsSystemEqualTo(isSystem== 0 ?false:true);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Subject>(subjectMapper.selectByExample(example)).getList();
        }
        public List<City> getCityByRely(){
                return cityMapper.selectByPrimary();
        }
}
