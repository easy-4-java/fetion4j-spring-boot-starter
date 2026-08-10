/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core;

import net.apexes.fetion4j.core.client.Controller;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Represents the Fetion account quota (e.g. remaining SMS count).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class Quota {

    private int maxBuddies;
    private int sendSmsDayLimit;
    private int sendSmsDayCount;
    private int sendSmsMonthLimit;
    private int sendSmsMonthCount;

    public int getMaxBuddies() {
        return maxBuddies;
    }

    public int getSendSmsDayLimit() {
        return sendSmsDayLimit;
    }

    public int getSendSmsDayCount() {
        return sendSmsDayCount;
    }

    public int getSendSmsMonthLimit() {
        return sendSmsMonthLimit;
    }

    public int getSendSmsMonthCount() {
        return sendSmsMonthCount;
    }

    /**
     *
     * @param controller
     * @param xml
     */
    public void update(Controller controller, XmlElement xml) {
        //<quotas>
        //<quota-limit>
        //    <limit name="max-buddies" value="500"/>
        //    <limit name="max-groupadmin-count" value="4"/>
        //    <limit name="max-joingroup-count" value="20"/>
        //</quota-limit>
        //<quota-frequency>
        //    <frequency name="send-sms" day-limit="1000" day-count="6" month-limit="15000" month-count="6"/>
        //    <frequency name="free-direct-sms" day-limit="0" day-count="0" month-limit="0" month-count="0"/>
        //</quota-frequency>
        //</quotas>
        XmlElement limitEl = xml.getChild("quota-limit");
        if (limitEl != null) {
            for (XmlElement el : limitEl.getChildren()) {
                if ("max-buddies".equals(el.getStringAttribute("name"))) {
                    maxBuddies = el.getIntAttribute("value", 0);
                    break;
                }
            }
        }
        XmlElement frequencyEl = xml.getChild("quota-frequency");
        if (frequencyEl != null) {
            for (XmlElement el : frequencyEl.getChildren()) {
                if ("send-sms".equals(el.getStringAttribute("name"))) {
                    sendSmsDayLimit = el.getIntAttribute("day-limit", sendSmsDayLimit);
                    sendSmsDayCount = el.getIntAttribute("day-count", sendSmsDayCount);
                    sendSmsMonthLimit = el.getIntAttribute("month-limit", sendSmsMonthLimit);
                    sendSmsMonthCount = el.getIntAttribute("month-count", sendSmsMonthCount);
                    controller.fireSmsCountChanged(sendSmsDayCount, sendSmsMonthCount);
                    break;
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return "Quota{" + "maxBuddies=" + maxBuddies
                + ", sendSmsDayLimit=" + sendSmsDayLimit
                + ", sendSmsDayCount=" + sendSmsDayCount
                + ", sendSmsMonthLimit=" + sendSmsMonthLimit
                + ", sendSmsMonthCount=" + sendSmsMonthCount
                + '}';
    }
}
