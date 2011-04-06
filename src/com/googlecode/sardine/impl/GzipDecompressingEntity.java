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

package com.googlecode.sardine.impl;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Simple wrapper for decompressing gzipped {@link InputStream}s on the fly.
 *
 * @version $Id$
 */
public final class GzipDecompressingEntity extends HttpEntityWrapper
{

	public GzipDecompressingEntity(final HttpEntity entity)
	{
		super(entity);
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException
	{
		final InputStream wrappedin = wrappedEntity.getContent();
		return new GZIPInputStream(wrappedin);
	}

	@Override
	public long getContentLength()
	{
		return -1;
	}
}
