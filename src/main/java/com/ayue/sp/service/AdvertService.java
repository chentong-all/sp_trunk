package com.ayue.sp.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.dao.AdvertRecordMapper;
import com.ayue.sp.db.po.AdvertRecord;
import com.ayue.sp.db.po.AdvertRecordExample;
import com.ayue.sp.tools.idgenerator.IdGeneratorTools;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 2020年10月9日
 *
 * @author ayue
 */
@Service
public class AdvertService {
        @Autowired
        private AdvertRecordMapper advertRecordMapper;
        @Autowired
        private IdGeneratorTools idGeneratorTools;

        public AdvertRecord addAdvertService(String pictureUrl, long startTime, long endTime, String comment,int type) {
                AdvertRecord advertRecord = new AdvertRecord();
                advertRecord.setId(idGeneratorTools.getAdvertRecordId());
                advertRecord.setComment(comment);
                advertRecord.setEndTime(new Date(endTime));
                advertRecord.setPictureUrl(pictureUrl);
                advertRecord.setStartTime(new Date(startTime));
                advertRecord.setType(type);
                advertRecordMapper.insert(advertRecord);
                return advertRecord;
        }

        public List<AdvertRecord> getAdvertRecords(int page,int type) {
                AdvertRecordExample example = new AdvertRecordExample();
                example.createCriteria().andTypeEqualTo(type);
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<AdvertRecord>(advertRecordMapper.selectByExample(example)).getList();
        }

        public void delAdvertRecord(int id) {
                advertRecordMapper.deleteByPrimaryKey(id);
        }

        public void updateAdvertRecord(int id, String pictureUrl, long startTime, long endTime, String comment,int type) {
                AdvertRecord advertRecord = new AdvertRecord();
                advertRecord.setId(id);
                advertRecord.setComment(comment);
                advertRecord.setEndTime(new Date(endTime));
                advertRecord.setPictureUrl(pictureUrl);
                advertRecord.setStartTime(new Date(startTime));
                advertRecord.setType(type);
                advertRecordMapper.updateByPrimaryKeySelective(advertRecord);
        }

        public List<AdvertRecord> getAdvertRecords(String comment) {
                AdvertRecordExample example = new AdvertRecordExample();
                example.or().andCommentLike("%" + comment + "%");
                return advertRecordMapper.selectByExample(example);
        }

}
