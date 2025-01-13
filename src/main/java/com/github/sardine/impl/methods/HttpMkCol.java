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

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

/**
 * Simple class for making <code>MKCOL</code> requests.
 *
 */
public class HttpMkCol extends HttpUriRequestBase
{
	public static final String METHOD_NAME = "MKCOL";

	public HttpMkCol(String uri)
	{
		this(URI.create(uri));
	}

	public HttpMkCol(URI uri)
	{
		super(METHOD_NAME, uri);
	}

	public String getMethod()
	{
		return METHOD_NAME;
	}
}
