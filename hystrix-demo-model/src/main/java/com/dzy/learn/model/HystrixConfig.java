package com.dzy.learn.model;

import java.lang.reflect.Method;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
public class HystrixConfig {

    private String config;
    private Method method;
    private Method failMethod;

    public String getConfig() {
        return config;
    }
    public void setConfig(String config) {
        this.config = config;
    }
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }
    public Method getFailMethod() {
        return failMethod;
    }
    public void setFailMethod(Method failMethod) {
        this.failMethod = failMethod;
    }
}
