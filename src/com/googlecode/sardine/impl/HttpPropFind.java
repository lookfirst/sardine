package com.googlecode.sardine.impl;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * Simple class for making propfind a bit easier to deal with.
 */
public class HttpPropFind extends HttpEntityEnclosingRequestBase {
    public HttpPropFind(String url) {
        this.setDepth(1);
        this.setURI(URI.create(url));
        this.setHeader("Content-Type", "text/xml");
    }

    @Override
    public String getMethod() {
        return "PROPFIND";
    }

    public void setDepth(int val) {
        this.setHeader("Depth", String.valueOf(val));
    }
}
