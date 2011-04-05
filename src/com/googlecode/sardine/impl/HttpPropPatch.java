package com.googlecode.sardine.impl;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * Simple class for making proppatch a bit easier to deal with.
 */
public class HttpPropPatch extends HttpEntityEnclosingRequestBase {
    public HttpPropPatch(String url) {
        this.setURI(URI.create(url));
        this.setHeader("Content-Type", "text/xml");
    }

    @Override
    public String getMethod() {
        return "PROPPATCH";
    }
}
