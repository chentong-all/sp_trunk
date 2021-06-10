package com.ayue.sp.tools.task;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.ayue.sp.core.Formula;

public class TimerTaskManager {

        private Logger logger = Logger.getLogger(getClass());

        private boolean isWorking = true;
        /**
         * 秒级计数器
         */
        private int secondCounter = 1;
        private long lastSecondTaskTime = -1;
        /**
         * 分钟级计数器
         */
        private int minuterCounter = 1;
        private long lastMinuterTaskTime = -1;
        /**
         * 小时级计数器
         */
        private int hourCounter = 1;
        private long lastHourTaskTime = -1;
        /**
         * 秒级任务执行器集合
         */
        @Autowired(required = false)
        private List<ISecondTaskExecuter> secondTaskExecuters;
        /**
         * 分钟级任务执行器集合
         */
        @Autowired(required = false)
        private List<IMinuteTaskExecuter> minuteTaskExecuters;
        /**
         * 小时级任务执行器集合
         */
        @Autowired(required = false)
        private List<IHourTaskExecuter> hourTaskExecuters;
        /**
         * 日级任务执行器集合
         */
        @Autowired(required = false)
        private List<IDayFreeTaskExecuter> dayFreeTaskExecuters;
        /**
         * 每日凌晨执行任务
         */
        @Autowired(required = false)
        private List<IDayStartTaskExecuter> dayStartExecuters;

        /**
         * 周级任务执行器集合
         */
        @Autowired(required = false)
        private List<IWeekFreeTaskExecuter> weekFreeTaskExecuters;
        /**
         * 周级开始凌晨任务执行器集合
         */
        @Autowired(required = false)
        private List<IWeekStartTaskExecuter> weekStartTaskExecuters;

        private Lock secondLock = new ReentrantLock();
        private Lock minuteLock = new ReentrantLock();

        public void secondExecute() {
                if (!isWorking) {
                        return;
                }
                if (Formula.isEmptyCollection(secondTaskExecuters)) {
                        return;
                }
                if (secondLock.tryLock()) {
                        try {
                                long currTime = System.currentTimeMillis();
                                if (lastSecondTaskTime != -1) {
                                        // 间隔不到800毫秒就触发了一次新的秒级任务，不执行
                                        if (currTime - lastSecondTaskTime < 800) {
                                                logger.warn("second task has some problems,because interval last task just" + (currTime - lastSecondTaskTime) + "ms");
                                                return;
                                        }
                                }
                                lastSecondTaskTime = currTime;
                                long now = System.currentTimeMillis();
                                for (ISecondTaskExecuter executer : secondTaskExecuters) {
                                        try {
                                                executer.secondTaskExecute(secondCounter);
                                        } catch (Exception ex) {
                                                logger.error("执行秒级任务报错，错误类为" + executer.getClass().getName(), ex);
                                        }
                                }
                                secondCounter++;
                                if (System.currentTimeMillis() - now > 300) {
                                        logger.warn("警告，秒级任务执行时间超过300毫秒!!!");
                                }
                        } finally {
                                secondLock.unlock();
                        }
                } else {
                        logger.warn("无法获得秒级后台任务锁！！！！！！");
                }
        }

        public void minuteExecute() {
                if (!isWorking) {
                        return;
                }
                if (Formula.isEmptyCollection(minuteTaskExecuters)) {
                        return;
                }
                if (minuteLock.tryLock()) {
                        try {
                                long currTime = System.currentTimeMillis();
                                if (lastMinuterTaskTime != -1) {
                                        // 间隔不到800毫秒就触发了一次新的秒级任务，不执行
                                        if (currTime - lastMinuterTaskTime < 55000) {
                                                logger.warn("分钟级任务触发时间有问题，与上次分钟级任务时间间隔仅为" + ((currTime - lastSecondTaskTime) / 1000) + "秒");
                                                return;
                                        }
                                }
                                lastMinuterTaskTime = currTime;
                                for (IMinuteTaskExecuter executer : minuteTaskExecuters) {
                                        try {
                                                executer.minuteTaskExecute(minuterCounter);
                                        } catch (Exception ex) {
                                                logger.error("执行分钟级任务报错，错误类为" + executer.getClass().getName(), ex);
                                        }
                                }
                                minuterCounter++;
                        } finally {
                                minuteLock.unlock();
                        }
                } else {
                        logger.warn("无法获得分钟级后台任务锁！！！！！！");
                }
        }

        public void hourExecute() {
                if (!isWorking) {
                        return;
                }
                if (Formula.isEmptyCollection(hourTaskExecuters)){
                        return;}
                long currTime = System.currentTimeMillis();
                if (lastHourTaskTime != -1) {
                        // 间隔不到800毫秒就触发了一次新的秒级任务，不执行
                        if (currTime - lastHourTaskTime < 3300000) {
                                logger.warn("小时级任务触发时间有问题，与上次小时级任务时间间隔仅为" + ((currTime - lastSecondTaskTime) / 60000) + "分钟");
                                return;
                        }
                }
                lastHourTaskTime = currTime;
                for (IHourTaskExecuter executer : hourTaskExecuters) {
                        try {
                                long startTime = System.currentTimeMillis();
                                executer.hourTaskExecute(hourCounter);
                                logger.info("TimerTaskManager.hourExecute:" + executer.getClass().getName() + "  执行耗时：" + (System.currentTimeMillis() - startTime));
                        } catch (Exception ex) {
                                logger.error("执行小时级任务报错，错误类为" + executer.getClass().getName(), ex);
                        }
                }
                hourCounter++;
        }

        public void dayFreeExecute() {
                if (!isWorking){
                        return;}
                if (Formula.isEmptyCollection(dayFreeTaskExecuters)){
                        return;}
                for (IDayFreeTaskExecuter executer : dayFreeTaskExecuters) {
                        try {
                                long startTime = System.currentTimeMillis();
                                executer.dayFreeTaskExecute(1);
                                logger.info("TimerTaskManager.dayFreeExecute:" + executer.getClass().getName() + "  执行耗时：" + (System.currentTimeMillis() - startTime));
                        } catch (Exception ex) {
                                logger.error("执行日级空闲任务报错，错误类为" + executer.getClass().getName());
                        }
                }
        }

        public void dayStartExecute() {
                if (!isWorking){
                        return;}
                if (Formula.isEmptyCollection(dayStartExecuters)){
                        return;}
                for (IDayStartTaskExecuter executer : dayStartExecuters) {
                        try {
                                long startTime = System.currentTimeMillis();
                                executer.dayStartTaskExecute(1);
                                logger.info("TimerTaskManager.dayStartExecute:" + executer.getClass().getName() + "  执行耗时：" + (System.currentTimeMillis() - startTime));
                        } catch (Exception ex) {
                                logger.error("执行日级初始任务报错，错误类为" + executer.getClass().getName(), ex);
                        }
                }
        }

        public void weekFreeExecute() {
                if (!isWorking){
                        return;}
                if (Formula.isEmptyCollection(weekFreeTaskExecuters)){
                        return;}
                for (IWeekFreeTaskExecuter executer : weekFreeTaskExecuters) {
                        try {
                                long startTime = System.currentTimeMillis();
                                executer.weekFreeTaskExecute(1);
                                logger.info("TimerTaskManager.weekFreeExecute:" + executer.getClass().getName() + "  执行耗时：" + (System.currentTimeMillis() - startTime));
                        } catch (Exception ex) {
                                logger.error("执行周级空闲任务报错，错误类为" + executer.getClass().getName(), ex);
                        }
                }
        }

        public void weekStartExecute() {
                if (!isWorking) {
                        return;
                }
                if (Formula.isEmptyCollection(weekStartTaskExecuters)) {
                        return;
                }
                for (IWeekStartTaskExecuter executer : weekStartTaskExecuters) {
                        try {
                                long startTime = System.currentTimeMillis();
                                executer.weekStartExecute(1);
                                logger.info("TimerTaskManager.weekStartExecute:" + executer.getClass().getName() + "  执行耗时：" + (System.currentTimeMillis() - startTime) + "时间：" +System.currentTimeMillis());
                        } catch (Exception ex) {
                                logger.error("执行周级初始任务报错，错误类为" + executer.getClass().getName(), ex);
                        }
                }
        }
}
