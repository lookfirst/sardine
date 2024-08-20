///*
// * Copyright 2009-2011 Jon Stevens et al.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.github.sardine.impl;
//
//import java.net.URI;
//
//import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
//import org.apache.hc.client5.http.protocol.RedirectStrategy;
//import org.apache.hc.core5.http.HttpRequest;
//import org.apache.hc.core5.http.HttpResponse;
//import org.apache.hc.core5.http.ProtocolException;
//import org.apache.hc.core5.http.protocol.HttpContext;
//
//import com.github.sardine.impl.methods.HttpPropFind;
//
//public class SardineRedirectStrategy implements RedirectStrategy {
//
//    @Override
//    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
//            throws ProtocolException {
//        String method = request.getMethod();
//        if (DefaultRedirectStrategy.INSTANCE.isRedirected(request, response, context)) {
//            return true;
//        }
//        return method.equalsIgnoreCase(HttpPropFind.METHOD_NAME);
//    }
//
//    @Override
//    public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) {
//        try {
//            return DefaultRedirectStrategy.INSTANCE.getLocationURI(request, response, context);
//        } catch (Exception e) {
//            throw new RuntimeException();
//        }
//    }
//
//}
