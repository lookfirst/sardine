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
 * @version $Id$
 */
public class HttpLock extends HttpEntityEnclosingRequestBase
{
	public static final String METHOD_NAME = "LOCK";

	public HttpLock(String url)
	{
		this(URI.create(url));
	}

	public HttpLock(URI url)
	{
		this.setURI(url);
		this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml" + HTTP.CHARSET_PARAM + "UTF-8");
	}

	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}

	/**
	 * The Depth header may be used with the <code>LOCK</code> method. Values other than <code>0</code> or <code>infinity</code> must not
	 * be used with the Depth header on a <code>LOCK</code> method. All resources that support the <code>LOCK</code>
	 * method must support the Depth header.
	 * <p/>
	 * If no Depth header is submitted on a <code>LOCK</code> request then the request must act as if
	 * a <code>Depth:infinity</code> had been submitted.
	 *
	 * @param depth <code>"0"</code> or <code>"infinity"</code>.
	 */
	public void setDepth(String depth)
	{
		this.setHeader(HttpHeaders.DEPTH, depth);
	}

	/**
	 * Clients may include Timeout headers in their LOCK requests. However, the server is not required to honor
	 * or even consider these requests.
	 */
	public void setTimeout(int seconds)
	{
		this.setHeader(HttpHeaders.TIMEOUT, "Second-" + seconds);
	}

	/**
	 * Desires an infinite length lock.
	 */
	public void setInfinite()
	{
		this.setHeader(HttpHeaders.TIMEOUT, "Infinite");
	}
}