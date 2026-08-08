/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */
package net.apexes.fetion4j.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Helper methods for navigating {@link XmlElement} trees.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class XmlElementHelper {

    /**
     *
     * @param xml
     * @param path
     * @return
     */
    public static XmlElement getNode(XmlElement xml, String path) {
        String start = "/" + xml.getName() + "/";
        if (!path.startsWith(start)) {
            return null;
        }
        path = path.replaceFirst(start, "");
        String[] nodes = path.split("/");
        for (String node : nodes) {
            xml = xml.getChild(node);
            if (xml == null) {
                return null;
            }
        }
        return xml;
    }

    /**
     *
     * @param xml
     * @param path
     * @return
     */
    public static String getNodeContent(XmlElement xml, String path) {
        xml = getNode(xml, path);
        if (xml == null) {
            return null;
        }
        return xml.getContent();
    }

    /**
     *
     * @param xml
     * @param path
     * @param name
     * @return
     */
    public static String getNodeAttribute(XmlElement xml, String path, String name) {
        xml = getNode(xml, path);
        if (xml == null) {
            return null;
        }
        return xml.getStringAttribute(name);
    }
    
    /**
     * 
     * @param xml
     * @param path
     * @param name
     * @param defaultValue
     * @return 
     */
    public static String getNodeAttribute(XmlElement xml, String path, 
            String name, String defaultValue) {
        xml = getNode(xml, path);
        if (xml == null) {
            return null;
        }
        return xml.getStringAttribute(name, defaultValue);
    }

    /**
     *
     * @param xmlFile
     * @return
     */
    public static XmlElement open(File xmlFile, String charsetName) throws IOException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(xmlFile), charsetName);
            XmlElement xml = new XmlElement();
            xml.parseFromReader(reader);
            return xml;
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                //throw ex;
            }
        }
    }

    public static void write(XmlElement xml, File xmlFile, String charsetName) throws IOException {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(xmlFile), "UTF-8");
            xml.write(writer, true, 4);
            writer.flush();
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
            }
        }
    }

    /*
    public static void main(String[] args) throws Exception {
        XmlElement xml = open("systemconfig.xml", "UTF-8");
        System.out.println(getNodeContent(xml, "/config/servers/get-pic-code"));
        System.out.println(getNodeContent(xml, "/config/servers/ssi-app-sign-in-v2"));
        System.out.println(getNodeAttribute(xml, "/config/servers", "version"));
    }
    //*/
}
