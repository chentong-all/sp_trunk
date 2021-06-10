package com.ayue.sp.service;

import com.ayue.sp.db.cache.dao.UserLoginZsetDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 2020年10月30日
 *
 * @author ayue
 */
@Service
public class LoginService {

    @Autowired
    private UserLoginZsetDao userLoginZsetDao;

    public void addUserToken(String userName,String password){
        userLoginZsetDao.addUserToken(userName,password);
    }

}
