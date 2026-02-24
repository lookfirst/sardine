package com.github.sardine.impl.methods;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>VERSION-CONTROL</code> requests.
 */
public class HttpVersionControl extends HttpUriRequestBase {

    public static final String METHOD_NAME = "VERSION-CONTROL";

    public HttpVersionControl(String uri) {
        this(URI.create(uri));
    }

    public HttpVersionControl(URI uri) {
        super(METHOD_NAME, uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
