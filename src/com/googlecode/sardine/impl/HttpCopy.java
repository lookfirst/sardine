package com.googlecode.sardine.impl;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import com.googlecode.sardine.util.SardineException;

import java.net.URI;

/**
 * Simple class for making copy a bit easier to deal with. Assumes Overwrite = T.
 */
public class HttpCopy extends HttpEntityEnclosingRequestBase {
    public HttpCopy(String sourceUrl, String destinationUrl) throws SardineException {
        this.setHeader("Destination", destinationUrl);
        this.setHeader("Overwrite", "T");
        this.setURI(URI.create(sourceUrl));

        if(sourceUrl.endsWith("/") && !destinationUrl.endsWith("/")) {
            throw new SardineException("Destinationurl must end with a /", destinationUrl);
        }
    }

    @Override
    public String getMethod() {
        return "COPY";
    }
}
