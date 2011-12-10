package com.googlecode.sardine;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.sardine.model.*;
import com.googlecode.sardine.util.SardineUtil;

/**
 * An Access control element (ACE) either grants or denies a particular set of (non-
 * abstract) privileges for a particular principal.
 *
 * @author David Delbecq
 * @version $Id$
 */
public class DavAce
{
	/**
	 * A "principal" is a distinct human or computational actor that
	 * initiates access to network resources.  In this protocol, a
	 * principal is an HTTP resource that represents such an actor.
	 * <p/>
	 * The DAV:principal element identifies the principal to which this ACE
	 * applies.
	 * <p/>
	 * <!ELEMENT principal (href | all | authenticated | unauthenticated
	 * | property | self)>
	 * <p/>
	 * The current user matches DAV:href only if that user is authenticated
	 * as being (or being a member of) the principal identified by the URL
	 * contained by that DAV:href.
	 * <p/>
	 * Either a href or one of all,authenticated,unauthenticated,property,self.
	 * <p/>
	 * DAV:property not supported.
	 */
	private final DavPrincipal principal;

	/**
	 * List of granted privileges.
	 */
	private final List<String> granted;

	/**
	 * List of denied privileges.
	 */
	private final List<String> denied;

	/**
	 * The presence of a DAV:inherited element indicates that this ACE is
	 * inherited from another resource that is identified by the URL
	 * contained in a DAV:href element.  An inherited ACE cannot be modified
	 * directly, but instead the ACL on the resource from which it is
	 * inherited must be modified.
	 * <p/>
	 * Null or a href to the inherited resource.
	 */
	private final String inherited;

	private final boolean isprotected;


	public DavAce(DavPrincipal principal)
	{
		this.principal = principal;
		this.granted = new ArrayList<String>();
		this.denied = new ArrayList<String>();
		this.inherited = null;
		this.isprotected = false;
	}

	public DavAce(Ace ace)
	{
		principal = new DavPrincipal(ace.getPrincipal());

		granted = new ArrayList<String>();
		denied = new ArrayList<String>();
		if (ace.getGrant() != null)
		{
			for (Privilege p : ace.getGrant().getPrivilege())
			{
				for (Object o : p.getContent())
				{
					if (o instanceof SimplePrivilege)
					{
						granted.add(o.getClass().getAnnotation(XmlRootElement.class).name());
					}
				}
			}
		}
		if (ace.getDeny() != null)
		{
			for (Privilege p : ace.getDeny().getPrivilege())
			{
				for (Object o : p.getContent())
				{
					if (o instanceof SimplePrivilege)
					{
						denied.add(o.getClass().getAnnotation(XmlRootElement.class).name());
					}
				}
			}
		}
		if (ace.getInherited() != null)
		{
			inherited = ace.getInherited().getHref();
		}
		else
		{
			inherited = null;
		}
		this.isprotected = (ace.getProtected() != null);
	}

	public DavPrincipal getPrincipal()
	{
		return principal;
	}

	public List<String> getGranted()
	{
		return granted;
	}

	public List<String> getDenied()
	{
		return denied;
	}

	public String getInherited()
	{
		return inherited;
	}

	public boolean isProtected()
	{
		return isprotected;
	}

	public Ace toModel()
	{
		Ace ace = new Ace();
		Principal p = new Principal();
		switch (principal.getPrincipalType())
		{
			case HREF:
				p.setHref(principal.getValue());
				break;
			case PROPERTY:
				p.setProperty(new Property());
				p.getProperty().setProperty(SardineUtil.createElement(principal.getProperty()));
				break;
			case KEY:
				if (DavPrincipal.KEY_ALL.equals(principal.getValue()))
				{
					p.setAll(new All());
				}
				else if (DavPrincipal.KEY_AUTHENTICATED.equals(principal.getValue()))
				{
					p.setAuthenticated(new Authenticated());
				}
				else if (DavPrincipal.KEY_UNAUTHENTICATED.equals(principal.getValue()))
				{
					p.setUnauthenticated(new Unauthenticated());
				}
				else if (DavPrincipal.KEY_SELF.equals(principal.getValue()))
				{
					p.setSelf(new Self());
				}
		}
		ace.setPrincipal(p);
		if (granted != null && granted.size() > 0)
		{
			ace.setGrant(new Grant());
			ace.getGrant().setPrivilege(toPrivilege(granted));
		}
		if (denied != null && denied.size() > 0)
		{
			ace.setDeny(new Deny());
			ace.getDeny().setPrivilege(toPrivilege(denied));
		}
		return ace;
	}

	private List<Privilege> toPrivilege(List<String> rights)
	{
		List<Privilege> privileges = new ArrayList<Privilege>();
		for (String right : rights)
		{
			Privilege p = new Privilege();
			if ("all".equals(right))
			{
				p.getContent().add(new All());
			}
			else if ("bind".equals(right))
			{
				p.getContent().add(new Bind());
			}
			else if ("read".equals(right))
			{
				p.getContent().add(new Read());
			}
			else if ("read-acl".equals(right))
			{
				p.getContent().add(new ReadAcl());
			}
			else if ("read-current-user-privilege-set".equals(right))
			{
				p.getContent().add(new ReadCurrentUserPrivilegeSet());
			}
			else if ("unbind".equals(right))
			{
				p.getContent().add(new UnBind());
			}
			else if ("unlock".equals(right))
			{
				p.getContent().add(new Unlock());
			}
			else if ("write".equals(right))
			{
				p.getContent().add(new Write());
			}
			else if ("write-content".equals(right))
			{
				p.getContent().add(new WriteContent());
			}
			else if ("write-properties".equals(right))
			{
				p.getContent().add(new WriteProperties());
			}
			else
			{
				continue;
			}
			privileges.add(p);
		}
		return privileges;
	}
}
