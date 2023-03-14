package com.github.sardine.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "privilege")
public class Privilege {
    @XmlMixed
    @XmlAnyElement(lax = true)
	private List<Object> content;

	public List<Object> getContent() {
		if (content==null)
			content = new ArrayList<Object>();
		return content;
	}

	public void setContent(List<Object> content) {
		this.content = content;
	}

}
