package com.wj.reggie.common;

import org.springframework.stereotype.Component;

/**
 * @author wj
 * @version 1.0
 */

public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
