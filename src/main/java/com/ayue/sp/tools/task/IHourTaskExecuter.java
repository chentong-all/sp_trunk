package com.ayue.sp.tools.task;

/**
 * 小时级别任务执行器
 * 
 * @author Neo
 * @descirption
 */
public interface IHourTaskExecuter {
        /**
         * 执行小时级别任务，每小时调用一次
         * 
         * @param times
         * @author Neo
         * @description 方法说明
         */
        void hourTaskExecute(int times);
}
