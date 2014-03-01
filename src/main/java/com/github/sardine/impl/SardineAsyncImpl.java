/*
+ * Copyright 2009-2011 Jon Stevens et al.
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

import com.github.sardine.SardineAsync;
import com.github.sardine.Version;
import com.github.sardine.impl.handler.VoidResponseHandler;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.codecs.DefaultHttpRequestWriterFactory;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParser;
import org.apache.http.impl.nio.codecs.DefaultHttpResponseParserFactory;
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionFactory;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.nio.NHttpMessageParser;
import org.apache.http.nio.NHttpMessageParserFactory;
import org.apache.http.nio.NHttpMessageWriterFactory;
import org.apache.http.nio.client.methods.ZeroCopyPut;
import org.apache.http.nio.conn.ManagedNHttpClientConnection;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NHttpConnectionFactory;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.SessionInputBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.VersionInfo;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.nio.charset.CodingErrorAction;
import java.util.Map;
import java.util.concurrent.Future;


/**
 * Implementation of the Sardine interface. This is where the meat of the Sardine library lives.
 */
public class SardineAsyncImpl extends SardineImplBase implements SardineAsync
{

	private HttpAsyncClientBuilder builder;
	private CloseableHttpAsyncClient client;
	private NHttpClientConnectionManager connectionManager; 
	
	/***
	 * 
	 * @param builder
	 */
	public SardineAsyncImpl(HttpAsyncClientBuilder builder){
		 this(builder, null);
	}
	
	/**
	 * Constructor which takes a custom client builder. It is up to the caller to make sure
	 * that the builder is fully initialized
	 * @param builder custom client builder
	 * @param connectionManager custom connection manager
	 */
	public SardineAsyncImpl(HttpAsyncClientBuilder builder, NHttpClientConnectionManager connectionManager) {
		this.builder = builder;
		if (builder != null) {
			this.connectionManager = connectionManager;
			if (connectionManager == null) {
				try {
					setConnectionManager();
				} catch (IOReactorException e) {
					e.printStackTrace();
				}
			}
			buildClient();
		} else {
			log.warn("builder is null: unable to initialize");
		}
	}
	
	
	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param selector Proxy configuration
	 */
	public SardineAsyncImpl(String username, String password, ProxySelector selector)
	{
		this.init(new SardineRedirectStrategy(), username, password);
	}

	/**
	 */
	public SardineAsyncImpl()
	{
		this.init(new SardineRedirectStrategy(), null, null);
	}

	/**
	 * @param redirect Custom redirect strategy
	 */
	public SardineAsyncImpl(RedirectStrategy redirect)
	{
		this.init(redirect, null, null);
	}

	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	public SardineAsyncImpl(String username, String password)
	{
		this.init(new SardineRedirectStrategy(), username, password);
	}
	
	
	/*** 
	 * Set http protocol version
	 * @param version
	 */
	@Override
	public void setProtocolVersion(ProtocolVersion version) {
		super.setProtocolVersion(version);
	}

	/***
	 * Default initialization
	 * @param redirect
	 * @param username
	 * @param password
	 */
	private void init(RedirectStrategy redirect, String username, String password)
	{
		builder = HttpAsyncClientBuilder.create();
		try {
			setConnectionManager();
		} catch (IOReactorException e) {
			e.printStackTrace();
		}
		builder.setSchemePortResolver(DefaultSchemePortResolver.INSTANCE);
		setUserAgent();
		builder.setRedirectStrategy(redirect);
		setCredentials(username, password);
		buildClient();
	}
	
	/***
	 * Set use agent
	 */
	protected void setUserAgent() {
		String version = Version.getSpecification();
		if (version == null)
		{
			version = VersionInfo.UNAVAILABLE;
		}
		builder.setUserAgent("Sardine/" + version);
	}
	
	/***
	 * Set connection manager
	 * @throws IOReactorException 
	 */
	protected void setConnectionManager() throws IOReactorException {
		
		 // Use custom message parser / writer to customize the way HTTP
		// messages are parsed from and written out to the data stream.
		NHttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {

			@Override
			public NHttpMessageParser<HttpResponse> create(
					final SessionInputBuffer buffer,
					final MessageConstraints constraints) {
				LineParser lineParser = new BasicLineParser() {

					@Override
					public Header parseHeader(final CharArrayBuffer buffer) {
						try {
							return super.parseHeader(buffer);
						} catch (ParseException ex) {
							return new BasicHeader(buffer.toString(), null);
						}
					}

				};
				return new DefaultHttpResponseParser(
						buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints);
			}

		};
		NHttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

		// Use a custom connection factory to customize the process of
		// initialization of outgoing HTTP connections. Beside standard connection
		// configuration parameters HTTP connection factory can define message
		// parser / writer routines to be employed by individual connections.
		NHttpConnectionFactory<ManagedNHttpClientConnection> connFactory =
											new ManagedNHttpClientConnectionFactory(requestWriterFactory,
														responseParserFactory,
														HeapByteBufferAllocator.INSTANCE);

		// Client HTTP connection objects when fully initialized can be bound to
		// an arbitrary network socket. The process of network socket initialization,
		// its connection to a remote address and binding to a local one is controlled
		// by a connection socket factory.

		// SSL context for secure connections can be created either based on
		// system or application specific properties.
		SSLContext sslcontext = SSLContexts.createSystemDefault();
		// Use custom hostname verifier to customize SSL hostname verification.
		X509HostnameVerifier hostnameVerifier = new BrowserCompatHostnameVerifier();

		// Create a registry of custom connection session strategies for supported
		// protocol schemes.
		Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
			.register("http", NoopIOSessionStrategy.INSTANCE)
			.register("https", new SSLIOSessionStrategy(sslcontext, hostnameVerifier))
			.build();

		// Create I/O reactor configuration
		IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
				.setIoThreadCount(Runtime.getRuntime().availableProcessors())
				.setConnectTimeout(30000)
				.setSoTimeout(30000)
				.setTcpNoDelay(true)
				.build();

		// Create a custom I/O reactort
		ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

		// Create a connection manager with custom configuration.
		PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(
				ioReactor, connFactory, sessionStrategyRegistry);

		// Create message constraints
		MessageConstraints messageConstraints = MessageConstraints.custom()
			.setMaxHeaderCount(200)
			.setMaxLineLength(2000)
			.build();

		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
			.setMalformedInputAction(CodingErrorAction.IGNORE)
			.setUnmappableInputAction(CodingErrorAction.IGNORE)
			.setCharset(HTTP.DEF_CONTENT_CHARSET)
			.setBufferSize(4000)
			.setMessageConstraints(messageConstraints)
			.build();
		// Configure the connection manager to use connection configuration either
		// by default or for a specific host.
		connManager.setDefaultConnectionConfig(connectionConfig);

		// Configure total max or per route limits for persistent connections
		// that can be kept in the pool or leased by the connection manager.
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(10);
		
		connectionManager = connManager;
		builder.setConnectionManager(connectionManager);
	}
	
	/***
	 * Build client. 
	 */
	@Override
	public void buildClient() {
		if (builder != null){
			 client = builder.build();
			 client.start();
		} else {
			log.warn("builder is null: cannot build client");
		}
	}

	/**
	 * Add credentials to any scope. Supports Basic, Digest and NTLM authentication methods.
	 *
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	public void setCredentials(String username, String password){
		this.setCredentials(username, password, "", "");
	}

	/**
	 * @param username	Use in authentication header credentials
	 * @param password	Use in authentication header credentials
	 * @param domain	  NTLM authentication
	 * @param workstation NTLM authentication
	 */
	public void setCredentials(String username, String password, String domain, String workstation)	{
		BasicCredentialsProvider basicCredentialsProvider = createCredentialsProvider(username, password, domain, workstation);
		if (basicCredentialsProvider != null)
			 builder.setDefaultCredentialsProvider(basicCredentialsProvider);		
	}

	/**
	 * Adds handling of GZIP compression to the client.
	 */
	public void enableCompression()	{
		builder.addInterceptorFirst(new RequestAcceptEncoding());
		builder.addInterceptorFirst(new ResponseContentEncoding());
	}

	@Override
	public void enablePreemptiveAuthentication(String hostname)	{
		super.enablePreemptiveAuthentication(hostname);
	}

	@Override
	public void disablePreemptiveAuthentication() {
		super.disablePreemptiveAuthentication();
	}


	@Override
	public Future<HttpResponse> put(String url, File file, FutureCallback<HttpResponse> callback) throws IOException {
		ZeroCopyPut put = new ZeroCopyPut(
			url,
			file,
			ContentType.DEFAULT_BINARY) {

			@Override
			protected HttpEntityEnclosingRequest createRequest(
					final URI requestURI, final HttpEntity entity) {
				HttpEntityEnclosingRequest request = super.createRequest(requestURI, entity);
				request.addHeader(HttpHeaders.CONTENT_TYPE, HTTP.DEF_CONTENT_CHARSET.name());
				return request;
			}
		};

		return client.execute(put, new BasicAsyncResponseConsumer() {

			@Override
			protected void onResponseReceived(final HttpResponse response) throws IOException {
				super.onResponseReceived(response);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
					return;
				}
				throw new SardineException("Unexpected response", statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}
		}, context, callback);
	}

	@Override
	public Future<HttpResponse> put(String url, byte[] data, FutureCallback<HttpResponse> callback) throws IOException
	{
		return put(url, data, null, callback);
	}

	@Override
	public Future<HttpResponse> put(String url, byte[] data, String contentType, FutureCallback<HttpResponse> callback) throws IOException
	{
		ByteArrayEntity entity = new ByteArrayEntity(data);
		return put(url, entity, contentType, true,callback);
	}

	@Override
	public Future<HttpResponse> put(String url, InputStream dataStream, FutureCallback<HttpResponse> callback) throws IOException
	{
		return put(url, dataStream, (String) null,callback);
	}

	@Override
	public Future<HttpResponse> put(String url, InputStream dataStream, String contentType, FutureCallback<HttpResponse> callback) throws IOException
	{
		return put(url, dataStream, contentType, true,callback);
	}

	@Override
	public Future<HttpResponse> put(String url, InputStream dataStream, String contentType, boolean expectContinue, FutureCallback<HttpResponse> callback) throws IOException
	{
		// A length of -1 means "go until end of stream"
		return put(url, dataStream, contentType, expectContinue, -1, callback);
	}

	@Override
	public Future<HttpResponse> put(String url, InputStream dataStream, String contentType, boolean expectContinue, long contentLength, FutureCallback<HttpResponse> callback) throws IOException
	{
		InputStreamEntity entity = new InputStreamEntity(dataStream, contentLength);
		return put(url, entity, contentType, expectContinue,callback);
	}

	@Override
	public Future<HttpResponse> put(String url, InputStream dataStream, Map<String, String> headers, FutureCallback<HttpResponse> callback) throws IOException
	{
		// A length of -1 means "go until end of stream"
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		return put(url, entity, headers,callback);
	}

	/**
	 * Upload the entity using <code>PUT</code>
	 *
	 * @param url			Resource
	 * @param entity		 The entity to read from
	 * @param contentType	Content Type header
	 * @param expectContinue Add <code>Expect: continue</code> header
	 */
	public Future<HttpResponse> put(String url, HttpEntity entity, String contentType, boolean expectContinue, FutureCallback<HttpResponse> callback) throws IOException
	{
		Map<String, String> headers =  generatePutHeaders(url, entity, contentType, expectContinue);
		return put(url, entity, headers,callback);
	}

	/**
	 * Upload the entity using <code>PUT</code>
	 *
	 * @param url	 Resource
	 * @param entity  The entity to read from
	 * @param headers Headers to add to request
	 */
	public Future<HttpResponse> put(String url, HttpEntity entity, Map<String, String> headers, FutureCallback<HttpResponse> callback) throws IOException
	{
		return put(url, entity, headers, new VoidResponseHandler(),callback);
	 }

	public <T> Future<HttpResponse> put(String url, HttpEntity entity, Map<String, String> headers, ResponseHandler<T> handler, FutureCallback<HttpResponse> callback) throws IOException
	{
		HttpPut put = generatePutEntity(url, entity, headers);
		try
		{
			return execute(put, handler, callback);
		}
		catch (HttpResponseException e)
		{
			if (e.getStatusCode() == HttpStatus.SC_EXPECTATION_FAILED)
			{
				// Retry with the Expect header removed
				put.removeHeaders(HTTP.EXPECT_DIRECTIVE);
				if (entity.isRepeatable())
				{
					return execute(put, handler, callback);
				}
			}
			throw e;
		}
	}

	/**
	 * Validate the response using the response handler. Aborts the request if there is an exception.
	 *
	 * @param <T>             Return type
	 * @param request		 Request to execute
	 * @param responseHandler Determines the return type.
	 * @return parsed response
	 */
	protected <T> Future<HttpResponse> execute(HttpRequestBase request, ResponseHandler<T> responseHandler, FutureCallback<HttpResponse> callback)
			throws IOException
	{
		// Clear circular redirect cache
		context.removeAttribute(HttpClientContext.REDIRECT_LOCATIONS);
		// Execute with response handler
		return client.execute(request, context, callback);
	}

	/**
	 * No validation of the response. Aborts the request if there is an exception.
	 *
	 * @param request Request to execute
	 * @return The response to check the reply status code
	 */
	protected Future<HttpResponse> execute(HttpRequestBase request, FutureCallback<HttpResponse> callback)
			throws IOException
	{
		// Clear circular redirect cache
		context.removeAttribute(HttpClientContext.REDIRECT_LOCATIONS);
		// Execute with no response handler
		return client.execute(request, context,callback);
	}
	
	@Override
	public void shutdown() 	{
		if (connectionManager != null) {
			try {
				connectionManager.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
