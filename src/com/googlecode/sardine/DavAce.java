package com.googlecode.sardine;

import com.googlecode.sardine.model.Ace;
import com.googlecode.sardine.model.Privilege;
import com.googlecode.sardine.model.SimplePrivilege;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

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
	private final String principal;

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

	private final QName property;

	public DavAce(Ace ace)
	{
		principal = ace.getPrincipal().getHref();
		if (ace.getPrincipal().getProperty() != null)
		{
			property = new QName(ace.getPrincipal().getProperty().getProperty().getNamespaceURI(),
					ace.getPrincipal().getProperty().getProperty().getLocalName());
		}
		else
		{
			property = null;
		}
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
	}

	public String getPrincipal()
	{
		return principal;
	}

	public QName getProperty()
	{
		return property;
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
}
