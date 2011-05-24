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

import com.googlecode.sardine.Version;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.net.ProxySelector;

/**
 * @version $Id:$
 */
public class HttpClientUtils
{
	public static AbstractHttpClient createDefaultClient()
	{
		return createDefaultClient(null);
	}

	public static AbstractHttpClient createDefaultClient(ProxySelector selector)
	{
		SchemeRegistry schemeRegistry = createDefaultSchemeRegistry();
		ClientConnectionManager cm = createDefaultConnectionManager(schemeRegistry);
		HttpParams params = createDefaultHttpParams();
		AbstractHttpClient client = new DefaultHttpClient(cm, params);
		client.setRoutePlanner(createDefaultRoutePlanner(schemeRegistry, selector));
		return client;
	}


	/**
	 * Creates default params setting the user agent.
	 *
	 * @return Basic HTTP parameters with a custom user agent
	 */
	protected static HttpParams createDefaultHttpParams()
	{
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params, "Sardine/" + Version.getSpecification());
		// Only selectively enable this for PUT but not all entity enclosing methods
		HttpProtocolParams.setUseExpectContinue(params, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);

		HttpConnectionParams.setTcpNoDelay(params, true);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		return params;
	}

	/**
	 * Creates a new {@link org.apache.http.conn.scheme.SchemeRegistry} for default ports
	 * with socket factories.
	 *
	 * @return a new {@link org.apache.http.conn.scheme.SchemeRegistry}.
	 */
	protected static SchemeRegistry createDefaultSchemeRegistry()
	{
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", 80, createDefaultSocketFactory()));
		registry.register(new Scheme("https", 443, createDefaultSecureSocketFactory()));
		return registry;
	}

	/**
	 * @return Default socket factory
	 */
	protected static PlainSocketFactory createDefaultSocketFactory()
	{
		return PlainSocketFactory.getSocketFactory();
	}

	/**
	 * @return Default SSL socket factory
	 */
	protected static SSLSocketFactory createDefaultSecureSocketFactory()
	{
		return SSLSocketFactory.getSocketFactory();
	}

	/**
	 * Use fail fast connection manager when connections are not released properly.
	 *
	 * @param schemeRegistry Protocol registry
	 * @return Default connection manager
	 */
	protected static ClientConnectionManager createDefaultConnectionManager(SchemeRegistry schemeRegistry)
	{
		return new SingleClientConnManager(schemeRegistry);
	}

	/**
	 * Override to provide proxy configuration
	 *
	 * @param schemeRegistry Protocol registry
	 * @param selector	   Proxy configuration
	 * @return ProxySelectorRoutePlanner configured with schemeRegistry and selector
	 */
	protected static HttpRoutePlanner createDefaultRoutePlanner(SchemeRegistry schemeRegistry, ProxySelector selector)
	{
		return new ProxySelectorRoutePlanner(schemeRegistry, selector);
	}
}
