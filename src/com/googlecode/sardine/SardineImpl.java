package com.googlecode.sardine;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import com.googlecode.sardine.model.Getcontentlength;
import com.googlecode.sardine.model.Getcontenttype;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.Response;
import com.googlecode.sardine.util.SardineUtil;
import com.googlecode.sardine.util.SardineUtil.HttpPropFind;

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
		URL urlObj = new URL(url);
		String path = urlObj.getPath();

		HttpPropFind pf = new HttpPropFind(url);
		HttpResponse response = this.client.execute(pf);

		try
		{
			Multistatus r = (Multistatus) this.factory.getUnmarshaller().unmarshal(response.getEntity().getContent());
			List<Response> responses = r.getResponse();

			List<DavResource> resources = new ArrayList<DavResource>(responses.size());

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
		catch (JAXBException ex)
		{
			throw new IOException(ex);
		}
	}

}
