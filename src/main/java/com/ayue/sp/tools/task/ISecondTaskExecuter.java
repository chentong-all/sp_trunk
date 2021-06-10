package com.ayue.sp.tools.task;

/**
 * 秒级任务执行接口
 * 
 * @author Neo
 * @descirption
 */
public interface ISecondTaskExecuter {
        /**
         * 执行秒级任务，每两秒调用一次
         * 
         * @param times
         * @author Neo
         * @description 方法说明
         */
        void secondTaskExecute(int times);
}
