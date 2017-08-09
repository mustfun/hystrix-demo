package com.dzy.learn.enums;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/9
 * @since 1.0
 */
public enum  HystrixType {

    SUCCESS("success"),
    FAIL("fail");

    private String name;

    private HystrixType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
