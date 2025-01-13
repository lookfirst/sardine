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

package com.github.sardine.impl.methods;

import java.net.URI;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.HttpHeaders;

/**
 *
 */
public class HttpUnlock extends HttpUriRequestBase {
    public static final String METHOD_NAME = "UNLOCK";

    /**
     * @param uri   The resource
     * @param token The Lock-Token request header is used with the UNLOCK method to identify the lock to be removed. The
     *              lock token in the Lock-Token request header must identify a lock that contains the resource
     *              identified by Request-URI as a member.
     */
    public HttpUnlock(String uri, String token) {
        this(URI.create(uri), token);
    }

    /**
     * @param uri   The resource
     * @param token The Lock-Token request header is used with the UNLOCK method to identify the lock to be removed. The
     *              lock token in the Lock-Token request header must identify a lock that contains the resource
     *              identified by Request-URI as a member.
     */
    public HttpUnlock(URI uri, String token) {
        super(METHOD_NAME, uri);
        this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
        this.setHeader("Lock-Token", "<" + token + ">");
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
