package com.github.sardine;

import com.github.sardine.model.Prop;
import com.github.sardine.model.Propstat;
import com.github.sardine.model.Response;

import java.util.List;

/**
 * Quota and Size Properties
 *
 * @author Alexander Makarov
 */
public class DavQuota
{
	/**
	 * The DAV:quota-available-bytes property value is the value in octets
	 * representing the amount of additional disk space beyond the current
	 * allocation that can be allocated to this resource before further
	 * allocations will be refused.
	 */
	private final long quotaAvailableBytes;

	/**
	 * The DAV:quota-used-bytes value is the value in octets representing
	 * the amount of space used by this resource and possibly a number of
	 * other similar resources, where the set of "similar" meets at least
	 * the criterion that allocating space to any resource in the set will
	 * count against the DAV:quota-available-bytes.
	 */
	private final long quotaUsedBytes;

	public DavQuota(Response response)
	{
		this.quotaAvailableBytes = this.getAvailable(response);
		this.quotaUsedBytes = this.getUsed(response);
	}

	private long getAvailable(Response response) {
		final List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return Long.MAX_VALUE;
		}
		else
		{
			for (Propstat propstat : list)
			{
				final Prop prop = propstat.getProp();
				if(null == prop) {
					continue;
				}
				if(null == prop.getQuotaAvailableBytes()) {
					continue;
				}
				if (prop.getQuotaAvailableBytes().getContent().isEmpty())
				{
					continue;
				}
				return Long.valueOf(prop.getQuotaAvailableBytes().getContent().get(0));
			}
			return Long.MAX_VALUE;
		}
	}

	private long getUsed(Response response) {
		final List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return 0L;
		}
		else
		{
			for (Propstat propstat : list)
			{
				final Prop prop = propstat.getProp();
				if(null == prop) {
					continue;
				}
				if(null == prop.getQuotaUsedBytes()) {
					continue;
				}
				if (prop.getQuotaUsedBytes().getContent().isEmpty())
				{
					continue;
				}
				return Long.valueOf(prop.getQuotaUsedBytes().getContent().get(0));
			}
			return 0L;
		}
	}

	public long getQuotaAvailableBytes()
	{
		return quotaAvailableBytes;
	}

	public long getQuotaUsedBytes()
	{
		return quotaUsedBytes;
	}

}
