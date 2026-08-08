/*
 * Copyright (C) 2013, Apexes Network Technology. All rights reserved.
 *
 *       http://www.apexes.net
 *
 */
package net.apexes.fetion4j.core.client;

import java.util.ArrayList;

import net.apexes.fetion4j.core.util.XmlElement;
import net.apexes.fetion4j.core.util.XmlElementHelper;

/**
 * Validates China Mobile phone numbers against the Fetion mobile-number distribution table.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class CmccMobileValidator {
    
    private ArrayList<Part> partList;
    
    //<r><c v="cmcc">
    //<d s="13500000000" e="13999999999"/>
    //<d s="13400000000" e="13489999999"/>
    //<d s="15900000000" e="15999999999"/>
    //</c></r>
    /**
     * 
     * @param xml 
     */
    public CmccMobileValidator(XmlElement xml) {
        partList = new ArrayList<Part>();
        xml = XmlElementHelper.getNode(xml, "/r/c");
        
        for (XmlElement el : xml.getChildren()) {
            long s = el.getLongAttribute("s");
            long e = el.getLongAttribute("e");
            if (s != 0 && e != 0) {
                partList.add(new Part(s, e));
            }
        }
    }
    
    public boolean isCmccMobileNo(long mobileNo) {
        boolean b = false;
        for (Part part : partList) {
            b = part.isWithin(mobileNo);
            if (b) {
                break;
            }
        }
        return b;
    }
    
    /**
     * 
     */
    private static class Part {
        private long start;
        private long end;
        
        Part(long start, long end) {
            this.start = start;
            this.end = end;
        }
        
        boolean isWithin(long mobileNo) {
            return mobileNo >= start && mobileNo <= end;
        }
    }
    
}
