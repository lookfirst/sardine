package com.github.sardine.impl.methods;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>VERSION-CONTROL</code> requests.
 */
public class HttpVersionControl extends HttpRequestBase {

    public static final String METHOD_NAME = "VERSION-CONTROL";

    public HttpVersionControl(String uri) {
        this(URI.create(uri));
    }

    public HttpVersionControl(URI uri) {
        this.setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
