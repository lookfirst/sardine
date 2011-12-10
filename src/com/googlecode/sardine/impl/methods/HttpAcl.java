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

package com.googlecode.sardine.impl.methods;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.protocol.HTTP;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>ACL</code> requests.
 *
 * @version $Id: HttpPropFind.java 290 2011-07-04 17:22:05Z latchkey $
 */
public class HttpAcl extends HttpEntityEnclosingRequestBase
{
	public static final String METHOD_NAME = "ACL";

	public HttpAcl(final String uri)
	{
		this(URI.create(uri));
	}

	/**
	 * @param uri The resource
	 */
	public HttpAcl(final URI uri)
	{
		this.setURI(uri);
		this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml" + HTTP.CHARSET_PARAM + HTTP.UTF_8.toLowerCase());
	}

	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}
}
