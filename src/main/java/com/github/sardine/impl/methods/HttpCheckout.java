package com.github.sardine.impl.methods;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>CHECKOUT</code> requests.
 */
public class HttpCheckout extends HttpRequestBase {

    public static final String METHOD_NAME = "CHECKOUT";

    public HttpCheckout(String uri) {
        this(URI.create(uri));
    }

    public HttpCheckout(URI uri) {
        this.setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
