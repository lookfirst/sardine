package com.github.sardine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.</p>
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>
 *   &lt;D:owner&gt; 
 *        &lt;D:href&gt;http://www.example.com/acl/users/gstein&lt;/D:href&gt;
 *      &lt;/D:owner&gt;
 * </pre>
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
