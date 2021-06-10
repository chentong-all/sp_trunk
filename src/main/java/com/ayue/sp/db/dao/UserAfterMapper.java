package com.ayue.sp.db.dao;

import com.ayue.sp.db.po.User;
import com.ayue.sp.db.po.UserAfter;
import com.ayue.sp.db.po.UserExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAfterMapper {


        UserAfter getUserLogin(@Param("name") String userName, @Param("password") String password);
}