package com.googlecode.sardine.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 *   &lt;D:owner> 
          &lt;D:href>http://www.example.com/acl/users/gstein&lt;/D:href>        
        &lt;/D:owner> 
 * 
 * 
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "")
@XmlRootElement(name = "acl")
public class Acl {
	private List<Ace> ace;

	public List<Ace> getAce() {
		return ace;
	}

	public void setAce(List<Ace> ace) {
		this.ace = ace;
	}
}
