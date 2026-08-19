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
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
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

    /** @return return the status. */
    public int getStatus() {
        return status;
    }

    /** @return return the status message. */
    public String getStatusMessage() {
        return statusMessage;
    }

    /** @return return the type. */
    public Type getType() {
        return type;
    }

    /** @return return the describe. */
    public String getDescribe() {
        return describe;
    }

    @Override
    /**
     * <p>To string.</p>
     * @return the result
     */
    public String toString() {
        return "Result{" 
                + "status=" + status 
                + ", statusMessage=" + statusMessage 
                + ", describe=" + describe 
                + '}';
    }
}
