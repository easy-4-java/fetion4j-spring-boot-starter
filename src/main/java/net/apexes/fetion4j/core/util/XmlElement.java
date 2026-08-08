/* XmlElement.java
 *
 * $Revision: 1.5 $
 * $Date: 2002/03/24 10:27:59 $
 * $Name: RELEASE_2_2_1 $
 *
 * This file is part of NanoXML 2 Lite.
 * Copyright (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 *****************************************************************************/
package net.apexes.fetion4j.core.util;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight DOM-like XML element used for parsing Fetion responses.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 1.0.0
 */
public class XmlElement implements java.io.Serializable {

    /**
     * Serialization serial version ID.
     */
    private static final long serialVersionUID = 6685035139346394777L;
    /**
     * Separate line of system default.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    /**
     * The attributes given to the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field can be empty.
     *     <li>The field is never <code>null</code>.
     *     <li>The keys and the values are strings.
     * </ul></dd></dl>
     */
    private Map<String, String> attributes;
    /**
     * Child elements of the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field can be empty.
     *     <li>The field is never <code>null</code>.
     *     <li>The elements are instances of <code>XmlElement</code>
     *         or a subclass of <code>XmlElement</code>.
     * </ul></dd></dl>
     */
    private List<XmlElement> children;
    /**
     * The name of the element.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is <code>null</code> iff the element is not
     *         initialized by either parse or setName.
     *     <li>If the field is not <code>null</code>, it's not empty.
     *     <li>If the field is not <code>null</code>, it contains a valid
     *         XML identifier.
     * </ul></dd></dl>
     */
    private String name;
    /**
     * The parent of the element.
     * 
     */
    private XmlElement parent;
    /**
     * The #PCDATA content of the object.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is <code>null</code> iff the element is not a
     *         #PCDATA element.
     *     <li>The field can be any string, including the empty string.
     * </ul></dd></dl>
     */
    private String content;
    /**
     * Conversion table for &amp;...; entities. The keys are the entity names
     * without the &amp; and ; delimiters.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is never <code>null</code>.
     *     <li>The field always contains the following associations:
     *         "lt"&nbsp;=&gt;&nbsp;"&lt;", "gt"&nbsp;=&gt;&nbsp;"&gt;",
     *         "quot"&nbsp;=&gt;&nbsp;"\"", "apos"&nbsp;=&gt;&nbsp;"'",
     *         "amp"&nbsp;=&gt;&nbsp;"&amp;"
     *     <li>The keys are strings
     *     <li>The values are char arrays
     * </ul></dd></dl>
     */
    private Map<String, char[]> entities;
    /**
     * The line number where the element starts.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li><code>lineNr &gt= 0</code>
     * </ul></dd></dl>
     */
    private int lineNr;
    /**
     * <code>true</code> if the leading and trailing whitespace of #PCDATA
     * sections have to be ignored.
     */
    private boolean ignoreWhitespace;
    /**
     * Character read too much.
     * This character provides push-back functionality to the input reader
     * without having to use a PushbackReader.
     * If there is no such character, this field is '\0'.
     */
    private char charReadTooMuch;
    /**
     * The reader provided by the caller of the parse method.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>The field is not <code>null</code> while the parse method
     *         is running.
     * </ul></dd></dl>
     */
    private Reader reader;
    /**
     * The current line number in the source content.
     *
     * <dl><dt><b>Invariants:</b></dt><dd>
     * <ul><li>parserLineNr &gt; 0 while the parse method is running.
     * </ul></dd></dl>
     */
    private int parserLineNr;
    
    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XmlElement(new Hashtable(), false, true)
     * </code></ul>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>enumerateChildren() => empty enumeration
     *     <li>enumeratePropertyNames() => empty enumeration
     *     <li>getChildren() => empty vector
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl>
     *
     * @see nanoxml.XmlElement#XmlElement(java.util.Hashtable)
     *         XmlElement(Hashtable)
     * @see nanoxml.XmlElement#XmlElement(boolean)
     * @see nanoxml.XmlElement#XmlElement(java.util.Hashtable,boolean)
     *         XmlElement(Hashtable, boolean)
     */
    public XmlElement() {
        this(new HashMap<String, char[]>());
    }

    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XmlElement(entities, false, true)
     * </code></ul>
     *
     * @param entities
     *     The entity conversion table.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>entities != null</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>enumerateChildren() => empty enumeration
     *     <li>enumeratePropertyNames() => empty enumeration
     *     <li>getChildren() => empty vector
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#XmlElement()
     * @see nanoxml.XmlElement#XmlElement(boolean)
     * @see nanoxml.XmlElement#XmlElement(java.util.Hashtable,boolean)
     *         XmlElement(Hashtable, boolean)
     */
    public XmlElement(Map<String, char[]> entities) {
        this(entities, true, true);
    }

    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XmlElement(new Hashtable(), skipLeadingWhitespace, true)
     * </code></ul>
     *
     * @param skipLeadingWhitespace
     *     <code>true</code> if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     *
     * </dl><dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>enumerateChildren() => empty enumeration
     *     <li>enumeratePropertyNames() => empty enumeration
     *     <li>getChildren() => empty vector
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#XmlElement()
     * @see nanoxml.XmlElement#XmlElement(java.util.Hashtable)
     *         XmlElement(Hashtable)
     * @see nanoxml.XmlElement#XmlElement(java.util.Hashtable,boolean)
     *         XmlElement(Hashtable, boolean)
     */
    public XmlElement(boolean skipLeadingWhitespace) {
        this(new HashMap<String, char[]>(), skipLeadingWhitespace, true);
    }

    /**
     * Creates and initializes a new XML element.
     * Calling the construction is equivalent to:
     * <ul><code>new XmlElement(entities, skipLeadingWhitespace, true)
     * </code></ul>
     *
     * @param entities
     *     The entity conversion table.
     * @param skipLeadingWhitespace
     *     <code>true</code> if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>entities != null</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>enumerateChildren() => empty enumeration
     *     <li>enumeratePropertyNames() => empty enumeration
     *     <li>getChildren() => empty vector
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#XmlElement()
     * @see nanoxml.XmlElement#XmlElement(boolean)
     * @see nanoxml.XmlElement#XmlElement(java.util.Hashtable)
     *         XmlElement(Hashtable)
     */
    public XmlElement(Map<String, char[]> entities, boolean skipLeadingWhitespace) {
        this(entities, skipLeadingWhitespace, true);
    }

    /**
     * Creates and initializes a new XML element.
     * <P>
     * This constructor should <I>only</I> be called from
     * {@link #createAnotherElement() createAnotherElement}
     * to create child elements.
     *
     * @param entities
     *     The entity conversion table.
     * @param skipLeadingWhitespace
     *     <code>true</code> if leading and trailing whitespace in PCDATA
     *     content has to be removed.
     * @param fillBasicConversionTable
     *     <code>true</code> if the basic entities need to be added to
     *     the entity list.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>entities != null</code>
     *     <li>if <code>fillBasicConversionTable == false</code>
     *         then <code>entities</code> contains at least the following
     *         entries: <code>amp</code>, <code>lt</code>, <code>gt</code>,
     *         <code>apos</code> and <code>quot</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => 0
     *     <li>enumerateChildren() => empty enumeration
     *     <li>enumeratePropertyNames() => empty enumeration
     *     <li>getChildren() => empty vector
     *     <li>getContent() => ""
     *     <li>getLineNr() => 0
     *     <li>getName() => null
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#createAnotherElement()
     */
    protected XmlElement(Map<String, char[]> entities, 
            boolean skipLeadingWhitespace,
            boolean fillBasicConversionTable) {
        this.ignoreWhitespace = skipLeadingWhitespace;
        this.name = null;
        this.parent = null;
        this.content = "";
        this.attributes = new LinkedHashMap<String, String>();
        this.children = new ArrayList<XmlElement>();
        this.entities = new HashMap<String, char[]>();
        this.lineNr = 0;
        if (entities != null && !entities.isEmpty()) {
            this.entities.putAll(entities);
        }
        if (fillBasicConversionTable) {
            this.entities.put("amp", new char[]{'&'});
            this.entities.put("quot", new char[]{'"'});
            this.entities.put("apos", new char[]{'\''});
            this.entities.put("lt", new char[]{'<'});
            this.entities.put("gt", new char[]{'>'});
        }
    }
   
    /**
     * 
     * @return <code>true</code> if the leading and trailing whitespace of #PCDATA
     * sections have to be ignored.
     */
    public boolean isIgnoreWhitespace() {
        return ignoreWhitespace;
    }

    /**
     * Adds a child element.
     *
     * @param child
     *     The child element to add.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>child != null</code>
     *     <li><code>child.getName() != null</code>
     *     <li><code>child</code> does not have a parent element
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => old.countChildren() + 1
     *     <li>enumerateChildren() => old.enumerateChildren() + child
     *     <li>getChildren() => old.enumerateChildren() + child
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#countChildren()
     * @see nanoxml.XmlElement#enumerateChildren()
     * @see nanoxml.XmlElement#getChildren()
     * @see nanoxml.XmlElement#removeChild(nanoxml.XmlElement)
     *         removeChild(XmlElement)
     */
    public void addChild(XmlElement child) {
        if (child == this) {
            throw new IllegalArgumentException("Child element can't is this.");
        }
        XmlElement el = this.parent;
        while (el != null) {
            if (child == el) {
                throw new IllegalArgumentException("Child element can't is super.");
            }
            el = el.parent;
        }
        child.setParent(this);
        this.children.add(child);
    }
    
    /**
     * Removes a child element.
     *
     * @param child
     *     The child element to remove.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>child != null</code>
     *     <li><code>child</code> is a child element of the receiver
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>countChildren() => old.countChildren() - 1
     *     <li>enumerateChildren() => old.enumerateChildren() - child
     *     <li>getChildren() => old.enumerateChildren() - child
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#addChild(nanoxml.XmlElement)
     *         addChild(XmlElement)
     * @see nanoxml.XmlElement#countChildren()
     * @see nanoxml.XmlElement#enumerateChildren()
     * @see nanoxml.XmlElement#getChildren()
     */
    public void removeChild(XmlElement child) {
        child.setParent(null);
        this.children.remove(child);
    }

    /**
     * Returns the name of the element.
     *
     * @see nanoxml.XmlElement#setName(java.lang.String) setName(String)
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Changes the name of the element.
     *
     * @param name
     *     The new name.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * @see nanoxml.XmlElement#getName()
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the parent of the element.
     * 
     * @return 
     */
    public XmlElement getParent() {
        return parent;
    }
    
    /**
     * 
     * @param parent 
     */
    protected void setParent(XmlElement parent) {
        this.parent = parent;
    }
    
    /**
     * Returns the number of child elements of the element.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result >= 0</code>
     * </ul></dd></dl>
     *
     * @see nanoxml.XmlElement#addChild(nanoxml.XmlElement)
     *         addChild(XmlElement)
     * @see nanoxml.XmlElement#enumerateChildren()
     * @see nanoxml.XmlElement#getChildren()
     * @see nanoxml.XmlElement#removeChild(nanoxml.XmlElement)
     *         removeChild(XmlElement)
     */
    public int getChildrenCount() {
        return this.children.size();
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public XmlElement getChild(int index) {
        return children.get(index);
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    public XmlElement getChild(String name) {
        for (XmlElement elt : children) {
            if (name.equals(elt.getName())) {
                return elt;
            }
        }
        return null;
    }
    
    /**
     * Returns the child elements as a List.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     * </ul></dd></dl>
     *
     * @see nanoxml.XmlElement#addChild(nanoxml.XmlElement)
     *         addChild(XmlElement)
     * @see nanoxml.XmlElement#countChildren()
     * @see nanoxml.XmlElement#enumerateChildren()
     * @see nanoxml.XmlElement#removeChild(nanoxml.XmlElement)
     *         removeChild(XmlElement)
     */
    public List<XmlElement> getChildren() {
        return children;
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    public List<XmlElement> getChildren(String name) {
        ArrayList<XmlElement> list = new ArrayList<XmlElement>();
        for (XmlElement elt : children) {
            if (name.equals(elt.getName())) {
                list.add(elt);
            }
        }
        return list;
    }
    
    /**
     * Returns the line nr in the source data on which the element is found.
     * This method returns <code>0</code> there is no associated source data.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result >= 0</code>
     * </ul></dd></dl>
     */
    public int getLineNr() {
        return this.lineNr;
    }
            
    /**
     * Returns the PCDATA content of the object. If there is no such content,
     * <CODE>null</CODE> is returned.
     *
     * @see nanoxml.XmlElement#setContent(java.lang.String)
     *         setContent(String)
     */
    public String getContent() {
        return content;
    }
        
    /**
     * Changes the content string.
     *
     * @param content
     *     The new content string.
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Adds or modifies an attribute.
     *
     * @param name
     *     The name of the attribute.
     * @param value
     *     The value of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>value != null</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>enumerateAttributeNames()
     *         => old.enumerateAttributeNames() + name
     *     <li>getAttribute(name) => value
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setDoubleAttribute(java.lang.String, double)
     *         setDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getAttribute(java.lang.String)
     *         getAttribute(String)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String, java.lang.Object)
     *         getAttribute(String, Object)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String,
     *                                      java.util.Hashtable,
     *                                      java.lang.String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String)
     *         getStringAttribute(String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.lang.String)
     *         getStringAttribute(String, String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value.toString());
    }
    
    /**
     * Removes an attribute.
     *
     * @param name
     *     The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>enumerateAttributeNames()
     *         => old.enumerateAttributeNames() - name
     *     <li>getAttribute(name) => <code>null</code>
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#setDoubleAttribute(java.lang.String, double)
     *         setDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String)
     *         getAttribute(String)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String, java.lang.Object)
     *         getAttribute(String, Object)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String,
     *                                      java.util.Hashtable,
     *                                      java.lang.String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String)
     *         getStringAttribute(String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.lang.String)
     *         getStringAttribute(String, String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String)
     *         getIntAttribute(String)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String, int)
     *         getIntAttribute(String, int)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String)
     *         getDoubleAttribute(String)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String, double)
     *         getDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getBooleanAttribute(java.lang.String,
     *                                             java.lang.String,
     *                                             java.lang.String, boolean)
     *         getBooleanAttribute(String, String, String, boolean)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }
    
    /**
     * Get the attribute names.
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     * </ul></dd></dl>
     *
     * @see nanoxml.XmlElement#setDoubleAttribute(java.lang.String, double)
     *         setDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String)
     *         getAttribute(String)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String, java.lang.Object)
     *         getAttribute(String, String)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String,
     *                                      java.util.Hashtable,
     *                                      java.lang.String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String)
     *         getStringAttribute(String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.lang.String)
     *         getStringAttribute(String, String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String)
     *         getIntAttribute(String)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String, int)
     *         getIntAttribute(String, int)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String)
     *         getDoubleAttribute(String)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String, double)
     *         getDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     * @see nanoxml.XmlElement#getBooleanAttribute(java.lang.String,
     *                                             java.lang.String,
     *                                             java.lang.String, boolean)
     *         getBooleanAttribute(String, String, String, boolean)
     */
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>null</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getAttribute(java.lang.String, java.lang.Object)
     *         getAttribute(String, Object)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String,
     *                                      java.util.Hashtable,
     *                                      java.lang.String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     */
    public Object getAttribute(String name) {
        return this.getAttribute(name, null);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getAttribute(java.lang.String)
     *         getAttribute(String)
     * @see nanoxml.XmlElement#getAttribute(java.lang.String,
     *                                      java.util.Hashtable,
     *                                      java.lang.String, boolean)
     *         getAttribute(String, Hashtable, String, boolean)
     */
    public Object getAttribute(String name, Object defaultValue) {
        Object value = this.attributes.get(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>0</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String, int)
     *         getIntAttribute(String, int)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public int getIntAttribute(String name) {
        return this.getIntAttribute(name, 0);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String)
     *         getIntAttribute(String)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public int getIntAttribute(String name, int defaultValue) {
        String value = attributes.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw this.invalidValue(name, value);
            }
        }
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name
     *     The name of the attribute.
     * @param value
     *     The value of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>enumerateAttributeNames()
     *         => old.enumerateAttributeNames() + name
     *     <li>getIntAttribute(name) => value
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setDoubleAttribute(java.lang.String, double)
     *         setDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String)
     *         getIntAttribute(String)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String, int)
     *         getIntAttribute(String, int)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public void setIntAttribute(String name, int value) {
        this.attributes.put(name, Integer.toString(value));
    }
    
    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>0</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String, int)
     *         getIntAttribute(String, int)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public long getLongAttribute(String name) {
        return this.getLongAttribute(name, 0);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String)
     *         getIntAttribute(String)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public long getLongAttribute(String name, long defaultValue) {
        String value = attributes.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw this.invalidValue(name, value);
            }
        }
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name
     *     The name of the attribute.
     * @param value
     *     The value of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>enumerateAttributeNames()
     *         => old.enumerateAttributeNames() + name
     *     <li>getIntAttribute(name) => value
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setDoubleAttribute(java.lang.String, double)
     *         setDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String)
     *         getIntAttribute(String)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String, int)
     *         getIntAttribute(String, int)
     * @see nanoxml.XmlElement#getIntAttribute(java.lang.String,
     *                                         java.util.Hashtable,
     *                                         java.lang.String, boolean)
     *         getIntAttribute(String, Hashtable, String, boolean)
     */
    public void setLongAttribute(String name, long value) {
        this.attributes.put(name, Long.toString(value));
    }
    
    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>null</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.lang.String)
     *         getStringAttribute(String, String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     */
    public String getStringAttribute(String name) {
        return this.getStringAttribute(name, null);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String)
     *         getStringAttribute(String)
     * @see nanoxml.XmlElement#getStringAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getStringAttribute(String, Hashtable, String, boolean)
     */
    public String getStringAttribute(String name, String defaultValue) {
        return (String) this.getAttribute(name, defaultValue);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>0.0</code> is returned.
     *
     * @param name The name of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setDoubleAttribute(java.lang.String, double)
     *         setDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String, double)
     *         getDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     */
    public double getDoubleAttribute(String name) {
        return this.getDoubleAttribute(name, 0.);
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     *
     * @param name         The name of the attribute.
     * @param defaultValue Key to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setDoubleAttribute(java.lang.String, double)
     *         setDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String)
     *         getDoubleAttribute(String)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     */
    public double getDoubleAttribute(String name, double defaultValue) {
        String value = attributes.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Double.valueOf(value).doubleValue();
            } catch (NumberFormatException e) {
                throw this.invalidValue(name, value);
            }
        }
    }
    
    /**
     * Adds or modifies an attribute.
     *
     * @param name
     *     The name of the attribute.
     * @param value
     *     The value of the attribute.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>enumerateAttributeNames()
     *         => old.enumerateAttributeNames() + name
     *     <li>getDoubleAttribute(name) => value
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setIntAttribute(java.lang.String, int)
     *         setIntAttribute(String, int)
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String)
     *         getDoubleAttribute(String)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String, double)
     *         getDoubleAttribute(String, double)
     * @see nanoxml.XmlElement#getDoubleAttribute(java.lang.String,
     *                                            java.util.Hashtable,
     *                                            java.lang.String, boolean)
     *         getDoubleAttribute(String, Hashtable, String, boolean)
     */
    public void setDoubleAttribute(String name, double value) {
        this.attributes.put(name, Double.toString(value));
    }

    /**
     * Returns an attribute of the element.
     * If the attribute doesn't exist, <code>defaultValue</code> is returned.
     * If the value of the attribute is equal to <code>trueValue</code>,
     * <code>true</code> is returned.
     * If the value of the attribute is equal to <code>falseValue</code>,
     * <code>false</code> is returned.
     * If the value doesn't match <code>trueValue</code> or
     * <code>falseValue</code>, an exception is thrown.
     *
     * @param name         The name of the attribute.
     * @param trueValue    The value associated with <code>true</code>.
     * @param falseValue   The value associated with <code>true</code>.
     * @param defaultValue Value to use if the attribute is missing.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name</code> is a valid XML identifier
     *     <li><code>trueValue</code> and <code>falseValue</code>
     *         are different strings.
     * </ul></dd></dl><dl>
     *
     * @see nanoxml.XmlElement#setAttribute(java.lang.String, java.lang.Object)
     *         setAttribute(String, Object)
     * @see nanoxml.XmlElement#removeAttribute(java.lang.String)
     *         removeAttribute(String)
     * @see nanoxml.XmlElement#enumerateAttributeNames()
     */
    public boolean getBooleanAttribute(String name,
            String trueValue,
            String falseValue,
            boolean defaultValue) {
        Object value = this.attributes.get(name);
        if (value == null) {
            return defaultValue;
        } else if (value.equals(trueValue)) {
            return true;
        } else if (value.equals(falseValue)) {
            return false;
        } else {
            throw this.invalidValue(name, (String) value);
        }
    }

    /**
     * Reads one XML element from a java.io.Reader and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>reader != null</code>
     *     <li><code>reader</code> is not closed
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     *     <li>the reader points to the first character following the last
     *         '&gt;' character of the XML element
     * </ul></dd></dl><dl>
     *
     * @throws java.io.IOException
     *     If an error occured while reading the input.
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the read data.
     */
    public void parseFromReader(Reader reader) throws IOException, XmlParseException {
        this.parseFromReader(reader, /*startingLineNr*/ 1);
    }

    /**
     * Reads one XML element from a java.io.Reader and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     * @param startingLineNr
     *     The line number of the first line in the data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>reader != null</code>
     *     <li><code>reader</code> is not closed
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     *     <li>the reader points to the first character following the last
     *         '&gt;' character of the XML element
     * </ul></dd></dl><dl>
     *
     * @throws java.io.IOException
     *     If an error occured while reading the input.
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the read data.
     */
    public void parseFromReader(Reader reader, int startingLineNr)
            throws IOException, XmlParseException {
        this.name = null;
        this.content = "";
        this.attributes = new LinkedHashMap<String, String>();
        this.children = new ArrayList<XmlElement>();
        this.charReadTooMuch = '\0';
        this.reader = reader;
        this.parserLineNr = startingLineNr;

        for (;;) {
            char ch = this.scanWhitespace();

            if (ch != '<') {
                throw this.expectedInput("<");
            }

            ch = this.readChar();

            if ((ch == '!') || (ch == '?')) {
                this.skipSpecialTag(0);
            } else {
                this.unreadChar(ch);
                this.scanElement(this);
                return;
            }
        }
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>string.length() &gt; 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the string.
     */
    public void parseString(String string) throws XmlParseException {
        try {
            this.parseFromReader(new StringReader(string),
                    /*startingLineNr*/ 1);
        } catch (IOException e) {
            // Java exception handling suxx
        }
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     * @param offset
     *     The first character in <code>string</code> to scan.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>offset &lt; string.length()</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the string.
     */
    public void parseString(String string, int offset) throws XmlParseException {
        this.parseString(string.substring(offset));
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     * @param offset
     *     The first character in <code>string</code> to scan.
     * @param end
     *     The character where to stop scanning.
     *     This character is not scanned.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>end &lt;= string.length()</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the string.
     */
    public void parseString(String string, int offset, int end)
            throws XmlParseException {
        this.parseString(string.substring(offset, end));
    }

    /**
     * Reads one XML element from a String and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     * @param offset
     *     The first character in <code>string</code> to scan.
     * @param end
     *     The character where to stop scanning.
     *     This character is not scanned.
     * @param startingLineNr
     *     The line number of the first line in the data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li><code>end &lt;= string.length()</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the string.
     */
    public void parseString(String string,
            int offset,
            int end,
            int startingLineNr)
            throws XmlParseException {
        try {
            this.parseFromReader(
                    new StringReader(string.substring(offset, end)), 
                    startingLineNr);
        } catch (IOException e) {
            // Java exception handling suxx
        }
    }

    /**
     * Reads one XML element from a char array and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     * @param offset
     *     The first character in <code>string</code> to scan.
     * @param end
     *     The character where to stop scanning.
     *     This character is not scanned.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>input != null</code>
     *     <li><code>end &lt;= input.length</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the string.
     */
    public void parseCharArray(char[] input, int offset, int end)
            throws XmlParseException {
        this.parseCharArray(input, offset, end, /*startingLineNr*/ 1);
    }

    /**
     * Reads one XML element from a char array and parses it.
     *
     * @param reader
     *     The reader from which to retrieve the XML data.
     * @param offset
     *     The first character in <code>string</code> to scan.
     * @param end
     *     The character where to stop scanning.
     *     This character is not scanned.
     * @param startingLineNr
     *     The line number of the first line in the data.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>input != null</code>
     *     <li><code>end &lt;= input.length</code>
     *     <li><code>offset &lt; end</code>
     *     <li><code>offset &gt;= 0</code>
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>the state of the receiver is updated to reflect the XML element
     *         parsed from the reader
     * </ul></dd></dl><dl>
     *
     * @throws nanoxml.XmlParseException
     *     If an error occured while parsing the string.
     */
    public void parseCharArray(char[] input, int offset, int end, int startingLineNr)
            throws XmlParseException {
        try {
            Reader reader0 = new CharArrayReader(input, offset, end);
            this.parseFromReader(reader0, startingLineNr);
        } catch (IOException e) {
            // This exception will never happen.
        }
    }

    /**
     * Writes the XML element to a writer.
     *
     * @param writer
     *     The writer to write the XML data to.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>writer != null</code>
     *     <li><code>writer</code> is not closed
     * </ul></dd></dl>
     *
     * @throws java.io.IOException
     *      If the data could not be written to the writer.
     *
     * @see nanoxml.XmlElement#toString()
     */
    public void write(Writer writer) throws IOException {
        write(writer, this, false, 0, 0);
    }
    
    /**
     * Writes the XML element to a writer.
     *
     * @param writer
     *     The writer to write the XML data to.
     * @param standalone
     *     If true then element on a separate line.
     * @param indent
     *     How many spaces to indent the element.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>writer != null</code>
     *     <li><code>writer</code> is not closed
     * </ul></dd></dl>
     *
     * @throws java.io.IOException
     *      If the data could not be written to the writer.
     *
     * @see nanoxml.XmlElement#toString()
     */
    public void write(Writer writer, boolean standalone, int indent)
            throws IOException {
        write(writer, this, standalone, indent, 0);
    }
    
    /**
     * Writes the XML element to a writer.
     *
     * @param writer
     *     The writer to write the XML data to.
     * @param standalone
     *     If true then element on a separate line.
     * @param indent
     *     How many spaces to indent the element.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>writer != null</code>
     *     <li><code>writer</code> is not closed
     * </ul></dd></dl>
     *
     * @throws java.io.IOException
     *      If the data could not be written to the writer.
     *
     * @see nanoxml.XmlElement#toString()
     */
    public void write(Writer writer, int indent) throws IOException {
        write(writer, this, true, indent, 0);
    }
    
    /**
     * Writes the XML element to a writer.
     *
     * @param writer
     *     The writer to write the XML data to.
     * @param standalone
     * 
     * @param indent
     *     How many spaces to indent the element.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>writer != null</code>
     *     <li><code>writer</code> is not closed
     * </ul></dd></dl>
     *
     * @throws java.io.IOException
     *      If the data could not be written to the writer.
     *
     * @see nanoxml.XmlElement#toString()
     */
    protected static void write(Writer writer, XmlElement element,
            boolean standalone, int indent, int level) throws IOException {
        if (element.getName() == null) {
            writeEncoded(writer, element.getContent());
            return;
        }
        
        if (level != 0) {
            if (standalone) {
                writer.append(LINE_SEPARATOR);
            }
            for (int i = indent * level; i > 0; i--) {
                writer.append(' ');
            }
        }
        
        writer.write('<');
        writer.write(element.getName());
        Set<String> attrNames = element.getAttributeNames();
        if (! attrNames.isEmpty()) {
            Iterator<String> it = attrNames.iterator();
            while (it.hasNext()) {
                writer.write(' ');
                String key = it.next();
                String value = element.getStringAttribute(key);
                writer.write(key);
                writer.write('=');
                writer.write('"');
                writeEncoded(writer, value);
                writer.write('"');
            }
        }
        if ((element.getContent() != null) && (!element.getContent().isEmpty())) {
            writer.write('>');
            writeEncoded(writer, element.getContent());
            writer.write('<');
            writer.write('/');
            writer.write(element.getName());
            writer.write('>');
        } else if (element.getChildrenCount() == 0) {
            writer.write('/');
            writer.write('>');
        } else {
            writer.write('>');
            Iterator<XmlElement> it = element.getChildren().iterator();
            while (it.hasNext()) {
                XmlElement child = it.next();
                write(writer, child, standalone, indent, level + 1);
            }
            
            if (standalone || indent != 0) {
                writer.append(LINE_SEPARATOR);
            }
            if (level != 0) {
                for (int i = indent * level; i > 0; i--) {
                    writer.append(' ');
                }
            }
            
            writer.write('<');
            writer.write('/');
            writer.write(element.getName());
            writer.write('>');
        }
    }

    /**
     * Writes a string encoded to a writer.
     *
     * @param writer
     *     The writer to write the XML data to.
     * @param str
     *     The string to write encoded.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>writer != null</code>
     *     <li><code>writer</code> is not closed
     *     <li><code>str != null</code>
     * </ul></dd></dl>
     */
    protected static void writeEncoded(Writer writer, String str) throws IOException {
        for (int i = 0; i < str.length(); i += 1) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<':
                    writer.write('&');
                    writer.write('l');
                    writer.write('t');
                    writer.write(';');
                    break;
                case '>':
                    writer.write('&');
                    writer.write('g');
                    writer.write('t');
                    writer.write(';');
                    break;
                case '&':
                    writer.write('&');
                    writer.write('a');
                    writer.write('m');
                    writer.write('p');
                    writer.write(';');
                    break;
                case '"':
                    writer.write('&');
                    writer.write('q');
                    writer.write('u');
                    writer.write('o');
                    writer.write('t');
                    writer.write(';');
                    break;
                case '\'':
                    writer.write('&');
                    writer.write('a');
                    writer.write('p');
                    writer.write('o');
                    writer.write('s');
                    writer.write(';');
                    break;
                default:
                    writer.write(ch);
            }
        }
    }
    
    /**
     * Writes the XML element to a string.
     *
     * @see nanoxml.XmlElement#write(java.io.Writer) write(Writer)
     */
    @Override
    public String toString() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            write(writer, 4);
            writer.flush();
            return new String(out.toByteArray());
        } catch (IOException e) {
            // Java exception handling suxx
            return super.toString();
        }
    }
    
    /**
     * Creates a new similar XML element.
     * <P>
     * You should override this method when subclassing XmlElement.
     */
    protected XmlElement createAnotherElement() {
        return new XmlElement(entities, ignoreWhitespace, true);
    }
    
    /**
     * Scans an identifier from the current reader.
     * The scanned identifier is appended to <code>result</code>.
     *
     * @param result
     *     The buffer in which the scanned identifier will be put.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     *     <li>The next character read from the reader is a valid first
     *         character of an XML identifier.
     * </ul></dd></dl>
     *
     * <dl><dt><b>Postconditions:</b></dt><dd>
     * <ul><li>The next character read from the reader won't be an identifier
     *         character.
     * </ul></dd></dl><dl>
     */
    protected void scanIdentifier(StringBuilder result) throws IOException {
        for (;;) {
            char ch = this.readChar();
            if (((ch < 'A') || (ch > 'Z')) && ((ch < 'a') || (ch > 'z'))
                    && ((ch < '0') || (ch > '9')) && (ch != '_') && (ch != '.')
                    && (ch != ':') && (ch != '-') && (ch <= '\u007E')) {
                this.unreadChar(ch);
                return;
            }
            result.append(ch);
        }
    }

    /**
     * This method scans an identifier from the current reader.
     *
     * @return the next character following the whitespace.
     */
    protected char scanWhitespace() throws IOException {
        for (;;) {
            char ch = this.readChar();
            switch (ch) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    break;
                default:
                    return ch;
            }
        }
    }

    /**
     * This method scans an identifier from the current reader.
     * The scanned whitespace is appended to <code>result</code>.
     *
     * @return the next character following the whitespace.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>result != null</code>
     * </ul></dd></dl>
     */
    protected char scanWhitespace(StringBuilder buf) throws IOException {
        for (;;) {
            char ch = this.readChar();
            switch (ch) {
                case ' ':
                case '\t':
                case '\n':
                    buf.append(ch);
                    break;
                case '\r':
                    break;
                default:
                    return ch;
            }
        }
    }

    /**
     * This method scans a delimited string from the current reader.
     * The scanned string without delimiters is appended to
     * <code>string</code>.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>string != null</code>
     *     <li>the next char read is the string delimiter
     * </ul></dd></dl>
     */
    protected void scanString(StringBuilder buf) throws IOException {
        char delimiter = this.readChar();
        if ((delimiter != '\'') && (delimiter != '"')) {
            throw this.expectedInput("' or \"");
        }
        for (;;) {
            char ch = this.readChar();
            if (ch == delimiter) {
                return;
            } else if (ch == '&') {
                this.resolveEntity(buf);
            } else {
                buf.append(ch);
            }
        }
    }

    /**
     * Scans a #PCDATA element. CDATA sections and entities are resolved.
     * The next &lt; char is skipped.
     * The scanned data is appended to <code>data</code>.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>data != null</code>
     * </ul></dd></dl>
     */
    protected void scanPCData(StringBuilder buf) throws IOException {
        for (;;) {
            char ch = this.readChar();
            if (ch == '<') {
                ch = this.readChar();
                if (ch == '!') {
                    this.checkCDATA(buf);
                } else {
                    this.unreadChar(ch);
                    return;
                }
            } else if (ch == '&') {
                this.resolveEntity(buf);
            } else {
                buf.append(ch);
            }
        }
    }

    /**
     * Scans a special tag and if the tag is a CDATA section, append its
     * content to <code>buf</code>.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>buf != null</code>
     *     <li>The first &lt; has already been read.
     * </ul></dd></dl>
     */
    protected boolean checkCDATA(StringBuilder buf) throws IOException {
        char ch = this.readChar();
        if (ch != '[') {
            this.unreadChar(ch);
            this.skipSpecialTag(0);
            return false;
        } else if (!this.checkLiteral("CDATA[")) {
            this.skipSpecialTag(1); // one [ has already been read
            return false;
        } else {
            int delimiterCharsSkipped = 0;
            while (delimiterCharsSkipped < 3) {
                ch = this.readChar();
                switch (ch) {
                    case ']':
                        if (delimiterCharsSkipped < 2) {
                            delimiterCharsSkipped += 1;
                        } else {
                            buf.append(']');
                            buf.append(']');
                            delimiterCharsSkipped = 0;
                        }
                        break;
                    case '>':
                        if (delimiterCharsSkipped < 2) {
                            for (int i = 0; i < delimiterCharsSkipped; i++) {
                                buf.append(']');
                            }
                            delimiterCharsSkipped = 0;
                            buf.append('>');
                        } else {
                            delimiterCharsSkipped = 3;
                        }
                        break;
                    default:
                        for (int i = 0; i < delimiterCharsSkipped; i += 1) {
                            buf.append(']');
                        }
                        buf.append(ch);
                        delimiterCharsSkipped = 0;
                }
            }
            return true;
        }
    }

    /**
     * Skips a comment.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt;!-- has already been read.
     * </ul></dd></dl>
     */
    protected void skipComment() throws IOException {
        int dashesToRead = 2;
        while (dashesToRead > 0) {
            char ch = this.readChar();
            if (ch == '-') {
                dashesToRead -= 1;
            } else {
                dashesToRead = 2;
            }
        }
        if (this.readChar() != '>') {
            throw this.expectedInput(">");
        }
    }

    /**
     * Skips a special tag or comment.
     *
     * @param bracketLevel The number of open square brackets ([) that have
     *                     already been read.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt;! has already been read.
     *     <li><code>bracketLevel >= 0</code>
     * </ul></dd></dl>
     */
    protected void skipSpecialTag(int bracketLevel) throws IOException {
        int tagLevel = 1; // <
        char stringDelimiter = '\0';
        if (bracketLevel == 0) {
            char ch = this.readChar();
            if (ch == '[') {
                bracketLevel += 1;
            } else if (ch == '-') {
                ch = this.readChar();
                if (ch == '[') {
                    bracketLevel += 1;
                } else if (ch == ']') {
                    bracketLevel -= 1;
                } else if (ch == '-') {
                    this.skipComment();
                    return;
                }
            }
        }
        while (tagLevel > 0) {
            char ch = this.readChar();
            if (stringDelimiter == '\0') {
                if ((ch == '"') || (ch == '\'')) {
                    stringDelimiter = ch;
                } else if (bracketLevel <= 0) {
                    if (ch == '<') {
                        tagLevel += 1;
                    } else if (ch == '>') {
                        tagLevel -= 1;
                    }
                }
                if (ch == '[') {
                    bracketLevel += 1;
                } else if (ch == ']') {
                    bracketLevel -= 1;
                }
            } else {
                if (ch == stringDelimiter) {
                    stringDelimiter = '\0';
                }
            }
        }
    }

    /**
     * Scans the data for literal text.
     * Scanning stops when a character does not match or after the complete
     * text has been checked, whichever comes first.
     *
     * @param literal the literal to check.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>literal != null</code>
     * </ul></dd></dl>
     */
    protected boolean checkLiteral(String literal) throws IOException {
        int length = literal.length();
        for (int i = 0; i < length; i += 1) {
            if (this.readChar() != literal.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads a character from a reader.
     */
    protected char readChar() throws IOException {
        if (this.charReadTooMuch != '\0') {
            char ch = this.charReadTooMuch;
            this.charReadTooMuch = '\0';
            return ch;
        } else {
            int i = this.reader.read();
            if (i < 0) {
                throw this.unexpectedEndOfData();
            } else if (i == 10) {
                this.parserLineNr += 1;
                return '\n';
            } else {
                return (char) i;
            }
        }
    }

    /**
     * Scans an XML element.
     *
     * @param elt The element that will contain the result.
     * 
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &lt; has already been read.
     *     <li><code>elt != null</code>
     * </ul></dd></dl>
     */
    protected void scanElement(XmlElement elt)  throws IOException {
        StringBuilder buf = new StringBuilder();
        this.scanIdentifier(buf);
        String eltName = buf.toString();
        elt.setName(eltName);
        char ch = this.scanWhitespace();
        while ((ch != '>') && (ch != '/')) {
            buf.setLength(0);
            this.unreadChar(ch);
            this.scanIdentifier(buf);
            String key = buf.toString();
            ch = this.scanWhitespace();
            if (ch != '=') {
                throw this.expectedInput("=");
            }
            this.unreadChar(this.scanWhitespace());
            buf.setLength(0);
            this.scanString(buf);
            elt.setAttribute(key, buf);
            ch = this.scanWhitespace();
        }
        if (ch == '/') {
            ch = this.readChar();
            if (ch != '>') {
                throw this.expectedInput(">");
            }
            return;
        }
        buf.setLength(0);
        ch = this.scanWhitespace(buf);
        if (ch != '<') {
            this.unreadChar(ch);
            this.scanPCData(buf);
        } else {
            for (;;) {
                ch = this.readChar();
                if (ch == '!') {
                    if (this.checkCDATA(buf)) {
                        this.scanPCData(buf);
                        break;
                    } else {
                        ch = this.scanWhitespace(buf);
                        if (ch != '<') {
                            this.unreadChar(ch);
                            this.scanPCData(buf);
                            break;
                        }
                    }
                } else {
                    if ((ch != '/') || this.ignoreWhitespace) {
                        buf.setLength(0);
                    }
                    if (ch == '/') {
                        this.unreadChar(ch);
                    }
                    break;
                }
            }
        }
        if (buf.length() == 0) {
            while (ch != '/') {
                if (ch == '!') {
                    ch = this.readChar();
                    if (ch != '-') {
                        throw this.expectedInput("Comment or Element");
                    }
                    ch = this.readChar();
                    if (ch != '-') {
                        throw this.expectedInput("Comment or Element");
                    }
                    this.skipComment();
                } else {
                    this.unreadChar(ch);
                    XmlElement child = this.createAnotherElement();
                    this.scanElement(child);
                    elt.addChild(child);
                }
                ch = this.scanWhitespace();
                if (ch != '<') {
                    throw this.expectedInput("<");
                }
                ch = this.readChar();
            }
            this.unreadChar(ch);
        } else {
            if (this.ignoreWhitespace) {
                elt.setContent(buf.toString().trim());
            } else {
                elt.setContent(buf.toString());
            }
        }
        ch = this.readChar();
        if (ch != '/') {
            throw this.expectedInput("/");
        }
        this.unreadChar(this.scanWhitespace());
        if (!this.checkLiteral(eltName)) {
            throw this.expectedInput(eltName);
        }
        if (this.scanWhitespace() != '>') {
            throw this.expectedInput(">");
        }
    }

    /**
     * Resolves an entity. The name of the entity is read from the reader.
     * The value of the entity is appended to <code>buf</code>.
     *
     * @param buf Where to put the entity value.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The first &amp; has already been read.
     *     <li><code>buf != null</code>
     * </ul></dd></dl>
     */
    protected void resolveEntity(StringBuilder buf) throws IOException {
        char ch = '\0';
        StringBuilder keyBuf = new StringBuilder();
        for (;;) {
            ch = this.readChar();
            if (ch == ';') {
                break;
            }
            keyBuf.append(ch);
        }
        String key = keyBuf.toString();
        if (key.charAt(0) == '#') {
            try {
                if (key.charAt(1) == 'x') {
                    ch = (char) Integer.parseInt(key.substring(2), 16);
                } else {
                    ch = (char) Integer.parseInt(key.substring(1), 10);
                }
            } catch (NumberFormatException e) {
                throw this.unknownEntity(key);
            }
            buf.append(ch);
        } else {
            char[] value = this.entities.get(key);
            if (value == null) {
                throw this.unknownEntity(key);
            }
            buf.append(value);
        }
    }

    /**
     * Pushes a character back to the read-back buffer.
     *
     * @param ch The character to push back.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li>The read-back buffer is empty.
     *     <li><code>ch != '\0'</code>
     * </ul></dd></dl>
     */
    protected void unreadChar(char ch) {
        this.charReadTooMuch = ch;
    }

    /**
     * Creates a parse exception for when an invalid valueset is given to
     * a method.
     *
     * @param name The name of the entity.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     * </ul></dd></dl>
     */
    protected XmlParseException invalidValueSet(String name) {
        String msg = "Invalid value set (entity name = \"" + name + "\")";
        return new XmlParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when an invalid value is given to a
     * method.
     *
     * @param name  The name of the entity.
     * @param value The value of the entity.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>value != null</code>
     * </ul></dd></dl>
     */
    protected XmlParseException invalidValue(String name, String value) {
        String msg = "Attribute \"" + name + "\" does not contain a valid "
                + "value (\"" + value + "\")";
        return new XmlParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when the end of the data input has been
     * reached.
     */
    protected XmlParseException unexpectedEndOfData() {
        String msg = "Unexpected end of data reached";
        return new XmlParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when a syntax error occured.
     *
     * @param context The context in which the error occured.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>context != null</code>
     *     <li><code>context.length() &gt; 0</code>
     * </ul></dd></dl>
     */
    protected XmlParseException syntaxError(String context) {
        String msg = "Syntax error while parsing " + context;
        return new XmlParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when the next character read is not
     * the character that was expected.
     *
     * @param charSet The set of characters (in human readable form) that was
     *                expected.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>charSet != null</code>
     *     <li><code>charSet.length() &gt; 0</code>
     * </ul></dd></dl>
     */
    protected XmlParseException expectedInput(String charSet) {
        String msg = "Expected: " + charSet;
        return new XmlParseException(this.getName(), this.parserLineNr, msg);
    }

    /**
     * Creates a parse exception for when an entity could not be resolved.
     *
     * @param name The name of the entity.
     *
     * </dl><dl><dt><b>Preconditions:</b></dt><dd>
     * <ul><li><code>name != null</code>
     *     <li><code>name.length() &gt; 0</code>
     * </ul></dd></dl>
     */
    protected XmlParseException unknownEntity(String name) {
        String msg = "Unknown or invalid entity: &" + name + ";";
        return new XmlParseException(this.getName(), this.parserLineNr, msg);
    }
}
