package com.ayue.sp.tools;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ayue.sp.tools.idgenerator.IdGeneratorTools;
import com.ayue.sp.tools.lock.LockTools;
import com.ayue.sp.tools.lock.lockObject.AnswerLockKey;
import com.ayue.sp.tools.lock.lockObject.QuestionLockKey;
import com.ayue.sp.tools.lock.lockObject.UserLockKey;

/**
 * 2020年8月26日
 *
 * @author ayue
 */
public class Start {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private IdGeneratorTools idGeneratorTools;
        @Autowired
        private LockTools lockTools;

        public void init() {
                idGeneratorTools.init();
                lockTools.registerLockKey(UserLockKey.class, 1024);
                lockTools.registerLockKey(QuestionLockKey.class, 1024);
                lockTools.registerLockKey(AnswerLockKey.class, 1024);
                logger.info("project start sucess");
        }
}
