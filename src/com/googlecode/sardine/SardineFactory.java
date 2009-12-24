package com.googlecode.sardine;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * The perfect name for a class.
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
	 * @return
	 */
	public static Sardine begin()
	{
		return Factory.instance().begin();
	}

	public static void main(String[] args) throws Exception
	{
		Sardine sardine = SardineFactory.begin();
		List<DavResource> resources = sardine.getResources("http://webdav.prod.365.kink.y/");
		System.out.println("resources: " + resources);
	}
}
