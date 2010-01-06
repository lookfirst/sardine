package com.googlecode.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.SardineUtil;
import com.googlecode.sardine.util.SardineUtil.HttpPropFind;
import com.googlecode.sardine.util.SardineUtil.HttpMove;

/**
 * Implementation of the Sardine interface.
 *
 * @author jonstevens
 */
public class SardineImpl implements Sardine
{
	/** */
	Factory factory;

	/** */
	DefaultHttpClient client;

	/** */
	public SardineImpl(Factory factory)
	{
		this(factory, null, null);
	}

	/** */
	public SardineImpl(Factory factory, String username, String password)
	{
		this.factory = factory;

		HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 100);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
		        new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		this.client = new DefaultHttpClient(cm, params);

		if (username != null && password != null)
			this.client.getCredentialsProvider().setCredentials(
	                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
	                new UsernamePasswordCredentials(username, password));
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#getResources(java.lang.String)
	 */
	public List<DavResource> getResources(String url) throws IOException
	{
		HttpPropFind propFind = new HttpPropFind(url);
		propFind.setEntity(SardineUtil.getResourcesEntity());
		HttpResponse response = this.client.execute(propFind);

		int statusCode = response.getStatusLine().getStatusCode();
		if (!SardineUtil.isGoodResponse(statusCode))
			throw new IOException("Got status code: '" + statusCode + "'. Is the url valid? " + url);

		Multistatus r = null;
		try
		{
			r = (Multistatus) this.factory.getUnmarshaller().unmarshal(response.getEntity().getContent());
		}
		catch (JAXBException ex)
		{
			// This is done to get around a Java5 limitation that has been resolved in Java6.
			IOException exception = new IOException("Problem unmarshalling the data for url: " + url);
			exception.initCause(ex);
			throw exception;
		}

		List<Response> responses = r.getResponse();

		List<DavResource> resources = new ArrayList<DavResource>(responses.size());

		// Make sure the path is correctly detected even if the path identifier is changed by the server
		String path = responses.get(0).getHref().get(0);

		for (Response resp : responses)
		{
			String href = resp.getHref().get(0);

			// Ignore the pointless result
			if (href.equals(path))
				continue;

			// Each href includes the full path, so chop off to get the name of the current item.
			String name = href.substring(path.length(), href.length());

			// Ignore crap files
			if (name.equals(".DS_Store"))
				continue;

			// Remove the final / from directories
			if (name.endsWith("/"))
				name = name.substring(0, name.length() - 1);

			String creationdate = resp.getPropstat().get(0).getProp().getCreationdate().getContent().get(0);
			String modifieddate = resp.getPropstat().get(0).getProp().getGetlastmodified().getContent().get(0);

			String contentType = "";
			Getcontenttype gtt = resp.getPropstat().get(0).getProp().getGetcontenttype();
			if (gtt != null)
				contentType = gtt.getContent().get(0);

			String contentLength = "0";
			Getcontentlength gcl = resp.getPropstat().get(0).getProp().getGetcontentlength();
			if (gcl != null)
				contentLength = gcl.getContent().get(0);

			DavResource dr = new DavResource(url, name, SardineUtil.parseDate(creationdate),
					SardineUtil.parseDate(modifieddate), contentType, Long.valueOf(contentLength));

			resources.add(dr);
		}
		return resources;
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#getInputStream(java.lang.String)
	 */
	public InputStream getInputStream(String url) throws IOException
	{
		HttpGet get = new HttpGet(url);
		HttpResponse response = this.client.execute(get);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new IOException("The server has returned an HTTP error " + statusLine.getStatusCode() + ": "
				+ statusLine.getReasonPhrase() + "'. Is the url valid? " + url);

		return response.getEntity().getContent();
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, byte[])
	 */
	public void put(String url, byte[] data) throws IOException
	{
		HttpPut put = new HttpPut(url);

		ByteArrayEntity entity = new ByteArrayEntity(data);
		put.setEntity(entity);

		HttpResponse response = this.client.execute(put);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new IOException("The server has returned an HTTP error " + statusLine.getStatusCode() + ": "
				+ statusLine.getReasonPhrase());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#delete(java.lang.String)
	 */
	public void delete(String url) throws IOException
	{
		HttpDelete delete = new HttpDelete(url);

		HttpResponse response = this.client.execute(delete);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new IOException("The server has returned an HTTP error " + statusLine.getStatusCode() + ": "
				+ statusLine.getReasonPhrase());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#move(java.lang.String, java.lang.String)
	 */
	public void move(String sourceUrl, String destinationUrl) throws IOException
	{
		HttpMove move = new HttpMove(sourceUrl, destinationUrl);

		HttpResponse response = this.client.execute(move);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new IOException("The server has returned an HTTP error " + statusLine.getStatusCode() + ": "
				+ statusLine.getReasonPhrase());
	}
}
