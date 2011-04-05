package com.googlecode.sardine.impl;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * This {@link HttpRequestInterceptor} adds an Accept-Encoding of gzip.
 */
public final class GzipSupportRequestInterceptor implements HttpRequestInterceptor
{

	/**
	 * @param request
	 * @param context
	 * @throws HttpException
	 * @throws IOException
	 */
	public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException
	{
		if (!request.containsHeader(HttpHeaders.ACCEPT_ENCODING))
		{
			request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
		}
	}
}
