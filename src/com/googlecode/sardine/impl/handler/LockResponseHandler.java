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

package com.googlecode.sardine.impl.handler;

import com.googlecode.sardine.impl.SardineException;
import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.util.SardineUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id$
 */
public class LockResponseHandler extends ValidatingResponseHandler<String>
{
	public String handleResponse(HttpResponse response) throws IOException
	{
		super.validateResponse(response);

		// Process the response from the server.
		HttpEntity entity = response.getEntity();
		if (entity == null)
		{
			StatusLine statusLine = response.getStatusLine();
			throw new SardineException("No entity found in response", statusLine.getStatusCode(),
					statusLine.getReasonPhrase());
		}
		return this.getToken(entity.getContent());
	}

	/**
	 * Helper method for getting the Multistatus response processor.
	 *
	 * @param stream The input to read the status
	 * @return Multistatus element parsed from the stream
	 * @throws java.io.IOException When there is a JAXB error
	 */
	protected String getToken(InputStream stream)
			throws IOException
	{
		Prop prop = SardineUtil.unmarshal(stream);
		return prop.getLockdiscovery().getActivelock().iterator().next().getLocktoken().getHref().iterator().next();
	}
}
