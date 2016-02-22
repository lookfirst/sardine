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

import org.apache.http.HttpResponse;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContentLengthInputStream extends FilterInputStream
{

	private Long length;

	public ContentLengthInputStream(final HttpResponse response) throws IOException
	{
		super(response.getEntity().getContent());
		this.length = response.getEntity().getContentLength();
	}

	public ContentLengthInputStream(final InputStream in, final Long length)
	{
		super(in);
		this.length = length;
	}

	public Long getLength()
	{
		return length;
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