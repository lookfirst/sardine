package com.googlecode.sardine;

import com.googlecode.sardine.util.SardineException;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * The factory class is responsible for instantiating the JAXB stuff
 * as well as the instance to SardineImpl.
 *
 * @author jonstevens
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
	public Sardine begin() throws SardineException
	{
		return this.begin(null, null, null, null, null);
	}

	/** */
	public Sardine begin(SSLSocketFactory sslSocketFactory) throws SardineException
	{
		return this.begin(null, null, sslSocketFactory);
	}

	/** */
	public Sardine begin(String username, String password) throws SardineException
	{
		return this.begin(username, password, null, null, null);
	}

	/** */
	public Sardine begin(String username, String password, Integer port) throws SardineException
	{
		return this.begin(username, password, null, null, port);
	}

	/** */
	public Sardine begin(String username, String password, HttpRoutePlanner routePlanner) throws SardineException
	{
		return this.begin(username, password, null, routePlanner);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory) throws SardineException
	{
		return this.begin(username, password, sslSocketFactory, null, null);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner) throws SardineException
	{
		return this.begin(username, password, sslSocketFactory, routePlanner, null);
	}

	/** */
	public Sardine begin(String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner, Integer port) throws SardineException
	{
		return new SardineImpl(username, password, sslSocketFactory, routePlanner, port);
	}
}
