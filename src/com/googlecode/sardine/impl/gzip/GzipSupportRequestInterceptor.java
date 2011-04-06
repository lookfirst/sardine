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

package com.googlecode.sardine.impl.gzip;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * This {@link HttpRequestInterceptor} adds an Accept-Encoding of gzip.
 *
 * @version $Id$
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
