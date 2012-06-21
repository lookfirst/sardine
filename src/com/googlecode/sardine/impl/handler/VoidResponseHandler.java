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

import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * {@link org.apache.http.client.ResponseHandler} which just executes the request and checks the answer is
 * in the valid range of {@link ValidatingResponseHandler#validateResponse(org.apache.http.HttpResponse)}.
 *
 * @author mirko
 * @version $Id$
 */
public class VoidResponseHandler extends ValidatingResponseHandler<Void>
{
	@Override
	public Void handleResponse(HttpResponse response) throws IOException
	{
		this.validateResponse(response);
		return null;
	}
}
