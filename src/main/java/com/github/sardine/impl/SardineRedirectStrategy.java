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

package com.github.sardine.impl;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

import com.github.sardine.impl.methods.HttpAcl;
import com.github.sardine.impl.methods.HttpLock;
import com.github.sardine.impl.methods.HttpPropFind;

/**
* @version $Id:$
*/
public class SardineRedirectStrategy extends DefaultRedirectStrategy {
    @Override
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException
    {
        int statusCode = response.getStatusLine().getStatusCode();
        String method = request.getRequestLine().getMethod();
        Header locationHeader = response.getFirstHeader("location");
        switch (statusCode)
        {
            case HttpStatus.SC_MOVED_TEMPORARILY:
                return (method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpHead.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpLock.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpAcl.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpPropFind.METHOD_NAME)) && (locationHeader != null);
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                return method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpHead.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpLock.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpAcl.METHOD_NAME)
                        || method.equalsIgnoreCase(HttpPropFind.METHOD_NAME);
            case HttpStatus.SC_SEE_OTHER:
                return true;
            default:
                return false;
        }
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException
    {
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpPropFind.METHOD_NAME))
        {
            return new HttpPropFind(this.getLocationURI(request, response, context));
        }
        if (method.equalsIgnoreCase(HttpLock.METHOD_NAME))
        {
            return new HttpLock(this.getLocationURI(request, response, context));
        }
        return super.getRedirect(request, response, context);
    }
}
