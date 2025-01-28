package com.github.sardine.impl.methods;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>CHECKIN</code> requests.
 */
public class HttpCheckin extends HttpUriRequestBase {

    public static final String METHOD_NAME = "CHECKIN";

    public HttpCheckin(String uri) {
        this(URI.create(uri));
    }

    public HttpCheckin(URI uri) {
        super(METHOD_NAME, uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
