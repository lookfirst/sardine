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

package com.googlecode.sardine;

import com.googlecode.sardine.impl.gzip.GzipSupportRequestInterceptor;
import com.googlecode.sardine.impl.gzip.GzipSupportResponseInterceptor;
import com.googlecode.sardine.impl.handler.ExistsResponseHandler;
import com.googlecode.sardine.impl.handler.MultiStatusResponseHandler;
import com.googlecode.sardine.impl.handler.VoidResponseHandler;
import com.googlecode.sardine.impl.io.WrappedInputStream;
import com.googlecode.sardine.impl.methods.*;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Sardine interface. This is where the meat of the Sardine library lives.
 *
 * @author jonstevens
 * @version $Id$
 */
public class SardineImpl implements Sardine
{
	/** */
	private final AbstractHttpClient client;

	/**
	 * Local context with authentication cache
	 */
	private HttpContext localcontext = new BasicHttpContext();

	/**
	 * @throws SardineException
	 */
	public SardineImpl() throws SardineException
	{
		this(null, null, null, null, null);
	}

	/**
	 * @param username
	 * @param password
	 * @throws SardineException
	 */
	public SardineImpl(String username, String password) throws SardineException
	{
		this(username, password, null, null);
	}

	/**
	 * @param username
	 * @param password
	 * @param factory
	 * @param routePlanner
	 * @throws SardineException
	 */
	public SardineImpl(String username, String password, SSLSocketFactory factory,
					   HttpRoutePlanner routePlanner) throws SardineException
	{
		this(username, password, factory, routePlanner, null);
	}

	/**
	 * @param username
	 * @param password
	 * @param factory
	 * @param routePlanner
	 * @param port
	 * @throws SardineException
	 */
	public SardineImpl(String username, String password, SSLSocketFactory factory,
					   HttpRoutePlanner routePlanner, Integer port) throws SardineException
	{

		SchemeRegistry schemeRegistry = createDefaultSchemeRegistry(factory, port);
		ClientConnectionManager cm = createDefaultConnectionManager(schemeRegistry);
		HttpParams params = createDefaultHttpParams();
		client = new DefaultHttpClient(cm, params);
		setCredentials(username, password);
		// for proxy configurations
		if (routePlanner != null)
		{
			client.setRoutePlanner(routePlanner);
		}
	}

	/**
	 * @param client
	 */
	public SardineImpl(final AbstractHttpClient client)
	{
		this(client, null, null);
	}

	/**
	 * @param http
	 * @param username
	 * @param password
	 */
	public SardineImpl(final AbstractHttpClient http, String username, String password)
	{
		client = http;
		setCredentials(username, password);
	}

	private void setCredentials(String username, String password)
	{
		if (username != null)
		{
			client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
					new UsernamePasswordCredentials(username, password));
		}
	}

	/**
	 * Adds handling of GZIP compression to the client.
	 */
	public void enableCompression()
	{
		this.client.addRequestInterceptor(new GzipSupportRequestInterceptor());
		this.client.addResponseInterceptor(new GzipSupportResponseInterceptor());
	}

	/**
	 * Disable GZIP compression header.
	 */
	public void disableCompression()
	{
		this.client.removeRequestInterceptorByClass(GzipSupportRequestInterceptor.class);
		this.client.removeResponseInterceptorByClass(GzipSupportResponseInterceptor.class);
	}

	public void enablePreemptiveAuthentication(String scheme, String hostname, int port)
	{
		AuthCache authCache = new BasicAuthCache();
		// Generate Basic preemptive scheme object and stick it to the local execution context
		BasicScheme basicAuth = new BasicScheme();
		// Configure HttpClient to authenticate preemptively by prepopulating the authentication data cache.
		authCache.put(new HttpHost(hostname), basicAuth);
		authCache.put(new HttpHost(hostname, -1, scheme), basicAuth);
		authCache.put(new HttpHost(hostname, port, scheme), basicAuth);
		// Add AuthCache to the execution context
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
	}

	public void disablePreemptiveAuthentication(String scheme, String hostname, int port)
	{
		localcontext.removeAttribute(ClientContext.AUTH_CACHE);
	}

	public List<DavResource> getResources(final String url) throws SardineException
	{
		HttpPropFind propFind = new HttpPropFind(url);
		propFind.setEntity(SardineUtil.getResourcesEntity());
		Multistatus multistatus = execute(propFind, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
		List<DavResource> resources = new ArrayList<DavResource>(responses.size());
		for (final Response resp : responses)
		{
			try
			{
				resources.add(new DavResource(resp));
			}
			catch (URISyntaxException e)
			{
				// Ignore resource with invalid URI
			}
		}
		return resources;
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#setCustomProps(java.lang.String, java.util.List<java.lang.String>)
	 */
	public void setCustomProps(String url, Map<String, String> setProps, List<String> removeProps) throws SardineException
	{
		HttpPropPatch propPatch = new HttpPropPatch(url);
		propPatch.setEntity(SardineUtil.getResourcePatchEntity(setProps, removeProps));
		this.execute(propPatch, new VoidResponseHandler());
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#getInputStream(String)
	 */
	public InputStream getInputStream(String url) throws SardineException
	{
		return this.get(url);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#get(java.lang.String)
	 */
	public InputStream get(String url) throws SardineException
	{
		HttpGet get = new HttpGet(url);
		// Must use #execute without handler, otherwise the entity is consumed
		// already after the handler exits.
		HttpResponse response = execute(get);
		VoidResponseHandler handler = new VoidResponseHandler();
		try
		{
			handler.handleResponse(response);
			// Will consume the entity when the stream is closed.
			return new WrappedInputStream(response);
		}
		catch (SardineException ex)
		{
			get.abort();
			throw ex;
		}
		catch (IOException ex)
		{
			get.abort();
			throw new SardineException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, byte[])
	 */
	public void put(String url, byte[] data) throws SardineException
	{
		put(url, data, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, byte[], java.lang.String)
	 */
	public void put(String url, byte[] data, String contentType) throws SardineException
	{
		HttpPut put = new HttpPut(url);
		ByteArrayEntity entity = new ByteArrayEntity(data);
		put(put, entity, null, true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, InputStream)
	 */
	public void put(String url, InputStream dataStream) throws SardineException
	{
		put(url, dataStream, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, java.io.InputStream, java.lang.String)
	 */
	public void put(String url, InputStream dataStream, String contentType) throws SardineException
	{
		put(url, dataStream, contentType, true);
	}

	public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws SardineException
	{
		HttpPut put = new HttpPut(url);
		// A length of -1 means "go until end of stream"
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put(put, entity, contentType, expectContinue);
	}

	/**
	 * Private helper for doing the work of a put
	 */
	private void put(HttpPut put, AbstractHttpEntity entity, String contentType, boolean expectContinue) throws SardineException
	{
		put.setEntity(entity);
		if (contentType != null)
		{
			put.setHeader("Content-Type", contentType);
		}
		if (expectContinue)
		{
			put.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
		}
		execute(put, new VoidResponseHandler());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#delete(java.lang.String)
	 */
	public void delete(String url) throws SardineException
	{
		final HttpDelete delete = new HttpDelete(url);
		execute(delete, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#move(java.lang.String, java.lang.String)
		  */
	public void move(String sourceUrl, String destinationUrl) throws SardineException
	{
		HttpMove move = new HttpMove(sourceUrl, destinationUrl);
		execute(move, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#copy(java.lang.String, java.lang.String)
		  */
	public void copy(String sourceUrl, String destinationUrl) throws SardineException
	{
		HttpCopy copy = new HttpCopy(sourceUrl, destinationUrl);
		execute(copy, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#createDirectory(java.lang.String)
		  */
	public void createDirectory(String url) throws SardineException
	{
		HttpMkCol mkcol = new HttpMkCol(url);
		execute(mkcol, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#exists(java.lang.String)
		  */
	public boolean exists(String url) throws SardineException
	{
		final HttpHead head = new HttpHead(url);
		return execute(head, new ExistsResponseHandler());
	}

	/**
	 * Wraps all checked exceptions to {@link SardineException}. Validate the response using the
	 * response handler.
	 *
	 * @param <T>             Return type
	 * @param request		 Request to execute
	 * @param responseHandler Determines the return type.
	 * @return parsed response
	 * @throws SardineException
	 */
	private <T> T execute(final HttpRequestBase request, final ResponseHandler<T> responseHandler)
			throws SardineException
	{
		try
		{
			return client.execute(request, responseHandler, localcontext);
		}
		catch (SardineException e)
		{
			// Catch first as we don't want to wrap again and throw again.
			request.abort();
			throw e;
		}
		catch (IOException e)
		{
			request.abort();
			throw new SardineException(e);
		}
	}

	/**
	 * No validation of the response. Wraps all checked exceptions to {@link SardineException}.
	 *
	 * @param request Request to execute
	 * @return Response
	 * @throws com.googlecode.sardine.util.SardineException
	 *
	 */
	private HttpResponse execute(final HttpRequestBase request)
			throws SardineException
	{
		try
		{
			return client.execute(request, localcontext);
		}
		catch (SardineException e)
		{
			request.abort();
			throw e;
		}
		catch (IOException e)
		{
			request.abort();
			throw new SardineException(e);
		}
	}

	/**
	 * Creates default params, set maximal total connections to {@link #MAX_TOTAL_CONNECTIONS}.
	 *
	 * @return httpParams
	 */
	protected HttpParams createDefaultHttpParams()
	{
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params, "Sardine/" + Version.getSpecification());
		// Only selectively enable this for PUT but not all entity enclosing methods
		HttpProtocolParams.setUseExpectContinue(params, false);
		return params;
	}

	/**
	 * Creates a new {@link org.apache.http.conn.scheme.SchemeRegistry} for default ports
	 * with socket factories.
	 *
	 * @param sslSocketFactory alternative {@link SSLSocketFactory}.
	 * @param port			 alternate port.
	 * @return a new {@link org.apache.http.conn.scheme.SchemeRegistry}.
	 */
	protected SchemeRegistry createDefaultSchemeRegistry(SSLSocketFactory sslSocketFactory, Integer port)
	{
		final SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", port != null ? port : 80, PlainSocketFactory.getSocketFactory()));
		if (sslSocketFactory != null)
		{
			registry.register(new Scheme("https", port != null ? port : 443, sslSocketFactory));
		}
		else
		{
			registry.register(new Scheme("https", port != null ? port : 443, SSLSocketFactory.getSocketFactory()));
		}
		return registry;
	}

	/**
	 * Use fail fast connection manager when connections are not released properly.
	 *
	 * @param schemeRegistry
	 * @return
	 */
	protected ClientConnectionManager createDefaultConnectionManager(SchemeRegistry schemeRegistry)
	{
		return new SingleClientConnManager(schemeRegistry);
	}
}
