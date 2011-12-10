package com.googlecode.sardine;

import com.googlecode.sardine.model.Principal;

import javax.xml.namespace.QName;

public class DavPrincipal
{
	public static final String KEY_SELF = "self";
	public static final String KEY_UNAUTHENTICATED = "unauthenticated";
	public static final String KEY_AUTHENTICATED = "authenticated";
	public static final String KEY_ALL = "all";

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
	public static enum PrincipalType
	{
		/**
		 * Principal is a String reference to an existing principal (url)
		 */
		HREF,
		/**
		 * Principal is String as one of the special values: all, authenticated, unauthenticated, self
		 */
		KEY,
		/**
		 * Principal is QNAME referencing a property (eg: DAV::owner, custom:someGroupId) that itself contains a href to
		 * an existing principal
		 */
		PROPERTY

	}

	private final PrincipalType principalType;
	private final String value;
	private final QName property;
	private final String displayName;

	public DavPrincipal(PrincipalType principalType, String value, String name)
	{
		this(principalType, value, null, name);
	}

	protected DavPrincipal(PrincipalType principalType, String value, QName property, String name)
	{
		if (value != null && principalType == PrincipalType.PROPERTY)
			throw new IllegalArgumentException("Principal type property can't have a string value");
		if (property != null && principalType != PrincipalType.PROPERTY)
			throw new IllegalArgumentException("Principal type " + principalType.name() + " property is not allowed to have a QName property");
		this.principalType = principalType;
		this.value = value;
		this.property = property;
		this.displayName = name;
	}

	public DavPrincipal(PrincipalType principalType, QName property, String name)
	{
		this(principalType, null, property, name);
	}

	public DavPrincipal(Principal principal)
	{
		this.displayName = null;
		if (principal.getHref() != null)
		{
			this.principalType = PrincipalType.HREF;
			this.value = principal.getHref();
			this.property = null;
		}
		else if (principal.getProperty() != null)
		{
			this.principalType = PrincipalType.PROPERTY;
			this.value = null;
			this.property = new QName(principal.getProperty().getProperty().getNamespaceURI(),
					principal.getProperty().getProperty().getLocalName());
		}
		else if (principal.getAll() != null || principal.getAuthenticated() != null || principal.getUnauthenticated() != null || principal.getSelf() != null)
		{
			this.principalType = PrincipalType.KEY;
			this.property = null;
			if (principal.getAll() != null)
				this.value = KEY_ALL;
			else if (principal.getAuthenticated() != null)
				this.value = KEY_AUTHENTICATED;
			else if (principal.getUnauthenticated() != null)
				this.value = KEY_UNAUTHENTICATED;
			else
				this.value = KEY_SELF;
		}
		else
		{
			this.principalType = null;
			this.value = null;
			this.property = null;
		}
	}

	public PrincipalType getPrincipalType()
	{
		return principalType;
	}

	public String getValue()
	{
		return value;
	}

	public QName getProperty()
	{
		return property;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String toString()
	{
		return "[principalType=" + principalType + ", value=" + value
				+ ", property=" + property + ", displayName=" + displayName + "]";

	}
}
