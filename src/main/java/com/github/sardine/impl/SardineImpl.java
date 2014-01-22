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

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.VersionInfo;
import org.w3c.dom.Element;

import com.github.sardine.DavAce;
import com.github.sardine.DavAcl;
import com.github.sardine.DavPrincipal;
import com.github.sardine.DavQuota;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.Version;
import com.github.sardine.impl.handler.ExistsResponseHandler;
import com.github.sardine.impl.handler.LockResponseHandler;
import com.github.sardine.impl.handler.MultiStatusResponseHandler;
import com.github.sardine.impl.handler.VoidResponseHandler;
import com.github.sardine.impl.io.ConsumingInputStream;
import com.github.sardine.impl.io.ContentLengthInputStream;
import com.github.sardine.impl.methods.HttpCopy;
import com.github.sardine.impl.methods.HttpMkCol;
import com.github.sardine.impl.methods.HttpMove;
import com.github.sardine.impl.methods.HttpPropFind;
import com.github.sardine.model.Multistatus;
import com.github.sardine.model.ObjectFactory;
import com.github.sardine.model.Prop;
import com.github.sardine.model.Propfind;
import com.github.sardine.model.Response;
import com.github.sardine.util.SardineUtil;

/**
 * Implementation of the Sardine interface. This is where the meat of the Sardine library lives.
 *
 * @author jonstevens
 * @version $Id$
 */
public class SardineImpl extends SardineImplBase implements Sardine
{
	private HttpClientBuilder builder;
	private HttpClient client;
	private HttpClientConnectionManager connectionManager; 

	/***
	 * 
	 * @param builder
	 */
	public SardineImpl(HttpClientBuilder builder){
		 this(builder, null);
	}
	
	/**
	 * Constructor which takes a custom client builder. It is up to the caller to make sure
	 * that the builder is fully initialized
	 * @param builder custom client builder
	 * @param connectionManager custom connection manager
	 */
	public SardineImpl(HttpClientBuilder builder, HttpClientConnectionManager connectionManager) {
		this.builder = builder;
		if (builder != null) {
			this.connectionManager = connectionManager;
			if (connectionManager == null)
				setConnectionManager();
			buildClient();
		}
		else
			log.warn("builder is null: unable to initialize");

	}
	
	
	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param selector Proxy configuration
	 */
	public SardineImpl(String username, String password, ProxySelector selector, boolean enableCompression)
	{
		this.init(new SardineRedirectStrategy(), username, password,enableCompression);
	}

	/**
	 * @param http Custom client configuration
	 */
	public SardineImpl()
	{
		this.init(new SardineRedirectStrategy(), null, null,false);
	}

	/**
	 * @param http Custom client configuration
	 *             @param redirect Custom redirect strategy
	 */
	public SardineImpl(RedirectStrategy redirect)
	{
		this.init(redirect, null, null, false);
	}

	/**
	 * @param http	 Custom client configuration
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	public SardineImpl(String username, String password, boolean enableCompression)
	{
		this.init(new SardineRedirectStrategy(), username, password, enableCompression);
	}

	/***
	 * Default initialization
	 * @param redirect
	 * @param username
	 * @param password
	 */
	private void init(RedirectStrategy redirect, String username, String password, boolean enableCompression)
	{
		builder = HttpClientBuilder.create();
		setConnectionManager();
		builder.setSchemePortResolver(DefaultSchemePortResolver.INSTANCE);
		setUserAgent();
		builder.setRedirectStrategy(redirect);
		setCredentials(username, password);
		if (enableCompression)
			enableCompression();
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
	 */
	protected void setConnectionManager() {
		BasicHttpClientConnectionManager cm  = new BasicHttpClientConnectionManager();
		cm.setConnectionConfig(	ConnectionConfig.custom()
				                                .setCharset(HTTP.DEF_CONTENT_CHARSET)
												.setBufferSize(8192)
												.build() );
		cm.setSocketConfig(SocketConfig.custom()
				                       .setTcpNoDelay(true)
				                       .build());
		
		connectionManager = cm;
		builder.setConnectionManager(connectionManager);
	}
	
	/***
	 * Build client. 
	 */
	@Override
	public void buildClient() {
		if (builder != null)
		     client = builder.build();
		else
			log.warn("builder is null: cannot build client");
	}
	
	/***
	 * Set http protocol version
	 * @param version
	 */
	@Override
	public void setProtocolVersion(ProtocolVersion version) {
		super.setProtocolVersion(version);
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
	 * Enables HTTP GZIP compression. If enabled, requests originating from Sardine
	 * will include "gzip" as an "Accept-Encoding" header.
	 * <p/>
	 * If the server also supports gzip compression, it should serve the
	 * contents in compressed gzip format and include "gzip" as the
	 * Content-Encoding. If the content encoding is present, Sardine will
	 * automatically decompress the files upon reception.
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
	public void disablePreemptiveAuthentication()	{
		super.disablePreemptiveAuthentication();
	}


	@Override
	public List<DavResource> list(String url) throws IOException	{
		return this.list(url, 1);
	}

	@Override
	public List<DavResource> list(String url, int depth) throws IOException
	{
		return list(url, depth, true);
	}

        @Override
	public List<DavResource> list(String url, int depth, boolean allProp) throws IOException
	{
		Multistatus multistatus = execute(generateListEntity(url, depth, allProp),
				                          new MultiStatusResponseHandler());
		return processListResponses( multistatus.getResponse());

	}

	@Override
	public List<DavResource> list(String url, int depth, java.util.Set<QName> props) throws IOException
	{
		Propfind body = new Propfind();
        
		Prop prop = new Prop();
		ObjectFactory objectFactory = new ObjectFactory();
		prop.setGetcontentlength(objectFactory.createGetcontentlength());
		prop.setGetlastmodified(objectFactory.createGetlastmodified());
		prop.setCreationdate(objectFactory.createCreationdate());
		prop.setDisplayname(objectFactory.createDisplayname());
		prop.setGetcontenttype(objectFactory.createGetcontenttype());
		prop.setResourcetype(objectFactory.createResourcetype());
		prop.setGetetag(objectFactory.createGetetag());
                
		List<Element> any = prop.getAny();
		for (QName entry : props) {
			Element element = SardineUtil.createElement(entry);
			any.add(element);
		}

		body.setProp(prop);
                
		return list(url, depth, body);

	}



	protected List<DavResource> list(String url, int depth, Propfind body) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth(Integer.toString(depth));
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
		List<DavResource> resources = new ArrayList<DavResource>(responses.size());
		for (Response response : responses)
		{
			try
			{
				resources.add(new DavResource(response));
			}
			catch (URISyntaxException e)
			{
				log.warn(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
			}
		}
		return resources;
        }


	@Override
	public List<DavResource> patch(String url, Map<QName, String> setProps) throws IOException
	{
		return patch(url, setProps, Collections.<QName>emptyList());
	}

	/**
	 * Creates a {@link com.github.sardine.model.Propertyupdate} element containing all properties to set from setProps and all properties to
	 * remove from removeProps. Note this method will use a {@link com.github.sardine.util.SardineUtil#CUSTOM_NAMESPACE_URI} as
	 * namespace and {@link com.github.sardine.util.SardineUtil#CUSTOM_NAMESPACE_PREFIX} as prefix.
	 */
	@Override
	public List<DavResource> patch(String url, Map<QName, String> setProps, List<QName> removeProps) throws IOException
	{

		Multistatus multistatus = this.execute( generatePatchEntity(url, setProps, removeProps), 
				                                new MultiStatusResponseHandler());
		return processListResponses(multistatus.getResponse());
	}


	@Override
	public String lock(String url) throws IOException
	{
		// Return the lock token
		return execute(generateLockEntity(url),
				            new LockResponseHandler());
	}

	@Override
	public String refreshLock(String url, String token, String file) throws IOException
	{
		return this.execute( generateRefreshLockEntity(url, token, file), 
				             new LockResponseHandler());
	}

	@Override
	public void unlock(String url, String token) throws IOException
	{
		this.execute(generateUnlockEntity(url, token),
				     new VoidResponseHandler());
	}

	@Override
	public void setAcl(String url, List<DavAce> aces) throws IOException
	{
		this.execute(generateSetAclEntity(url, aces),
				     new VoidResponseHandler());
	}


	@Override
	public DavAcl getAcl(String url) throws IOException
	{
		Multistatus multistatus = this.execute(generateGetAclEntity(url),
				                                new MultiStatusResponseHandler());
		return processGetAclReponses(multistatus.getResponse());

	}

	@Override
	public DavQuota getQuota(String url) throws IOException
	{
		Multistatus multistatus = this.execute(generateGetQuotaEntity(url), 
				                               new MultiStatusResponseHandler());
		return processGetQuotaResponses(multistatus.getResponse());

	}

	@Override
	public List<DavPrincipal> getPrincipals(String url) throws IOException
	{
		HttpPropFind entity = generateGetPrincipalsEntity(url);
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		return processGetPrincipalsResponses(multistatus.getResponse());
	}

	@Override
	public List<String> getPrincipalCollectionSet(String url) throws IOException
	{
		HttpPropFind entity = generatePrincipalCollectionSetEntity(url);
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		return processGgetPrincipalCollectionSetResponses(multistatus.getResponse());
	}

	@Override
	public ContentLengthInputStream get(String url) throws IOException
	{
		return this.get(url, Collections.<String, String>emptyMap());
	}

	@Override
	public ContentLengthInputStream get(String url, Map<String, String> headers) throws IOException
	{
		HttpGet get = new HttpGet(url);
		for (String header : headers.keySet())
		{
			get.addHeader(header, headers.get(header));
		}
		// Must use #execute without handler, otherwise the entity is consumed
		// already after the handler exits.
		HttpResponse response = this.execute(get);
		VoidResponseHandler handler = new VoidResponseHandler();
		try
		{
			handler.handleResponse(response);
			// Will consume the entity when the stream is closed.
			return new ConsumingInputStream(response);
		}
		catch (IOException ex)
		{
			get.abort();
			throw ex;
		}
	}

	@Override
	public void put(String url, byte[] data) throws IOException
	{
		this.put(url, data, null);
	}

	@Override
	public void put(String url, byte[] data, String contentType) throws IOException
	{
		ByteArrayEntity entity = new ByteArrayEntity(data);
		this.put(url, entity, contentType, true);
	}

	@Override
	public void put(String url, InputStream dataStream) throws IOException
	{
		this.put(url, dataStream, (String) null);
	}

	@Override
	public void put(String url, InputStream dataStream, String contentType) throws IOException
	{
		this.put(url, dataStream, contentType, true);
	}

	@Override
	public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException
	{
		// A length of -1 means "go until end of stream"
		put(url, dataStream, contentType, expectContinue, -1);
	}

	@Override
	public void put(String url, InputStream dataStream, String contentType, boolean expectContinue, long contentLength) throws IOException
	{
		InputStreamEntity entity = new InputStreamEntity(dataStream, contentLength);
		this.put(url, entity, contentType, expectContinue);
	}

	@Override
	public void put(String url, InputStream dataStream, Map<String, String> headers) throws IOException
	{
		// A length of -1 means "go until end of stream"
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		this.put(url, entity, headers);
	}

	/**
	 * Upload the entity using <code>PUT</code>
	 *
	 * @param url			Resource
	 * @param entity		 The entity to read from
	 * @param contentType	Content Type header
	 * @param expectContinue Add <code>Expect: continue</code> header
	 */
	public void put(String url, HttpEntity entity, String contentType, boolean expectContinue) throws IOException
	{
		Map<String, String> headers =  generatePutHeaders(url, entity, contentType, expectContinue);
		this.put(url, entity, headers);
	}

	/**
	 * Upload the entity using <code>PUT</code>
	 *
	 * @param url	 Resource
	 * @param entity  The entity to read from
	 * @param headers Headers to add to request
	 */
	public void put(String url, HttpEntity entity, Map<String, String> headers) throws IOException
	{
		put(url, entity, headers, new VoidResponseHandler());
	 }

	public <T> T put(String url, HttpEntity entity, Map<String, String> headers, ResponseHandler<T> handler) throws IOException
	{
		HttpPut put = generatePutEntity(url, entity, headers);
		try
		{
			return this.execute(put, handler);
		}
		catch (HttpResponseException e)
		{
			if (e.getStatusCode() == HttpStatus.SC_EXPECTATION_FAILED)
			{
				// Retry with the Expect header removed
				put.removeHeaders(HTTP.EXPECT_DIRECTIVE);
				if (entity.isRepeatable())
				{
					return this.execute(put, handler);
				}
			}
			throw e;
		}
	}

	@Override
	public void delete(String url) throws IOException
	{
		this.execute(new HttpDelete(url),
				     new VoidResponseHandler());
	}

	@Override
	public void move(String sourceUrl, String destinationUrl) throws IOException
	{
		this.execute(new HttpMove(sourceUrl, destinationUrl),
					new VoidResponseHandler());
	}

	@Override
	public void copy(String sourceUrl, String destinationUrl) throws IOException
	{
		this.execute(new HttpCopy(sourceUrl, destinationUrl), 
				     new VoidResponseHandler());
	}

	@Override
	public void createDirectory(String url) throws IOException
	{
		this.execute( new HttpMkCol(url), 
				      new VoidResponseHandler());
	}

	@Override
	public boolean exists(String url) throws IOException
	{
		HttpHead head = new HttpHead(url);
		head.setProtocolVersion(version);
		return this.execute(head, new ExistsResponseHandler());
	}

	/**
	 * Validate the response using the response handler. Aborts the request if there is an exception.
	 *
	 * @param <T>             Return type
	 * @param request		 Request to execute
	 * @param responseHandler Determines the return type.
	 * @return parsed response
	 */
	protected <T> T execute(HttpRequestBase request, ResponseHandler<T> responseHandler)
			throws IOException
	{
		try
		{
			// Clear circular redirect cache
			context.removeAttribute(HttpClientContext.REDIRECT_LOCATIONS);
			// Execute with response handler
			return client.execute(request, responseHandler, context);
		}
		catch (IOException e)
		{
			request.abort();
			throw e;
		}
	}

	/**
	 * No validation of the response. Aborts the request if there is an exception.
	 *
	 * @param request Request to execute
	 * @return The response to check the reply status code
	 */
	protected HttpResponse execute(HttpRequestBase request)
			throws IOException
	{
		try
		{
			// Clear circular redirect cache
			context.removeAttribute(HttpClientContext.REDIRECT_LOCATIONS);
			// Execute with no response handler
			return client.execute(request, context);
		}
		catch (IOException e)
		{
			request.abort();
			throw e;
		}
	}

	@Override
	public void shutdown() 	{
		if (connectionManager != null)
		   connectionManager.shutdown();
	}
}
