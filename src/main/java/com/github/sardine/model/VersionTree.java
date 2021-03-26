package com.github.sardine.model;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "prop"
})
@XmlRootElement(name = "version-tree")
public class VersionTree {

    @XmlElement
    protected Prop prop;

    /**
     * Gets the value of the prop property.
     *
     * @return
     *     possible object is
     *     {@link Prop }
     *
     */
    public Prop getProp() {
        return prop;
    }

    /**
     * Sets the value of the prop property.
     *
     * @param value
     *     allowed object is
     *     {@link Prop }
     *
     */
    public void setProp(Prop value) {
        this.prop = value;
    }
}
