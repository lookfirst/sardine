//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.4-10/27/2009 06:09 PM(mockbuild)-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.12.23 at 06:27:19 PM PST 
//


package com.github.sardine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{DAV:}lockscope"/&gt;
 *         &lt;element ref="{DAV:}locktype"/&gt;
 *         &lt;element ref="{DAV:}owner" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "lockscope",
    "locktype",
    "owner"
})
@XmlRootElement(name = "lockinfo")
public class Lockinfo {

    @XmlElement(required = true)
    private Lockscope lockscope;
    @XmlElement(required = true)
    private Locktype locktype;
    private Owner owner;

    /**
     * Gets the value of the lockscope property.
     * 
     * @return
     *     possible object is
     *     {@link Lockscope }
     *     
     */
    public Lockscope getLockscope() {
        return lockscope;
    }

    /**
     * Sets the value of the lockscope property.
     * 
     * @param value
     *     allowed object is
     *     {@link Lockscope }
     *     
     */
    public void setLockscope(Lockscope value) {
        this.lockscope = value;
    }

    /**
     * Gets the value of the locktype property.
     * 
     * @return
     *     possible object is
     *     {@link Locktype }
     *     
     */
    public Locktype getLocktype() {
        return locktype;
    }

    /**
     * Sets the value of the locktype property.
     * 
     * @param value
     *     allowed object is
     *     {@link Locktype }
     *     
     */
    public void setLocktype(Locktype value) {
        this.locktype = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link Owner }
     *     
     */
    public Owner getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link Owner }
     *     
     */
    public void setOwner(Owner value) {
        this.owner = value;
    }

}
