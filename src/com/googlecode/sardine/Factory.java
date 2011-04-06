package com.googlecode.sardine;

import com.googlecode.sardine.impl.SardineImpl;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * The factory class is responsible for instantiating the default implementation of Sardine.
 *
 * @author jonstevens
 * @version $Id$
 */
public class Factory
{
	/** */
	protected static Factory instance = new Factory();

	/** */
	protected static Factory instance()
	{
		return instance;
	}

	/** */
	public Sardine begin()
	{
		return this.begin(null, null, null, null, null);
	}

	/** */
	public Sardine begin(SSLSocketFactory sslSocketFactory)
	{
		return this.begin(null, null, sslSocketFactory);
	}

	/** */
	public Sardine begin(String username, String password)
	{
		return this.begin(username, password, null, null, null);
	}

	/** */
	public Sardine begin(String username, String password, Integer port)
	{
		return this.begin(username, password, null, null, port);
	}

	/** */
	public Sardine begin(String username, String password, HttpRoutePlanner routePlanner)
	{
		return this.begin(username, password, null, routePlanner);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory)
	{
		return this.begin(username, password, sslSocketFactory, null, null);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner)
	{
		return this.begin(username, password, sslSocketFactory, routePlanner, null);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner, Integer port)
	{
		return new SardineImpl(username, password, sslSocketFactory, routePlanner, port);
	}
}
