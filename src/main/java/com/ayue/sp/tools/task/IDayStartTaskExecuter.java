package com.ayue.sp.tools.task;

public interface IDayStartTaskExecuter {
        /**
         * 每日初始执行，只用来执行初始化操作，不要用来执行消耗大的任务
         * 
         * @param times
         * @author Neo
         * @description 方法说明
         */
        void dayStartTaskExecute(int times);
}
