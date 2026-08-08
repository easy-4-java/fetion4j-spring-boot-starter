/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core.sipc;

import java.util.ArrayList;
import java.util.List;

/**
 * Base type for Fetion SIPC request and response messages.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public abstract class SipcMessage {
    
    /**
     * 
     */
    public final static String SEPARATOR = "\r\n";
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * I头域的值。如“I: 1”中的1
     */
    private int callId;
    /**
     * Q头域中的数值。如“Q: 2 R”中的2
     */
    private int sequence;
    /**
     * 分块包的偏移，不是分块包时为-1
     */
    private int sliceOffset = -1;
    /**
     * 消息头域
     */
    private List<Field> fieldList;
    /**
     * 消息的正文内容
     */
    private String body = null;
    
    protected SipcMessage() {
        fieldList = new ArrayList<Field>();
    }

    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public int getCallId() {
        return callId;
    }

    public void setCallId(int callId) {
        this.callId = callId;
    }
    
    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    
    /**
     * 返回分块包的偏移， 如果没有就返回-1 一个请求的结果如果太大，
     * 服务器就会分成几个小的信令包返回，一般状态码为188 Partial就是一个分块包
     * 如果是一个分块包，L头部类似于64240;p=0，后面的p就代表了这个分块包在整个包的偏移
     *
     * @return
     */
    public int getSliceOffset() {
        return sliceOffset;
    }

    /**
     * 设置分块包的偏移
     *
     * @param sliceOffset
     */
    public void setSliceOffset(int sliceOffset) {
        this.sliceOffset = sliceOffset;
    }
    
    /**
     * 返回 body 的字节数
     * @return 
     */
    public int getBodyLength() {
        if (getBody() == null) {
            return 0;
        }
        return getBody().getBytes().length;
    }
    
    public void addField(String name, String value) {
        fieldList.add(new Field(name, value));
    }
    
    public void setField(String name, String value) {
        removeAllField(name);
        fieldList.add(new Field(name, value));
    }
    
    public void removeField(String fieldName) {
        for (int i = fieldList.size() - 1; i >= 0; i--) {
            Field field = fieldList.get(i);
            if (field.getName().equals(fieldName)) {
                fieldList.remove(i);
                break;
            }
        }
    }
    
    public void removeAllField(String fieldName) {
        for (int i = fieldList.size() - 1; i >= 0; i--) {
            Field field = fieldList.get(i);
            if (field.getName().equals(fieldName)) {
                fieldList.remove(i);
            }
        }
    }
    
    /**
     * 检查是否有给定名字的消息头域
     * @param fieldName
     * @return 如果有返回true，否则返回false.
     */
    public boolean hasField(String fieldName) {
        boolean b = false;
        for (Field field : fieldList) {
            if (field.getName().equals(fieldName)) {
                b = true;
                break;
            }
        }
        return b;
    }
    
    private Field getField(String fieldName) {
        for (Field field : fieldList) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }
    
    public String getFieldValue(String fieldName) {
        String value = null;
        Field field = getField(fieldName);
        if (field != null) {
            value = field.getValue();
        }
        return value;
    }
    
    public List<String> getFieldValues(String fieldName) {
        ArrayList<String> list = new ArrayList<String>();
        for (Field field : fieldList) {
            if (field.getName().equals(fieldName)) {
                list.add(field.getValue());
            }
        }
        return list;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
    public String getText() {
        StringBuilder buf = new StringBuilder(getHeadline());
        buf.append(SipcMessage.SEPARATOR);
        // I头域
        buf.append(Sipc.FIELD_I);
        buf.append(": ");
        buf.append(getCallId());
        buf.append(SipcMessage.SEPARATOR);
        // Q头域
        buf.append(Sipc.FIELD_Q);
        buf.append(": ");
        buf.append(getSequence());
        buf.append(" ");
        buf.append(getMethod());
        buf.append(SipcMessage.SEPARATOR);
        // 其他头域
        for (Field field : fieldList) {
            buf.append(field.getText());
            buf.append(SipcMessage.SEPARATOR);
        }
        // L头域和消息正文
        int len = getBodyLength();
        if (len > 0) {
            buf.append(Sipc.FIELD_L);
            buf.append(": ");
            buf.append(len);
            if (getSliceOffset() != -1) {
                buf.append(";p=");
                buf.append(getSliceOffset());
            }
            buf.append(SipcMessage.SEPARATOR);
            buf.append(SipcMessage.SEPARATOR);
            buf.append(getBody());
        } else {
            buf.append(SipcMessage.SEPARATOR);
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return getText();
    }
    
    /**
     * 获取消息的标题
     *
     * @return 返回消息的标题
     */
    protected abstract String getHeadline();
    
}
