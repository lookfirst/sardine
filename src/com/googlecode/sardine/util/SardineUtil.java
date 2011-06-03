package com.googlecode.sardine.util;

import com.googlecode.sardine.model.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Basic utility code. I borrowed some code from the webdavlib for
 * parsing dates.
 *
 * @author jonstevens
 * @version $Id$
 */
public class SardineUtil
{
	/**
	 * Default namespace prefix
	 */
	public static final String CUSTOM_NAMESPACE_PREFIX = "s";

	/**
	 * Default namespace URI
	 */
	public static final String CUSTOM_NAMESPACE_URI = "SAR:";

	/**
	 * Default namespace prefix
	 */
	public static final String DEFAULT_NAMESPACE_PREFIX = "d";

	/**
	 * Default namespace URI
	 */
	public static final String DEFAULT_NAMESPACE_URI = "DAV:";

	/**
	 * Reusable context for marshalling and unmarshalling
	 */
	private static final JAXBContext JAXB_CONTEXT;

	static
	{
		try
		{
			JAXB_CONTEXT = JAXBContext.newInstance(ObjectFactory.class);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Date formats using for Date parsing.
	 */
	private static final SimpleDateFormat DATETIME_FORMATS[] =
			{
					new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
					new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
					new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
					new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
					new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
					new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
					new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
			};

	/**
	 * GMT timezone.
	 */
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	static
	{
		for (SimpleDateFormat format : DATETIME_FORMATS)
		{
			format.setTimeZone(UTC);
		}
	}

	/**
	 * Loops over all the possible date formats and tries to find the right one.
	 *
	 * @param value ISO date string
	 * @return Null if there is a parsing failure
	 */
	public static Date parseDate(String value)
	{
		if (value == null)
		{
			return null;
		}
		Date date = null;
		for (SimpleDateFormat format : DATETIME_FORMATS)
		{
			try
			{
				date = format.parse(value);
				break;
			}
			catch (ParseException e)
			{
				// We loop through this until we found a valid one.
			}
		}
		return date;
	}

	/**
	 * Creates an {@link Unmarshaller} from the {@link SardineUtil#JAXB_CONTEXT}.
	 * Note: the unmarshaller is not thread safe, so it must be created for every request.
	 *
	 * @return A new unmarshaller
	 * @throws IOException When there is a JAXB error
	 */
	public static Unmarshaller createUnmarshaller() throws IOException
	{
		try
		{
			return JAXB_CONTEXT.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			throw new IOException("Creating unmarshaller failed", e);
		}
	}

	/**
	 * @return A new marshaller
	 * @throws IOException When there is a JAXB error
	 */
	public static Marshaller createMarshaller() throws IOException
	{
		try
		{
			return JAXB_CONTEXT.createMarshaller();
		}
		catch (JAXBException e)
		{
			throw new IOException("Creating marshaller failed", e);
		}
	}

	/** */
	public static Document createDocument() throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			IOException failure = new IOException(e.getMessage());
			// Backward compatibility
			failure.initCause(e);
			throw failure;
		}
		return builder.newDocument();
	}

	/**
	 * @param jaxbElement An object from the model
	 * @return The XML string for the WebDAV request
	 * @throws IOException When there is a JAXB error
	 */
	public static String toXml(Object jaxbElement) throws IOException
	{
		StringWriter writer = new StringWriter();
		try
		{
			Marshaller marshaller = createMarshaller();
			marshaller.marshal(jaxbElement, writer);
		}
		catch (JAXBException e)
		{
			throw new IOException("Error converting element", e);
		}
		return writer.toString();
	}

	/** */
	public static Map<QName, String> toQName(Map<String, String> setProps)
	{
		if (setProps == null)
		{
			return Collections.emptyMap();
		}
		Map<QName, String> result = new HashMap<QName, String>(setProps.size());
		for (Map.Entry<String, String> entry : setProps.entrySet())
		{
			result.put(createQNameWithCustomNamespace(entry.getKey()), entry.getValue());
		}
		return result;
	}

	/** */
	public static List<QName> toQName(List<String> removeProps)
	{
		if (removeProps == null)
		{
			return Collections.emptyList();
		}
		List<QName> result = new ArrayList<QName>(removeProps.size());
		for (String entry : removeProps)
		{
			result.add(createQNameWithCustomNamespace(entry));
		}
		return result;
	}

	/** */
	public static QName createQNameWithCustomNamespace(String key)
	{
		return new QName(CUSTOM_NAMESPACE_URI, key, CUSTOM_NAMESPACE_PREFIX);
	}

	/** */
	public static QName createQNameWithDefaultNamespace(String key)
	{
		return new QName(DEFAULT_NAMESPACE_URI, key, DEFAULT_NAMESPACE_PREFIX);
	}

	/** */
	public static Element createElement(Document document, QName key)
	{
		return document.createElementNS(key.getNamespaceURI(), key.getPrefix() + ":" + key.getLocalPart());
	}
}
