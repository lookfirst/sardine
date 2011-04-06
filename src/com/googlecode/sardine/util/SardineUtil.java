package com.googlecode.sardine.util;

import com.googlecode.sardine.impl.SardineException;
import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.ObjectFactory;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Basic utility code. I borrowed some code from the webdavlib for
 * parsing dates.
 *
 * @author jonstevens
 */
public class SardineUtil
{

	/** */
	public final static JAXBContext CONTEXT;

	static
	{
		try
		{
			CONTEXT = JAXBContext.newInstance(ObjectFactory.class);
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * cached version of getResources() webdav xml GET request
	 */
	private static StringEntity GET_RESOURCES = null;

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
	 * @param dateValue
	 */
	public static Date parseDate(String dateValue)
	{
		if (dateValue == null)
		{
			return null;
		}

		Date date = null;
		for (int i = 0; (date == null) && (i < formats.length); i++)
		{
			try
			{
				synchronized (formats[i])
				{
					date = formats[i].parse(dateValue);
				}
			}
			catch (ParseException e)
			{
			}
		}

		return date;
	}

	/**
	 * Stupid wrapper cause it needs to be in a try/catch
	 */
	public static StringEntity getResourcesEntity()
	{
		if (GET_RESOURCES == null)
		{
			try
			{
				GET_RESOURCES = new StringEntity("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
						"<propfind xmlns=\"DAV:\">\n" +
						"	<allprop/>\n" +
						"</propfind>", "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				// Ignored
			}
		}

		return GET_RESOURCES;
	}

	/**
	 * Build PROPPATCH entity.
	 */
	public static StringEntity getResourcePatchEntity(Map<String, String> setProps, List<String> removeProps)
	{
		StringEntity patchEntity = null;

		try
		{
			StringBuffer buf = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
			buf.append("<D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\">\n");

			if (setProps != null)
			{
				buf.append("<D:set>\n");
				buf.append("<D:prop>\n");
				for (Map.Entry<String, String> prop : setProps.entrySet())
				{
					buf.append("<S:");
					buf.append(prop.getKey()).append(">");
					buf.append(prop.getValue()).append("</S:");
					buf.append(prop.getKey()).append(">\n");
				}
				buf.append("</D:prop>\n");
				buf.append("</D:set>\n");
			}

			if (removeProps != null)
			{
				buf.append("<D:remove>\n");
				buf.append("<D:prop>\n");
				for (String removeProp : removeProps)
				{
					buf.append("<S:");
					buf.append(removeProp).append("/>");
				}
				buf.append("</D:prop>\n");
				buf.append("</D:remove>\n");
			}

			buf.append("</D:propertyupdate>\n");

			patchEntity = new StringEntity(buf.toString());

		}
		catch (UnsupportedEncodingException e)
		{
			// Ignored
		}

		return patchEntity;
	}

	/**
	 * Helper method for getting the Multistatus response processor.
	 */
	public static Multistatus getMultistatus(InputStream stream)
			throws SardineException
	{
		try
		{
			return (Multistatus) createUnmarshaller().unmarshal(stream);
		}
		catch (JAXBException ex)
		{
			throw new SardineException("Problem unmarshalling the data", ex);
		}
	}

	/**
	 * Creates an {@link Unmarshaller} from the {@link SardineUtil#CONTEXT}.
	 * Note: the unmarshaller is not thread safe, so it must be created for every request.
	 *
	 * @return a new Unmarshaller.
	 * @throws JAXBException
	 */
	public static Unmarshaller createUnmarshaller()
	{
		try
		{
			return CONTEXT.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			throw new RuntimeException("Could not create unmarshaller", e);
		}
	}

	/** */
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

}
