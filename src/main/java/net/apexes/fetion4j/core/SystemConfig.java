/*
 * Copyright (C) 2012, Apexes Network Technology. All rights reserved.
 * 
 *       http://www.apexes.net
 * 
 */

package net.apexes.fetion4j.core;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.apexes.fetion4j.core.client.activity.Template;
import net.apexes.fetion4j.core.util.XmlElement;
import net.apexes.fetion4j.core.util.XmlElementHelper;

/**
 * Parsed Fetion system configuration obtained from the navigation service.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public final class SystemConfig {
    
    private XmlElement xml;
    private LinkedHashMap<String, String> versionMap;
    
    public SystemConfig(XmlElement xml) {
        this.xml = xml;
        versionMap = new LinkedHashMap<String, String>();
        versionMap.put("servers", XmlElementHelper.getNodeAttribute(xml, "/config/servers", "version"));
        versionMap.put("service-no", XmlElementHelper.getNodeAttribute(xml, "/config/service-no", "version"));
        versionMap.put("parameters", XmlElementHelper.getNodeAttribute(xml, "/config/parameters", "version"));
        versionMap.put("hints", XmlElementHelper.getNodeAttribute(xml, "/config/hints", "version"));
        versionMap.put("http-applications", XmlElementHelper.getNodeAttribute(xml, "/config/http-applications", "version"));
        versionMap.put("client-config", XmlElementHelper.getNodeAttribute(xml, "/config/client-config", "version"));
        versionMap.put("services", XmlElementHelper.getNodeAttribute(xml, "/config/services", "version"));
    }
    
    public String getValue(String key) {
        return XmlElementHelper.getNodeContent(xml, key);
    }
    
    /**
     * 返回摘要信息。
     * @return 
     */
    public String getSummary() {
        String summary = Template.TMPL_SYSTEM_CONFIG;
        Iterator<Map.Entry<String, String>> it = versionMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String value = entry.getValue();
            if (value == null) {
                value = "0";
            }
            summary = summary.replace("{" + entry.getKey() + "-version}", value);
        }
        return summary;
    }
    
    public XmlElement getXml() {
        return xml;
    }
    
    /**
     * 
     * @param xml
     * @return 如果有修改返回 true，否则返回 false
     */
    public boolean update(XmlElement xml) {
        boolean isChanged = false;
        Iterator<String> it = versionMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            XmlElement newEl = xml.getChild(key);
            if (newEl != null) {
                XmlElement oldEl = this.xml.getChild(key);
                if (oldEl != null) {
                    this.xml.removeChild(oldEl);
                }
                this.xml.addChild(newEl);
                isChanged = true;
            }
        }
        return isChanged;
    }

    @Override
    public String toString() {
        return xml.toString();
    }
}
