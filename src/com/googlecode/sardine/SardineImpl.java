package com.googlecode.sardine;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Response;

/**
 *
 * @author jonstevens
 */
public class SardineImpl implements Sardine
{
	/** */
	Factory factory;

	/** */
	DefaultHttpClient client;

	/**
	 * Date formats using for Date parsing.
	 */
	static final SimpleDateFormat formats[] =
	{
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
			new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
			new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US)
	};

	/**
	 * GMT timezone.
	 */
	final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");

	static
	{
		for (SimpleDateFormat format : formats)
		{
			format.setTimeZone(gmtZone);
		}
	}

	/** */
	public SardineImpl(Factory factory)
	{
		this.factory = factory;
		this.client = new DefaultHttpClient();
	}

	/**
	 * Getting a directory listing.
	 *
	 * @throws IOException
	 */
	public List<DavResource> getResources(String url) throws IOException
	{
		HttpPropFind pf = new HttpPropFind(url);
		pf.addHeader("Depth", "1");
		HttpResponse response = this.client.execute(pf);

		try
		{
			Multistatus r = (Multistatus) this.factory.getUnmarshaller().unmarshal(response.getEntity().getContent());
			List<Response> responses = r.getResponse();

			List<DavResource> resources = new ArrayList<DavResource>(responses.size());

			for (Response resp : responses)
			{
				// Lots of horrible assumptions
				String href = resp.getHref().get(0).replace("/", "");
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

				DavResource dr = new DavResource(url, href, this.parseDate(creationdate), this.parseDate(modifieddate), contentType, Long.valueOf(contentLength));

				resources.add(dr);
			}
			return resources;
		}
		catch (JAXBException ex)
		{
			throw new IOException(ex);
		}
	}

	protected Date parseDate(String dateValue)
	{
		// TODO: move to the common util package related to http.
		Date date = null;
		for (int i = 0; (date == null) && (i < formats.length); i++)
		{
			try
			{
				synchronized (formats[i])
				{
					date = formats[i].parse(dateValue);
				}
			}
			catch (ParseException e)
			{
			}
		}

		return date;
	}

	public static class HttpPropFind extends HttpGet
	{
		public HttpPropFind(String url)
		{
			super(url);
		}

		@Override
		public String getMethod()
		{
			return "PROPFIND";
		}
	}
}
