/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core;

/**
 * Generic result envelope for Fetion client operations.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class Result {
    
    /**
     * 
     */
    public static enum Type {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 失败
         */
        FAILURE
    }
    
    /**
     * 状态码
     */
    private int status;
    /**
	 * 回复状态说明
	 */
	private String statusMessage;
    /**
     * 类型
     */
    private Type type;
    /**
     * 描述信息
     */
    private String describe;
    
    public Result(int status, String statusMessage, Type type, String describe) {
        this.status = status;
        this.statusMessage = statusMessage;
        this.type = type;
        this.describe = describe;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Type getType() {
        return type;
    }

    public String getDescribe() {
        return describe;
    }

    @Override
    public String toString() {
        return "Result{" 
                + "status=" + status 
                + ", statusMessage=" + statusMessage 
                + ", describe=" + describe 
                + '}';
    }
}
