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

import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

/**
 * Simple class for making move a bit easier to deal with.
 *
 * @version $Id$
 */
public class HttpMove extends HttpRequestBase
{
	public static final String METHOD_NAME = "MOVE";

	public HttpMove(URI sourceUrl, URI destinationUrl)
	{
		this.setHeader("Destination", destinationUrl.toString());
		this.setHeader("Overwrite", "T");
		this.setURI(sourceUrl);
	}

	public HttpMove(String sourceUrl, String destinationUrl)
	{
		this(URI.create(sourceUrl), URI.create(destinationUrl));
	}

	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}
}
