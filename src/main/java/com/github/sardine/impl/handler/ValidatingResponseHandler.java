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

package com.github.sardine.impl.handler;


import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.StatusLine;

import com.github.sardine.impl.SardineException;

/**
 * Basic response handler which takes an url for documentation.
 *
 * @param <T> return type of {@link HttpClientResponseHandler#handleResponse(ClassicHttpResponse)} (HttpResponse)}.
 * @author mirko
 */
public abstract class ValidatingResponseHandler<T> implements HttpClientResponseHandler<T>
{
	/**
	 * Checks the response for a statuscode between {@link org.apache.hc.core5.http.HttpStatus#SC_OK} and {@link org.apache.hc.core5.http.HttpStatus#SC_MULTIPLE_CHOICES}
	 * and throws an {@link com.github.sardine.impl.SardineException} otherwise.
	 *
	 * @param response to check
	 * @throws SardineException when the status code is not acceptable.
	 */
	protected void validateResponse(ClassicHttpResponse response) throws SardineException
	{
		StatusLine statusLine = new StatusLine(response);
		int statusCode = statusLine.getStatusCode();
		if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES)
		{
			return;
		}
		throw new SardineException("Unexpected response", statusLine.getStatusCode(), statusLine.getReasonPhrase());
	}
}
