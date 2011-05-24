package com.googlecode.sardine.util;

import com.googlecode.sardine.model.Allprop;
import com.googlecode.sardine.model.ObjectFactory;
import com.googlecode.sardine.model.Prop;
import com.googlecode.sardine.model.Propertyupdate;
import com.googlecode.sardine.model.Propfind;
import com.googlecode.sardine.model.Remove;
import com.googlecode.sardine.model.Set;
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
	 *
	 */
	public static final String DEFAULT_NAMESPACE_PREFIX = "S";

	/**
	 *
	 */
	public static final String DEFAULT_NAMESPACE_URI = "SAR:";

	/** */
	private static final JAXBContext context;

	static
	{
		try
		{
			context = JAXBContext.newInstance(ObjectFactory.class);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Date formats using for Date parsing.
	 */
	static final SimpleDateFormat formats[] =
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
	final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");

	static
	{
		for (SimpleDateFormat format : formats)
		{
			format.setTimeZone(gmtZone);
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
		for (final SimpleDateFormat format : formats)
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
	 * Build WebDAV <code>PROPFIND</code> entity.
	 *
	 * @return The XML body
	 * @throws IOException When there is a JAXB error
	 */
	public static String getPropfindEntity() throws IOException
	{
		final Propfind propfind = new Propfind();
		propfind.setAllprop(new Allprop());
		return toXml(propfind);
	}

	/**
	 * Build WebDAV <code>PROPPATCH</code> entity.
	 * <p/>
	 * Creates a {@link Propertyupdate} element containing all properties to set from setProps and all properties to
	 * remove from removeProps. Note this method will use a {@link #DEFAULT_NAMESPACE_URI} as
	 * namespace and {@link #DEFAULT_NAMESPACE_PREFIX} as prefix.
	 *
	 * @param addProperties	Properties to add
	 * @param removeProperties Properties to remove
	 * @return The XML body
	 * @throws IOException When there is a JAXB error
	 */
	public static String getPropPatchEntity(Map<String, String> addProperties, List<String> removeProperties) throws IOException
	{
		final Document document = createDocument();

		// Element
		final Propertyupdate proppatch = new Propertyupdate();

		// Add properties
		{
			final Set set = new Set();
			proppatch.getRemoveOrSet().add(set);
			final Prop prop = new Prop();
			final List<Element> any = prop.getAny();
			for (Map.Entry<QName, String> entry : toQName(addProperties).entrySet())
			{
				final Element element = createElement(document, entry.getKey());
				element.setTextContent(entry.getValue());
				any.add(element);
			}
			set.setProp(prop);
		}

		// Remove properties
		{
			final Remove remove = new Remove();
			proppatch.getRemoveOrSet().add(remove);
			final Prop prop = new Prop();
			final List<Element> any = prop.getAny();
			for (QName entry : toQName(removeProperties))
			{
				final Element element = createElement(document, entry);
				any.add(element);
			}
			remove.setProp(prop);
		}
		return toXml(proppatch);
	}

	/**
	 * Creates an {@link Unmarshaller} from the {@link SardineUtil#context}.
	 * Note: the unmarshaller is not thread safe, so it must be created for every request.
	 *
	 * @return A new unmarshaller
	 * @throws IOException When there is a JAXB error
	 */
	public static Unmarshaller createUnmarshaller() throws IOException
	{
		try
		{
			return context.createUnmarshaller();
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
			return context.createMarshaller();
		}
		catch (JAXBException e)
		{
			throw new IOException("Creating marshaller failed", e);
		}
	}

	private static Document createDocument() throws IOException
	{
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder;
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
	public static String toXml(final Object jaxbElement) throws IOException
	{
		final StringWriter writer = new StringWriter();
		try
		{
			final Marshaller marshaller = createMarshaller();
			marshaller.marshal(jaxbElement, writer);
		}
		catch (JAXBException e)
		{
			throw new IOException("Error converting element", e);
		}
		return writer.toString();
	}

	/**
	 * Creates a simple Map from the given custom properties of a response. This implementation does not take
	 * namespaces into account.
	 *
	 * @param elements custom properties.
	 * @return a map from the custom properties.
	 */
	public static Map<String, String> extractCustomProps(List<Element> elements)
	{
		Map<String, String> customPropsMap = new HashMap<String, String>(elements.size());

		for (Element element : elements)
		{
			String key = element.getLocalName();
			customPropsMap.put(key, element.getTextContent());
		}

		return customPropsMap;
	}

	private static Map<QName, String> toQName(Map<String, String> setProps)
	{
		if (setProps == null)
		{
			return Collections.emptyMap();
		}
		final HashMap<QName, String> result = new HashMap<QName, String>(setProps.size());
		for (final Map.Entry<String, String> entry : setProps.entrySet())
		{
			result.put(newQNameWithDefaultNamespace(entry.getKey()), entry.getValue());
		}
		return result;
	}

	private static List<QName> toQName(List<String> removeProps)
	{
		if (removeProps == null)
		{
			return Collections.emptyList();
		}
		final ArrayList<QName> result = new ArrayList<QName>(removeProps.size());
		for (final String entry : removeProps)
		{
			result.add(newQNameWithDefaultNamespace(entry));
		}
		return result;
	}

	private static QName newQNameWithDefaultNamespace(String key)
	{
		return new QName(DEFAULT_NAMESPACE_URI, key, DEFAULT_NAMESPACE_PREFIX);
	}

	private static Element createElement(Document document, QName key)
	{
		return document.createElementNS(key.getNamespaceURI(), key.getPrefix() + ":" + key.getLocalPart());
	}
}
