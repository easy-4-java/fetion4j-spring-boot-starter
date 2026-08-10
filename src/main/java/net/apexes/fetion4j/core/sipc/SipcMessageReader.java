/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.sipc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Parses Fetion SIPC messages from an input stream.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class SipcMessageReader {

    private DataInputStream in;
    
    public SipcMessageReader(InputStream in) throws IOException {
        this.in = new DataInputStream(in);
    }

    public void close() throws IOException {
        in.close();
    }

    public SipcMessage read() throws IOException, ParseException {
        SipcMessage message = null;
        String headline = in.readLine();
        if (headline != null) {
            if (headline.startsWith(Sipc.SIPC_VERSION)) {
                message = parseResponseMessage(headline);
            } else if (headline.endsWith(Sipc.SIPC_VERSION)) {
                message = parseRequestMessage(headline);
            }
        }
        return message;
    }

    private ResponseMessage parseResponseMessage(String headline) 
            throws IOException, ParseException {
        String[] strArr = headline.split(" ");
        int status;
        try {
            status = Integer.valueOf(strArr[1]);
        } catch (NumberFormatException ex) {
            throw new ParseException(strArr[1] + " not a Integer.", 0);
        }
        String statusMessage = strArr[2];
        ResponseMessage message = new ResponseMessage(status, statusMessage);
        complete(message);
        return message;
    }
    
    private RequestMessage parseRequestMessage(String headline) 
            throws IOException, ParseException {
        String[] strArr = headline.split(" ");
        String method = strArr[0];
        String acceptor = strArr[1];
        RequestMessage message = new RequestMessage(acceptor);
        message.setMethod(method);
        complete(message);
        return message;
    }
    
    private void complete(SipcMessage message) throws IOException, ParseException {
        int bodyLen = 0;
        String lineStr;
        while (true) {
            lineStr = in.readLine();
            if (lineStr == null) {
                break;
            }
            int i = lineStr.indexOf(':');
            if (i != -1) {
                String name = lineStr.substring(0, i).trim();
                String value = lineStr.substring(i + 1).trim();
                if (Sipc.FIELD_I.equals(name)) {
                    message.setCallId(Integer.valueOf(value));
                } else if (Sipc.FIELD_Q.equals(name)) {
                    int index = value.indexOf(' ');
                    String seq = value.substring(0, index).trim();
                    try {
                        int sequence = Integer.valueOf(seq);
                        message.setSequence(sequence);
                    } catch (NumberFormatException ex) {
                        throw new ParseException(seq + " not a Integer.", 0);
                    }
                    String method = value.substring(index+1).trim();
                    message.setMethod(method);
                } else if (Sipc.FIELD_L.equals(name)) {
                    i = value.indexOf(";p=");
                    if (i != -1) {
                        bodyLen = Integer.valueOf(value.substring(0, i));
                        int offset = Integer.valueOf(value.substring(i + 1));
                        message.setSliceOffset(offset);
                    } else {
                        bodyLen = Integer.valueOf(value);
                    }
                } else {
                    message.addField(name, value);
                }
            } else if (bodyLen > 0) {
                byte[] bytes = new byte[bodyLen];
                int len = 0;
                i = len;
                do {
                    len = in.read(bytes, i, bodyLen - i);
                    i += len;
                } while (i < bodyLen);
                String body = new String(bytes, "utf-8");
                message.setBody(body);
                break;
            } else {
                break;
            }
        }
    }
    
    /*
    public static void main(String[] args) throws Exception {
        java.io.FileInputStream in = new java.io.FileInputStream("test.txt");
        SipcMessageReader reader = new SipcMessageReader(in);
        SipcMessage message = reader.read();
        while (message != null) {
            //System.out.print(message.getText());
            message = reader.read();
        }
    }
    //*/
    
}
