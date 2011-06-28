package com.googlecode.sardine.util;

import com.googlecode.sardine.model.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
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

	public static <T> T unmarshal(InputStream in) throws IOException
	{
		Unmarshaller unmarshaller = createUnmarshaller();
		try
		{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			try
			{
				reader.setFeature(
						"http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
			}
			catch (SAXException e)
			{
				; //Not all parsers will support this attribute
			}
			try
			{
				reader.setFeature(
						"http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
			}
			catch (SAXException e)
			{
				; //Not all parsers will support this attribute
			}
			try
			{
				reader.setFeature(
						"http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
			}
			catch (SAXException e)
			{
				; //Not all parsers will support this attribute
			}
			try
			{
				reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
			}
			catch (SAXException e)
			{
				; //Not all parsers will support this attribute
			}
			return (T) unmarshaller.unmarshal(new SAXSource(reader, new InputSource(in)));
		}
		catch (SAXException e)
		{
			IOException failure = new IOException(e.getMessage());
			// Backward compatibility
			failure.initCause(e);
			throw failure;
		}
		catch (JAXBException e)
		{
			IOException failure = new IOException(e.getMessage());
			// Backward compatibility
			failure.initCause(e);
			throw failure;
		}
	}

	/**
	 * Creates an {@link Unmarshaller} from the {@link SardineUtil#JAXB_CONTEXT}.
	 * Note: the unmarshaller is not thread safe, so it must be created for every request.
	 *
	 * @return A new unmarshaller
	 * @throws IOException When there is a JAXB error
	 */
	private static Unmarshaller createUnmarshaller() throws IOException
	{
		try
		{
			return JAXB_CONTEXT.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			IOException failure = new IOException(e.getMessage());
			// Backward compatibility
			failure.initCause(e);
			throw failure;
		}
	}

	/**
	 * @return A new marshaller
	 * @throws IOException When there is a JAXB error
	 */
	private static Marshaller createMarshaller() throws IOException
	{
		try
		{
			return JAXB_CONTEXT.createMarshaller();
		}
		catch (JAXBException e)
		{
			IOException failure = new IOException(e.getMessage());
			// Backward compatibility
			failure.initCause(e);
			throw failure;
		}
	}

	/**
	 * @return New XML document from the default document builder factory.
	 */
	private static Document createDocument()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e.getMessage(), e);
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
			IOException failure = new IOException(e.getMessage());
			// Backward compatibility
			failure.initCause(e);
			throw failure;
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

	/**
	 * @param key Local element name.
	 */
	public static QName createQNameWithCustomNamespace(String key)
	{
		return new QName(CUSTOM_NAMESPACE_URI, key, CUSTOM_NAMESPACE_PREFIX);
	}

	/**
	 * @param key Local element name.
	 */
	public static QName createQNameWithDefaultNamespace(String key)
	{
		return new QName(DEFAULT_NAMESPACE_URI, key, DEFAULT_NAMESPACE_PREFIX);
	}

	/**
	 * @param key Fully qualified element name.
	 */
	public static Element createElement(QName key)
	{
		return createDocument().createElementNS(key.getNamespaceURI(), key.getPrefix() + ":" + key.getLocalPart());
	}
}
