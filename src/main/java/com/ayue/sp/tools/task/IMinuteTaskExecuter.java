package com.ayue.sp.tools.task;

/**
 * 分钟级别后台任务执行器接口
 * 
 * @author Neo
 * @descirption
 */
public interface IMinuteTaskExecuter {
        /**
         * 执行分钟级别后台任务-每分钟调用一次
         * 
         * @param times
         * @author Neo
         * @description 方法说明
         */
        void minuteTaskExecute(int times);
}
