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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteCountInputStream extends FilterInputStream
{

	private Long byteCount = 0L;

	public ByteCountInputStream(final InputStream in)
	{
		super(in);
	}

	@Override
	public long skip(long n) throws IOException
	{
		final long skip = in.skip(n);
		byteCount += skip;
		return skip;
	}

	@Override
	public int read() throws IOException
	{
		final int data = in.read();
		byteCount += data == -1 ? 0 : 1;
		return data;
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		final int read = in.read(b);
		byteCount += read == -1 ? 0 : read;
		return read;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		final int read = in.read(b, off, len);
		byteCount += read == -1 ? 0 : read;
		return read;
	}

	public Long getByteCount()
	{
		return byteCount;
	}
}
