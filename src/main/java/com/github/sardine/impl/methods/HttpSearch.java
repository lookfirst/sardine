/*
 * copyright(c) 2014 SAS Institute, Cary NC 27513 Created on Oct 22, 2014
 */
package com.github.sardine.impl.methods;

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.HttpHeaders;

/**
 * @author <A HREF="mailto:Gary.Williams@sas.com">Gary Williams</A>
 */
public class HttpSearch extends HttpUriRequestBase {
    public static final String METHOD_NAME = "SEARCH";

    public HttpSearch(String uri) {
        this(URI.create(uri));
    }

    public HttpSearch(URI uri) {
        super(METHOD_NAME, uri);
        this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
