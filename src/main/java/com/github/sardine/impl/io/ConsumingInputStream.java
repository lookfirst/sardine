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

package com.github.sardine.impl.io;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Wrapper for the input stream, will consume the rest of the response on {@link ConsumingInputStream#close()}.
 *
 * @author mirko
 */
public class ConsumingInputStream extends ContentLengthInputStream
{
	private HttpResponse response;

	/**
	 * @param response The HTTP response to read from
	 * @throws IOException		  If there is a problem reading from the response
	 * @throws NullPointerException If the response has no message entity
	 */
	public ConsumingInputStream(final HttpResponse response) throws IOException
	{
        super(response.getEntity().getContent(), response.getEntity().getContentLength());
		this.response = response;
	}

	@Override
	public void close() throws IOException
	{
		EntityUtils.consume(response.getEntity());
	}
}
