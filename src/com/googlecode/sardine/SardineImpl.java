package com.googlecode.sardine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
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
import com.googlecode.sardine.util.SardineException;
import com.googlecode.sardine.util.SardineUtil;
import com.googlecode.sardine.util.SardineUtil.HttpCopy;
import com.googlecode.sardine.util.SardineUtil.HttpMkCol;
import com.googlecode.sardine.util.SardineUtil.HttpMove;
import com.googlecode.sardine.util.SardineUtil.HttpPropFind;

/**
 * Implementation of the Sardine interface. This
 * is where the meat of the Sardine library lives.
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

		if ((username != null) && (password != null))
			this.client.getCredentialsProvider().setCredentials(
	                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
	                new UsernamePasswordCredentials(username, password));
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#getResources(java.lang.String)
	 */
	public List<DavResource> getResources(String url) throws SardineException
	{
		HttpPropFind propFind = new HttpPropFind(url);
		propFind.setEntity(SardineUtil.getResourcesEntity());

		HttpResponse response = this.executeWrapper(propFind);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new SardineException("Failed to get resources. Is the url valid?", url,
					statusLine.getStatusCode(), statusLine.getReasonPhrase());

		// Process the response from the server.
		Multistatus multistatus = SardineUtil.getMulitstatus(this.factory.getUnmarshaller(), response, url);

		List<Response> responses = multistatus.getResponse();

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
	public InputStream getInputStream(String url) throws SardineException
	{
		HttpGet get = new HttpGet(url);

		HttpResponse response = this.executeWrapper(get);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new SardineException(url, statusLine.getStatusCode(), statusLine.getReasonPhrase());

		try
		{
			return response.getEntity().getContent();
		}
		catch (IOException ex)
		{
			throw new SardineException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#put(java.lang.String, byte[])
	 */
	public void put(String url, byte[] data) throws SardineException
	{
		HttpPut put = new HttpPut(url);

		ByteArrayEntity entity = new ByteArrayEntity(data);
		put.setEntity(entity);

		HttpResponse response = this.executeWrapper(put);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new SardineException(url, statusLine.getStatusCode(), statusLine.getReasonPhrase());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#delete(java.lang.String)
	 */
	public void delete(String url) throws SardineException
	{
		HttpDelete delete = new HttpDelete(url);

		HttpResponse response = this.executeWrapper(delete);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new SardineException(url, statusLine.getStatusCode(), statusLine.getReasonPhrase());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#move(java.lang.String, java.lang.String)
	 */
	public void move(String sourceUrl, String destinationUrl) throws SardineException
	{
		HttpMove move = new HttpMove(sourceUrl, destinationUrl);

		HttpResponse response = this.executeWrapper(move);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new SardineException("sourceUrl: " + sourceUrl + ", destinationUrl: " + destinationUrl,
					statusLine.getStatusCode(), statusLine.getReasonPhrase());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#copy(java.lang.String, java.lang.String)
	 */
	public void copy(String sourceUrl, String destinationUrl)
		throws SardineException
	{
		HttpCopy copy = new HttpCopy(sourceUrl, destinationUrl);

		HttpResponse response = this.executeWrapper(copy);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new SardineException("sourceUrl: " + sourceUrl + ", destinationUrl: " + destinationUrl,
					statusLine.getStatusCode(), statusLine.getReasonPhrase());
	}

	/*
	 * (non-Javadoc)
	 * @see com.googlecode.sardine.Sardine#createDirectory(java.lang.String)
	 */
	public void createDirectory(String url) throws SardineException
	{
		HttpMkCol mkcol = new HttpMkCol(url);
		mkcol.setEntity(SardineUtil.createDirectoryEntity());

		HttpResponse response = this.executeWrapper(mkcol);

		StatusLine statusLine = response.getStatusLine();
		if (!SardineUtil.isGoodResponse(statusLine.getStatusCode()))
			throw new SardineException(url, statusLine.getStatusCode(), statusLine.getReasonPhrase());
	}

	/**
	 * Small wrapper around HttpClient.execute() in order to wrap the IOException
	 * into a SardineException.
	 */
	private HttpResponse executeWrapper(HttpRequestBase base) throws SardineException
	{
		try
		{
			return this.client.execute(base);
		}
		catch (IOException ex)
		{
			throw new SardineException(ex);
		}
	}
}
