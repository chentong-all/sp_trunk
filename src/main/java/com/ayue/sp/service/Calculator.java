package com.ayue.sp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayue.sp.db.po.User;

/**
 * 2020年10月8日
 *
 * @author ayue
 */
@Service
public class Calculator {
        @Autowired
        private UserService userService;
        @Autowired
        private ChatService chatService;

        public void sendSystemNews(String title, String content) {
                new Thread(() -> {
                        List<User> users = userService.getAllUser();
                        for (User user : users) {
                                chatService.addUserNews7(user.getId(), title, content);
                        }
                }).start();
        }
}
