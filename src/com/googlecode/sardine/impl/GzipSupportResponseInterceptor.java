/*
 * Copyright 2009-2011 Jon Stevens et al.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.sardine.impl;

import org.apache.http.*;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * This {@link HttpResponseInterceptor} decompresses the {@link HttpEntity} of the {@link HttpResponse} on the fly when
 * the content encoding indicates it should by replacing the entity of the response.
 *
 * @version $Id$
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
