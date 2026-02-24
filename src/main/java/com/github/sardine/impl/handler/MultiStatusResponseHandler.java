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

import java.io.IOException;
import java.io.InputStream;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.message.StatusLine;

import com.github.sardine.impl.SardineException;
import com.github.sardine.model.Multistatus;
import com.github.sardine.util.SardineUtil;

/**
 * {@link org.apache.hc.core5.http.io.HttpClientResponseHandler} which returns the {@link Multistatus} response of
 * a {@link com.github.sardine.impl.methods.HttpPropFind} request.
 *
 * @author mirko
 */
public class MultiStatusResponseHandler extends ValidatingResponseHandler<Multistatus>
{
	@Override
	public Multistatus handleResponse(ClassicHttpResponse response) throws IOException
	{
		super.validateResponse(response);

		// Process the response from the server.
		HttpEntity entity = response.getEntity();
		StatusLine statusLine = new StatusLine(response);
		if (entity == null)
		{
			throw new SardineException("No entity found in response", statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        try
        {
            return this.getMultistatus(entity.getContent());
        }
        catch(IOException e) {
            // JAXB error unmarshalling response stream
			final SardineException sardineException = new SardineException(e.getMessage(), statusLine.getStatusCode(), statusLine.getReasonPhrase());
			sardineException.initCause(e);
			throw sardineException;
        }
	}

	/**
	 * Helper method for getting the Multistatus response processor.
	 *
	 * @param stream The input to read the status
	 * @return Multistatus element parsed from the stream
	 * @throws IOException When there is a JAXB error
	 */
	protected Multistatus getMultistatus(InputStream stream)
			throws IOException
	{
		return SardineUtil.unmarshal(stream);
	}
}
