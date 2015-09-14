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

package com.github.sardine.impl.methods;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * Simple class for making WebDAV <code>PROPFIND</code> requests.
 *
 */
public class HttpPropFind extends HttpEntityEnclosingRequestBase
{
	public static final String METHOD_NAME = "PROPFIND";

	public HttpPropFind(final String uri)
	{
		this(URI.create(uri));
	}

	/**
	 * Sets the <code>Depth</code> request header to <code>1</code>, meaning the
	 * request applies to the resource and its children.
	 *
	 * @param uri The resource
	 */
	public HttpPropFind(final URI uri)
	{
		this.setDepth("1");
		this.setURI(uri);
		this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
	}

	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}

	/**
	 * A client may submit a Depth header with a value of "0", "1", or "infinity" with
	 * a {@link com.github.sardine.model.Propfind} on a collection resource with internal member URIs.
	 *
	 * @param depth <code>"0"</code>, <code>"1"</code> or <code>"infinity"</code>.
	 */
	public HttpPropFind setDepth(String depth)
	{
		this.setHeader(HttpHeaders.DEPTH, depth);
		return this;
	}
}
