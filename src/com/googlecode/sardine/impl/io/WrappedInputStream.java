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

package com.googlecode.sardine.impl.io;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for the input stream, will consume the rest of the response on {@link WrappedInputStream#close()}.
 *
 * @author mirko
 * @version $Id$
 */
public class WrappedInputStream extends InputStream
{

	private final InputStream delegate;
	private final HttpResponse response;

	/**
	 * @param response The HTTP response to read from
	 * @throws IOException		  If there is a problem reading from the response
	 * @throws NullPointerException If the response has no message entity
	 */
	public WrappedInputStream(final HttpResponse response) throws IOException
	{
		this.response = response;
		final HttpEntity entity = response.getEntity();
		if (entity == null)
		{
			throw new NullPointerException();
		}
		else
		{
			this.delegate = entity.getContent();
		}
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return delegate.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		return delegate.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException
	{
		return delegate.skip(n);
	}

	@Override
	public int available() throws IOException
	{
		return delegate.available();
	}

	@Override
	public void mark(int readlimit)
	{
		delegate.mark(readlimit);
	}

	@Override
	public void reset() throws IOException
	{
		delegate.reset();
	}

	@Override
	public boolean markSupported()
	{
		return delegate.markSupported();
	}

	@Override
	public int read() throws IOException
	{
		return delegate.read();
	}

	@Override
	public void close() throws IOException
	{
		EntityUtils.consume(response.getEntity());
	}
}
