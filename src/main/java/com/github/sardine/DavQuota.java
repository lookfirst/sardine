package com.github.sardine;

import com.github.sardine.model.Prop;
import com.github.sardine.model.Response;

/**
 * Quota and Size Properties
 *
 * @author Alexander Makarov
 */
public class DavQuota {
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

	public DavQuota(Response response) {
		Prop prop = response.getPropstat().get(0).getProp();
		this.quotaAvailableBytes = Long.valueOf(prop.getQuotaAvailableBytes().getContent().get(0));
		this.quotaUsedBytes = Long.valueOf(prop.getQuotaUsedBytes().getContent().get(0));
	}

	public long getQuotaAvailableBytes() {
		return quotaAvailableBytes;
	}

	public long getQuotaUsedBytes() {
		return quotaUsedBytes;
	}

}
