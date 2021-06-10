package com.ayue.sp.db.dao;

import com.ayue.sp.db.po.Answer;
import com.ayue.sp.db.po.AnswerCharge;
import com.ayue.sp.db.po.AnswerExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerChargeMapper {


        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table answer_charge
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        int insert(AnswerCharge record);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table answer_charge
         * @mbg.generated  Sat Sep 12 20:14:38 CST 2020
         */
        AnswerCharge selectByPrimaryKey(@Param("answerId") int answerId, @Param("userId") int userId);

       }