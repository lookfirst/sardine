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

package com.googlecode.sardine.util;

import java.io.IOException;

/**
 * Specialized type of exception for Sardine so
 * that it is easy to get the error information from it.
 *
 * @author jonstevens
 */
public class SardineException extends IOException
{
	private int statusCode;
	private String responsePhrase;

	/**
	 * @param cause
	 */
	public SardineException(Throwable cause)
	{
		this(cause.getMessage(), cause);
	}

	/**
	 * @param msg
	 */
	public SardineException(String msg)
	{
		this(msg, -1, null, null);
	}

	/**
	 * @param msg
	 * @param cause
	 */
	public SardineException(String msg, Throwable cause)
	{
		this(msg, -1, null, cause);
	}

	/**
	 * @param msg
	 * @param statusCode
	 * @param responsePhrase
	 */
	public SardineException(String msg, int statusCode, String responsePhrase)
	{
		this(msg, statusCode, responsePhrase, null);
	}

	/**
	 * @param msg
	 * @param statusCode
	 * @param responsePhrase
	 * @param cause
	 */
	public SardineException(String msg, int statusCode, String responsePhrase, Throwable cause)
	{
		super(msg, cause);
		this.statusCode = statusCode;
		this.responsePhrase = responsePhrase;
	}

	/**
	 * The http client status code.
	 *
	 * @return A status code of -1 if not known.
	 */
	public int getStatusCode()
	{
		return this.statusCode;
	}

	/**
	 * The http client response phrase.
	 *
	 * @return Null if not known.
	 */
	public String getResponsePhrase()
	{
		return this.responsePhrase;
	}
}
