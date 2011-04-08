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
 * Simple class for making propfind a bit easier to deal with.
 *
 * @version $Id$
 */
public class HttpPropFind extends HttpEntityEnclosingRequestBase
{
	public HttpPropFind(String url)
	{
		this.setDepth(1);
		this.setURI(URI.create(url));
		this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml" + HTTP.CHARSET_PARAM + "UTF-8");
	}

	@Override
	public String getMethod()
	{
		return "PROPFIND";
	}

	public void setDepth(int val)
	{
		this.setHeader("Depth", String.valueOf(val));
	}
}
