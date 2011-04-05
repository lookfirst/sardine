package com.googlecode.sardine.impl;

import org.apache.http.*;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * This {@link HttpResponseInterceptor} decompresses the {@link HttpEntity} of the {@link HttpResponse} on the fly when
 * the content encoding indicates it should by replacing the entity of the response.
 */
public final class GzipSupportResponseInterceptor implements HttpResponseInterceptor
{
	public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException
	{
		final HttpEntity entity = response.getEntity();
		if (entity != null)
		{
			final Header ceheader = entity.getContentEncoding();
			if (ceheader != null)
			{
				HeaderElement[] codecs = ceheader.getElements();
				for (int i = 0; i < codecs.length; i++)
				{
					if (codecs[i].getName().equalsIgnoreCase("gzip"))
					{
						response.setEntity(new GzipDecompressingEntity(response.getEntity()));
						return;
					}
				}
			}
		}
	}
}
