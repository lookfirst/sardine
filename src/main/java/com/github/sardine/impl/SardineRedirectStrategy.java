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

import com.github.sardine.impl.methods.HttpPropFind;
import com.github.sardine.impl.methods.HttpReport;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class SardineRedirectStrategy extends DefaultRedirectStrategy {

    @Override
    protected boolean isRedirectable(String method) {
        if (super.isRedirectable(method))
        {
            return true;
        }
        return method.equalsIgnoreCase(HttpPropFind.METHOD_NAME);
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException
    {
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpPropFind.METHOD_NAME))
        {
            HttpPropFind propfind = new HttpPropFind(this.getLocationURI(request, response, context));
            Header depth = request.getFirstHeader("Depth");
            if (depth != null && depth.getValue() != null)
            {
                propfind.setDepth(depth.getValue());
            }
            return this.copyEntity(propfind, request);
        }
        else if (method.equalsIgnoreCase(HttpReport.METHOD_NAME))
        {
            HttpReport report = new HttpReport(this.getLocationURI(request, response, context));
            Header depth = request.getFirstHeader("Depth");
            if (depth != null && depth.getValue() != null)
            {
                report.setDepth(depth.getValue());
            }
            return this.copyEntity(report, request);
        }
        return super.getRedirect(request, response, context);
    }

    private HttpUriRequest copyEntity(final HttpEntityEnclosingRequestBase redirect, final HttpRequest original) {
        if (original instanceof HttpEntityEnclosingRequest)
        {
            redirect.setEntity(((HttpEntityEnclosingRequest) original).getEntity());
        }
        return redirect;
    }
}
