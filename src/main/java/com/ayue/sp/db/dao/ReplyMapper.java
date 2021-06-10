package com.ayue.sp.db.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ayue.sp.db.po.Reply;
import com.ayue.sp.db.po.ReplyExample;

@Repository
public interface ReplyMapper {

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        long countByExample(ReplyExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int deleteByExample(ReplyExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int deleteByPrimaryKey(Integer id);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int insert(Reply record);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int insertSelective(Reply record);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        List<Reply> selectByExample(ReplyExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        Reply selectByPrimaryKey(Integer id);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int updateByExampleSelective(@Param("record") Reply record, @Param("example") ReplyExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int updateByExample(@Param("record") Reply record, @Param("example") ReplyExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int updateByPrimaryKeySelective(Reply record);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table reply
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int updateByPrimaryKey(Reply record);

        void addAgreeCount(@Param(value = "userId")Integer userId,@Param(value = "replyId") Integer replyId);

        void reduceAgreeCount(Integer replyId);

        void updateReplyOnline(@Param(value = "id")Integer replyId);
        void updateReplyOffline(@Param(value = "id")Integer replyId);
        Integer getReplyUserIdCount(Integer userId);
}