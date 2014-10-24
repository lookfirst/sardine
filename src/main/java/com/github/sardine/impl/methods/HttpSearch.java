/*
 * copyright(c) 2014 SAS Institute, Cary NC 27513 Created on Oct 22, 2014
 */
package com.github.sardine.impl.methods;

import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * @author <A HREF="mailto:Gary.Williams@sas.com">Gary Williams</A>
 */
public class HttpSearch
	extends HttpEntityEnclosingRequestBase
{
	public static final String METHOD_NAME = "SEARCH";
	
	public HttpSearch(final String uri)
	{
		this(URI.create(uri));
	}
	
	public HttpSearch(final URI uri)
	{
		this.setURI(uri);
		this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
	}
	
	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}
}
