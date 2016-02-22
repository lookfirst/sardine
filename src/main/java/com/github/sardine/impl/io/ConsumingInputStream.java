/*
 * Copyright 2009-2016 Jon Stevens et al.
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.FilterInputStream;
import java.io.IOException;

/**
 * Wrapper for the input stream, will consume the rest of the response on {@link ConsumingInputStream#close()}.
 */
public class ConsumingInputStream extends FilterInputStream
{
	private HttpEntity entity;

	/**
	 * @param response The HTTP response to read from
	 * @throws IOException          If there is a problem reading from the response
	 * @throws NullPointerException If the response has no message entity
	 */
	public ConsumingInputStream(final HttpResponse response) throws IOException
	{
		super(response.getEntity().getContent());
		this.entity = response.getEntity();
	}

	@Override
	public void close() throws IOException
	{
		EntityUtils.consume(entity);
	}

	@Override
	public int read() throws IOException
	{
		return in.read();
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return in.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		return in.read(b, off, len);
	}
}
