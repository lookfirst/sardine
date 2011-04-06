package com.googlecode.sardine;

import com.googlecode.sardine.impl.*;
import com.googlecode.sardine.impl.handler.ExistsResponseHandler;
import com.googlecode.sardine.impl.handler.MultiStatusResponseHandler;
import com.googlecode.sardine.impl.handler.VoidResponseHandler;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

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
	DefaultHttpClient client;

	/**
	 * was a username/password passed in?
	 */
	boolean authEnabled;

	/** */
	public SardineImpl() throws SardineException
	{
		this(null, null);
	}

	/** */
	public SardineImpl(String username, String password) throws SardineException
	{
		this(username, password, null, null);
	}

	/** */
	public SardineImpl(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner) throws SardineException
	{
		this(username, password, null, null, null);
	}

	/**
	 * Main constructor.
	 */
	public SardineImpl(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner, Integer port) throws SardineException
	{
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUserAgent(params, "Sardine/" + Version.getSpecification());

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", port != null ? port : 80, PlainSocketFactory.getSocketFactory()));
		if (sslSocketFactory != null)
		{
			schemeRegistry.register(new Scheme("https", port != null ? port : 443, sslSocketFactory));
		}
		else
		{
			schemeRegistry.register(new Scheme("https", port != null ? port : 443, SSLSocketFactory.getSocketFactory()));
		}

		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setMaxTotal(100);
		this.client = new DefaultHttpClient(cm, params);

		// for proxy configurations
		if (routePlanner != null)
		{
			this.client.setRoutePlanner(routePlanner);
		}

		if ((username != null) && (password != null))
		{
			this.client.getCredentialsProvider().setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
					new UsernamePasswordCredentials(username, password));

			this.authEnabled = true;
		}
	}

	/** */
	public void enableCompression()
	{
		this.client.addRequestInterceptor(new GzipSupportRequestInterceptor());
		this.client.addResponseInterceptor(new GzipSupportResponseInterceptor());
	}

	/** */
	public void disableCompression()
	{
		this.client.removeRequestInterceptorByClass(GzipSupportRequestInterceptor.class);
		this.client.removeResponseInterceptorByClass(GzipSupportResponseInterceptor.class);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#getResources(java.lang.String)
	 */
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

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, byte[])
	 */
	public void put(String url, byte[] data) throws SardineException
	{
		put(url, data, null);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, byte[], java.lang.String)
	 */
	public void put(String url, byte[] data, String contentType) throws SardineException
	{
		HttpPut put = new HttpPut(url);
		ByteArrayEntity entity = new ByteArrayEntity(data);
		put(put, entity, null);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, InputStream)
	 */
	public void put(String url, InputStream dataStream) throws SardineException
	{
		put(url, dataStream, null);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, java.io.InputStream, java.lang.String)
	 */
	public void put(String url, InputStream dataStream, String contentType) throws SardineException
	{
		HttpPut put = new HttpPut(url);
		// A length of -1 means "go until end of stream"
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put(put, entity, contentType);
	}

	/**
	 * Private helper for doing the work of a put
	 */
	private void put(HttpPut put, AbstractHttpEntity entity, String contentType) throws SardineException
	{
		put.setEntity(entity);
		if (contentType != null)
		{
			put.setHeader("Content-Type", contentType);
		}
		this.execute(put, new VoidResponseHandler());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#delete(java.lang.String)
	 */
	public void delete(String url) throws SardineException
	{
		HttpDelete delete = new HttpDelete(url);
		this.execute(delete, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#move(java.lang.String, java.lang.String)
		  */
	public void move(String sourceUrl, String destinationUrl) throws SardineException
	{
		HttpMove move = new HttpMove(sourceUrl, destinationUrl);
		this.execute(move, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#copy(java.lang.String, java.lang.String)
		  */
	public void copy(String sourceUrl, String destinationUrl)
			throws SardineException
	{
		HttpCopy copy = new HttpCopy(sourceUrl, destinationUrl);
		this.execute(copy, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#createDirectory(java.lang.String)
		  */
	public void createDirectory(String url) throws SardineException
	{
		HttpMkCol mkcol = new HttpMkCol(url);
		this.execute(mkcol, new VoidResponseHandler());
	}

	/*
		  * (non-Javadoc)
		  * @see com.googlecode.sardine.Sardine#exists(java.lang.String)
		  */
	public boolean exists(String url) throws SardineException
	{
		HttpHead head = new HttpHead(url);
		return this.execute(head, new ExistsResponseHandler());
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
			return client.execute(request, responseHandler);
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
			return client.execute(request);
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
}
