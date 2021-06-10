package com.ayue.sp.db.cache.dao;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.core.redisCache.dao.AbstractZSetCDao;
import com.ayue.sp.db.cache.vo.MsgCVO;

/**
 * 2020年8月21日
 *
 * @author ayue
 */
@Repository
public class MsgListZsetCDao extends AbstractZSetCDao<MsgCVO> {

        private final static String key = "chat:msgList";

        public MsgListZsetCDao() {
                super(MsgCVO.class);
        }

        private String getKey(String chatId) {
                return key + ":" + chatId;
        }

        public void addMsg(String chatId, MsgCVO msgCVo) {
                this.zadd(this.getKey(chatId), msgCVo, msgCVo.getSendTime());
        }

        public List<MsgCVO> getMsgs(String chatId, long cursor) {
                return this.zrangeDescByScore(this.getKey(chatId), cursor, Long.MAX_VALUE);
        }

        public MsgCVO getLastMsgs(String chatId) {
                List<MsgCVO> msgCVos = this.zrangeDesc(this.getKey(chatId), 0, 1);
                if (!Formula.isEmptyCollection(msgCVos)) {
                        return msgCVos.get(0);
                }
                return null;
        }

        public List<MsgCVO> getLastMsgs(List<String> chatIds) {
                List<MsgCVO> msgCVos = new LinkedList<MsgCVO>();
                for (String chatId : chatIds) {
                        MsgCVO msgCVo = this.getLastMsgs(chatId);
                        msgCVos.add(msgCVo);
                }
                return msgCVos;
        }

        public List<MsgCVO> clearMsgItem(String chatIdKey) {
                // 根据条数清理
                List<MsgCVO> outCountMsgList = this.zrangeDesc(chatIdKey, IConstants.MSG_MAX_COUNT, Long.MAX_VALUE);
                if (!Formula.isEmptyCollection(outCountMsgList)) {
                        this.zremList(chatIdKey, outCountMsgList);
                }
                // 根据时间清理
                List<MsgCVO> outTimeMsgList = this.zrangeByScore(chatIdKey, 0d, System.currentTimeMillis() - IConstants.MSG_MAX_TIME);
                if (!Formula.isEmptyCollection(outCountMsgList)) {
                        this.zremList(chatIdKey, outCountMsgList);
                }
                outCountMsgList.addAll(outTimeMsgList);
                return outCountMsgList;
        }

        public Set<String> getAllMsgListKeys() {
                return this.keys(MsgListZsetCDao.key + "*");
        }

        public void del(String chatId) {
                this.delete(getKey(chatId));
        }
}
