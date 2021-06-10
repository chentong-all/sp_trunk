package com.ayue.sp.tools.idgenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ayue.sp.db.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ayue.sp.db.dao.IdGeneratorRecorderMapper;

/**
 * 2020年8月26日
 *
 * @author ayue
 */
@Repository
public class IdGeneratorTools {
        @Autowired
        private IdGeneratorRecorderMapper idGeneratorRecorderMapper;

        private Map<String, IdGeneratorRecorderVO> idGeneratorRecorderVOMap = new HashMap<>();

        public void init() {
                List<IdGeneratorRecorder> idGeneratorRecorderList = idGeneratorRecorderMapper.selectByExample(null);
                for (IdGeneratorRecorder idGeneratorRecorder : idGeneratorRecorderList) {
                        IdGeneratorRecorderVO idGeneratorRecorderVO = new IdGeneratorRecorderVO();
                        AtomicInteger atomicValue = new AtomicInteger(idGeneratorRecorder.getTargetValue());
                        idGeneratorRecorderVO.setAtomicValue(atomicValue);
                        idGeneratorRecorder.setTargetValue(idGeneratorRecorder.getTargetValue() + idGeneratorRecorder.getStepSize());
                        idGeneratorRecorderVO.setIdGeneratorRecorder(idGeneratorRecorder);
                        idGeneratorRecorderVOMap.put(idGeneratorRecorder.getId(), idGeneratorRecorderVO);
                }
                new Thread(() -> {
                        idGeneratorRecorderMapper.updateTargetValue();
                }).start();
        }

        private int getGenerateId(String className) {
                IdGeneratorRecorderVO idGeneratorRecorderVO = idGeneratorRecorderVOMap.get(className);
                IdGeneratorRecorder idGeneratorRecorder = idGeneratorRecorderVO.getIdGeneratorRecorder();
                AtomicInteger atomicValue = idGeneratorRecorderVO.getAtomicValue();
                int incrValue = atomicValue.getAndIncrement();
                if (incrValue == idGeneratorRecorder.getTargetValue().longValue()) {
                        idGeneratorRecorder.setTargetValue(idGeneratorRecorder.getTargetValue() + idGeneratorRecorder.getStepSize());
                        new Thread(() -> {
                                IdGeneratorRecorderExample example = new IdGeneratorRecorderExample();
                                example.createCriteria().andIdEqualTo(idGeneratorRecorder.getId()).andTargetValueLessThan(idGeneratorRecorder.getTargetValue());
                                idGeneratorRecorderMapper.updateByExample(idGeneratorRecorder, example);
                        }).start();
                }
                return incrValue;
        }

        public int getUserId() {
                return getGenerateId(User.class.getSimpleName());
        }

        public int getAnswerId() {
                return getGenerateId(Answer.class.getSimpleName());
        }

        public int getCommentId() {
                return getGenerateId(Comment.class.getSimpleName());
        }

        public int getQuestionId() {
                return getGenerateId(Question.class.getSimpleName());
        }

        public int getReplyId() {
                return getGenerateId(Reply.class.getSimpleName());
        }

        public int getSubjectId() {
                return getGenerateId(Subject.class.getSimpleName());
        }

        public int getUserCashoutId() {
                return getGenerateId(UserCashout.class.getSimpleName());
        }

        public int getUserConvertId() {
                return getGenerateId(UserConvert.class.getSimpleName());
        }

        public int getUserNewsId() {
                return getGenerateId(UserNews.class.getSimpleName());
        }

        public int getUserRechargeId() {
                return getGenerateId(UserRecharge.class.getSimpleName());
        }

        public int getUserVoteId() {
                return getGenerateId(UserVote.class.getSimpleName());
        }

        public int getUserLoginRecordId() {
                return getGenerateId(UserLoginRecord.class.getSimpleName());
        }

        public int getAdvertRecordId() {
                return getGenerateId(AdvertRecord.class.getSimpleName());
        }

        private class IdGeneratorRecorderVO {
                IdGeneratorRecorder idGeneratorRecorder;
                AtomicInteger atomicValue;

                private IdGeneratorRecorder getIdGeneratorRecorder() {
                        return idGeneratorRecorder;
                }

                private void setIdGeneratorRecorder(IdGeneratorRecorder idGeneratorRecorder) {
                        this.idGeneratorRecorder = idGeneratorRecorder;
                }

                private AtomicInteger getAtomicValue() {
                        return atomicValue;
                }

                private void setAtomicValue(AtomicInteger atomicValue) {
                        this.atomicValue = atomicValue;
                }

                private IdGeneratorRecorderVO() {
                        super();
                }

        }

}
