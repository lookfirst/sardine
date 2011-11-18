package com.googlecode.sardine;

import com.googlecode.sardine.model.Ace;
import com.googlecode.sardine.model.Acl;
import com.googlecode.sardine.model.Group;
import com.googlecode.sardine.model.Owner;
import com.googlecode.sardine.model.Propstat;
import com.googlecode.sardine.model.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe access rights on a remote server. An access control list (ACL)
 * is a list of access control elements that define access
 * control to a particular resource.
 *
 * @author David Delbecq
 * @version $Id$
 */
public class DavAcl
{
	/**
	 * The value of the DAV:owner property is a single DAV:href XML element
	 * containing the URL of the principal that owns this resource.
	 */
	private final String owner;

	/**
	 * This property identifies a particular principal as being the "group"
	 * of the resource.  This property is commonly found on repositories
	 * that implement the Unix privileges model.
	 */
	private final String group;

	/**
	 *
	 */
	private List<DavAce> aces;

	public DavAcl(Response response)
	{
		this.owner = getOwner(response);
		this.group = getGroup(response);
		this.aces = getAces(response);
	}

	public String getGroup()
	{
		return group;
	}

	public String getOwner()
	{
		return owner;
	}

	public List<DavAce> getAces()
	{
		return aces;
	}

	private String getOwner(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			Owner o = propstat.getProp().getOwner();
			if (o != null)
			{
				if (o.getUnauthenticated() != null)
					return "unauthenticated";
				else if (o.getHref() != null)
					return o.getHref();
			}
		}
		return null;
	}

	private String getGroup(Response response)
	{
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			Group o = propstat.getProp().getGroup();
			if (o != null)
			{
				if (o.getHref() != null)
					return o.getHref();
			}
		}
		return null;
	}

	private List<DavAce> getAces(Response response)
	{
		ArrayList<DavAce> result = new ArrayList<DavAce>();
		List<Propstat> list = response.getPropstat();
		if (list.isEmpty())
		{
			return null;
		}
		for (Propstat propstat : list)
		{
			Acl a = propstat.getProp().getAcl();
			if (a != null && a.getAce() != null)
			{
				for (Ace ace : a.getAce())
				{
					result.add(new DavAce(ace));
				}
			}
		}
		return result;
	}
}
