package com.googlecode.sardine.impl;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * Simple class for making mkcol a bit easier to deal with.
 */
public class HttpMkCol extends HttpEntityEnclosingRequestBase {
    public HttpMkCol(String url) {
        this.setURI(URI.create(url));
    }

    @Override
    public String getMethod() {
        return "MKCOL";
    }
}
