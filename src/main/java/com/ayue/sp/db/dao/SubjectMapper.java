package com.ayue.sp.db.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ayue.sp.db.po.Subject;
import com.ayue.sp.db.po.SubjectExample;

@Repository
public interface SubjectMapper {

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        long countByExample(SubjectExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int deleteByExample(SubjectExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int deleteByPrimaryKey(Integer id);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int insert(Subject record);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int insertSelective(Subject record);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        List<Subject> selectByExample(SubjectExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        Subject selectByPrimaryKey(Integer id);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int updateByExampleSelective(@Param("record") Subject record, @Param("example") SubjectExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int updateByExample(@Param("record") Subject record, @Param("example") SubjectExample example);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int updateByPrimaryKeySelective(Subject record);

        /**
         * This method was generated by MyBatis Generator. This method corresponds to the database table subject
         * @mbg.generated  Thu Oct 08 11:50:26 CST 2020
         */
        int updateByPrimaryKey(Subject record);
        void updateSubject(Integer subject);
        void updateSubjects(Integer subject);
}