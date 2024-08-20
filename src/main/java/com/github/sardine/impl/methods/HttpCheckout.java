package com.github.sardine.impl.methods;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>CHECKOUT</code> requests.
 */
public class HttpCheckout extends HttpUriRequestBase {

    public static final String METHOD_NAME = "CHECKOUT";

    public HttpCheckout(final String url) {
        this(URI.create(url));
    }

    public HttpCheckout(final URI uri) {
        super(METHOD_NAME, uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
