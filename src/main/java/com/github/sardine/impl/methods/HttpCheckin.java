package com.github.sardine.impl.methods;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>CHECKIN</code> requests.
 */
public class HttpCheckin extends HttpRequestBase {

    public static final String METHOD_NAME = "CHECKIN";

    public HttpCheckin(String uri) {
        this(URI.create(uri));
    }

    public HttpCheckin(URI uri) {
        this.setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
