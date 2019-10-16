package com.leyou.common.threadlocals;

/**
 * 存放用户信息的容器
 */
public class UserHolder {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();

    /**
     * 设置threadlocal中的值
     * @param userId
     */
    public static void setUser(Long userId){

        tl.set(userId);
    }

    /**
     * 获取threadlocal中的数据
     * @return
     */
    public static Long getUser(){
        return tl.get();
    }

    /**
     * 删除数据
     */
    public static void removeUser(){
        tl.remove();
    }
}
