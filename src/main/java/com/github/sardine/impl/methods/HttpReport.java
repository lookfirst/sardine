package com.github.sardine.impl.methods;

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.HttpHeaders;


public class HttpReport extends HttpUriRequestBase {
    public static final String METHOD_NAME = "REPORT";

    public HttpReport(String uri) {
        this(URI.create(uri));
    }

    /**
     * Sets the <code>Depth</code> request header to <code>0</code>, meaning the report applies to the resource only.
     *
     * @param uri The resource
     */
    public HttpReport(URI uri) {
        super(METHOD_NAME, uri);
        this.setDepth("0");
        this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

    /**
     * A client may submit a Depth header with a value of "0", "1", or "infinity" with a
     * {@link com.github.sardine.model.Propfind} on a collection resource with internal member URIs.
     *
     * @param depth <code>"0"</code>, <code>"1"</code> or <code>"infinity"</code>.
     */
    public HttpReport setDepth(String depth) {
        this.setHeader(HttpHeaders.DEPTH, depth);
        return this;
    }
}
