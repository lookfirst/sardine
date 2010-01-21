package com.googlecode.sardine;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.googlecode.sardine.util.SardineException;

/**
 * The perfect name for a class. Provides the
 * static methods for working with the Sardine
 * interface.
 *
 * @author jonstevens
 */
public class SardineFactory
{
	/**
	 * @return the JAXBContext
	 */
	public static JAXBContext getContext()
	{
		return Factory.instance().getContext();
	}

	/**
	 * @return the JAXB Unmarshaller
	 */
	public static Unmarshaller getUnmarshaller()
	{
		return Factory.instance().getUnmarshaller();
	}

	/**
	 *
	 */
	public static Sardine begin() throws SardineException
	{
		return Factory.instance().begin();
	}

	/**
	 * Pass in a HTTP Auth username/password for being used with all
	 * connections
	 */
	public static Sardine begin(String username, String password) throws SardineException
	{
		return Factory.instance().begin(username, password);
	}

	/**
	 * for testing
	 */
	public static void main(String[] args) throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		List<DavResource> resources = sardine.getResources("http://webdav.prod.365.kink.y/cybernetentertainment/imagedb/7761/v/h/320/");
		for (DavResource res : resources)
			System.out.println(res);
	}
}
