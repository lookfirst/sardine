package com.github.sardine.report;

import com.github.sardine.DavResource;
import com.github.sardine.model.Limit;
import com.github.sardine.model.Multistatus;
import com.github.sardine.model.Prop;
import com.github.sardine.model.Response;
import com.github.sardine.model.SyncCollection;
import com.github.sardine.util.SardineUtil;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class SyncCollectionReport extends SardineReport<SyncCollectionReport.Result>
{
	private static final Logger log = Logger.getLogger(SardineReport.class.getName());

	public enum SyncLevel
	{
		LEVEL_1("1"),
		LEVEL_INFINITY("infinite");

		private final String value;

		SyncLevel(String value)
		{
			this.value = value;
		}

		public String toString()
		{
			return value;
		}
	}

	private final String syncToken;
	private final SyncLevel syncLevel;
	private final Set<QName> properties;
	private final Integer limit;

	public SyncCollectionReport(String syncToken, SyncLevel syncLevel, Set<QName> properties, Integer limit)
	{
		this.syncToken = syncToken;
		this.syncLevel = syncLevel;
		this.properties = properties;
		this.limit = limit;
	}

	@Override
	public Object toJaxb()
	{
		Prop prop = new Prop();
		List<Element> any = prop.getAny();
		for (QName entry : properties)
		{
			any.add(SardineUtil.createElement(entry));
		}

		SyncCollection syncCollection = new SyncCollection();
		syncCollection.setSyncToken(syncToken == null ? "" : syncToken);
		syncCollection.setSyncLevel(syncLevel.toString());
		syncCollection.setProp(prop);
		if (limit != null && limit > 0)
		{
			Limit l = new Limit();
			l.setNresults(BigInteger.valueOf(limit));
			syncCollection.setLimit(l);
		}
		return syncCollection;
	}

	@Override
	public Result fromMultistatus(Multistatus multistatus)
	{
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
				log.warning(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
			}
		}
		return new Result(resources, multistatus.getSyncToken());
	}

	public static class Result
	{
		private final List<DavResource> resources;
		private final String syncToken;

		Result(List<DavResource> resources, String syncToken)
		{
			this.resources = resources;
			this.syncToken = syncToken;
		}

		public List<DavResource> getResources()
		{
			return resources;
		}

		public String getSyncToken()
		{
			return syncToken;
		}
	}
}
