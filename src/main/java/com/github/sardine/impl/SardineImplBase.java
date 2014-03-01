package com.github.sardine.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.sardine.DavAce;
import com.github.sardine.DavAcl;
import com.github.sardine.DavPrincipal;
import com.github.sardine.DavQuota;
import com.github.sardine.DavResource;
import com.github.sardine.impl.methods.HttpAcl;
import com.github.sardine.impl.methods.HttpLock;
import com.github.sardine.impl.methods.HttpPropFind;
import com.github.sardine.impl.methods.HttpPropPatch;
import com.github.sardine.impl.methods.HttpUnlock;
import com.github.sardine.model.Ace;
import com.github.sardine.model.Acl;
import com.github.sardine.model.Allprop;
import com.github.sardine.model.Displayname;
import com.github.sardine.model.Exclusive;
import com.github.sardine.model.Group;
import com.github.sardine.model.Lockinfo;
import com.github.sardine.model.Lockscope;
import com.github.sardine.model.Locktype;
import com.github.sardine.model.ObjectFactory;
import com.github.sardine.model.Owner;
import com.github.sardine.model.PrincipalCollectionSet;
import com.github.sardine.model.PrincipalURL;
import com.github.sardine.model.Prop;
import com.github.sardine.model.Propertyupdate;
import com.github.sardine.model.Propfind;
import com.github.sardine.model.Propstat;
import com.github.sardine.model.QuotaAvailableBytes;
import com.github.sardine.model.QuotaUsedBytes;
import com.github.sardine.model.Remove;
import com.github.sardine.model.Resourcetype;
import com.github.sardine.model.Response;
import com.github.sardine.model.Set;
import com.github.sardine.model.Write;
import com.github.sardine.util.SardineUtil;

public class SardineImplBase {
	
	protected static Logger log = LoggerFactory.getLogger(DavResource.class);
	protected static final String UTF_8 = "UTF-8";
	

    protected ProtocolVersion version = HttpVersion.HTTP_1_1;

	/**
	 * Local context with authentication cache. Make sure the same context is used to execute
	 * logically related requests.
	 */
	protected HttpContext context = new BasicHttpContext();
	
	
	/*** 
	 * Set http protocol version
	 * @param version
	 */
	protected void setProtocolVersion(ProtocolVersion version) {
		this.version = version;
	}
	
	/**
	 * @param username	Use in authentication header credentials
	 * @param password	Use in authentication header credentials
	 * @param domain	  NTLM authentication
	 * @param workstation NTLM authentication
	 */
	protected BasicCredentialsProvider createCredentialsProvider(String username, String password, String domain, String workstation)	{
		BasicCredentialsProvider basicCredentialsProvider=null;
		if (username != null && password != null)
		{
			basicCredentialsProvider = new BasicCredentialsProvider();
			 basicCredentialsProvider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.NTLM),
					new NTCredentials(username, password, workstation, domain));
			 basicCredentialsProvider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.BASIC),
					new UsernamePasswordCredentials(username, password));
			 basicCredentialsProvider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.DIGEST),
					new UsernamePasswordCredentials(username, password));
			 basicCredentialsProvider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.SPNEGO),
					new UsernamePasswordCredentials(username, password));
			 basicCredentialsProvider.setCredentials(
					new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthSchemes.KERBEROS),
					new UsernamePasswordCredentials(username, password));
			
		}
		return basicCredentialsProvider;
	
	}

	/***
	 * 
	 * @param hostname
	 */
	protected void enablePreemptiveAuthentication(String hostname)	{
		AuthCache authCache = new BasicAuthCache();
		// Generate Basic preemptive scheme object and stick it to the local execution context
		BasicScheme basicAuth = new BasicScheme();

		populateAuthCache(hostname, "http", 80, authCache, basicAuth);
		populateAuthCache(hostname, "https", 443, authCache, basicAuth);		

		// Add AuthCache to the execution context
		this.context.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
	}
	

	
	/***
	 * 
	 * @param hostname  name of host
	 * @param scheme  http, https etc.
	 * @param port    port
	 * @param authCache  authorization cache
	 * @param basicAuth  basic authorization scheme
	 */
	private void populateAuthCache(String hostname, String scheme, int port, AuthCache authCache, BasicScheme basicAuth)	{
		authCache.put(new HttpHost(hostname), basicAuth);
		authCache.put(new HttpHost(hostname, -1, scheme), basicAuth);
		authCache.put(new HttpHost(hostname, port, scheme), basicAuth);
	}
	
	/***
	 * 
	 */
	protected void disablePreemptiveAuthentication()	{
		context.removeAttribute(HttpClientContext.AUTH_CACHE);
	}

	/***
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected HttpLock generateLockEntity(String url) throws IOException
	{
		HttpLock entity = new HttpLock(url);
		Lockinfo body = new Lockinfo();
		Lockscope scopeType = new Lockscope();
		scopeType.setExclusive(new Exclusive());
		body.setLockscope(scopeType);
		Locktype lockType = new Locktype();
		lockType.setWrite(new Write());
		body.setLocktype(lockType);
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
	    return entity;	
	}
	
	/***
	 * 
	 * @param url
	 * @param token
	 * @return
	 * @throws IOException
	 */
	protected HttpUnlock generateUnlockEntity(String url, String token) throws IOException
	{
		HttpUnlock entity = new HttpUnlock(url, token);
		Lockinfo body = new Lockinfo();
		Lockscope scopeType = new Lockscope();
		scopeType.setExclusive(new Exclusive());
		body.setLockscope(scopeType);
		Locktype lockType = new Locktype();
		lockType.setWrite(new Write());
		body.setLocktype(lockType);
		return entity;
	}
	
	/***
	 * 
	 * @param url
	 * @param token
	 * @param file
	 * @return
	 * @throws IOException
	 */
	protected HttpLock generateRefreshLockEntity(String url, String token, String file) throws IOException
	{
		HttpLock entity = new HttpLock(url);
		entity.setHeader("If", "<" + file + "> (<" + token + ">)");
	    return entity;	
	}

	/***
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected HttpPropFind generateGetQuotaEntity(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("0");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setQuotaAvailableBytes(new QuotaAvailableBytes());
		prop.setQuotaUsedBytes(new QuotaUsedBytes());
		body.setProp(prop);
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
	    return entity;	
	}
	
	/***
	 * 
	 * @param url
	 * @param aces
	 * @return
	 * @throws IOException
	 */
	protected HttpAcl generateSetAclEntity(String url, List<DavAce> aces) throws IOException
	{
		HttpAcl entity = new HttpAcl(url);
		// Build WebDAV <code>ACL</code> entity.
		Acl body = new Acl();
		body.setAce(new ArrayList<Ace>());
		for (DavAce davAce : aces)
		{
			// protected and inherited acl must not be part of ACL http request
			if (davAce.getInherited() != null || davAce.isProtected())
			{
				continue;
			}
			Ace ace = davAce.toModel();
			body.getAce().add(ace);
		}
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
		return entity;
	}
	
	/***
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected HttpPropFind generateGetAclEntity(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("0");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setOwner(new Owner());
		prop.setGroup(new Group());
		prop.setAcl(new Acl());
		body.setProp(prop);
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
	    return entity;	
	}
	
	/***
	 * 
	 * @param url
	 * @param entity
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	protected HttpPut generatePutEntity(String url, HttpEntity entity, Map<String, String> headers) throws IOException
	{
		HttpPut put = new HttpPut(url);
		put.setEntity(entity);
		for (String header : headers.keySet())
		{
			put.addHeader(header, headers.get(header));
		}
		if (entity.getContentType() == null && !put.containsHeader(HttpHeaders.CONTENT_TYPE))
		{
			put.addHeader(HttpHeaders.CONTENT_TYPE, HTTP.DEF_CONTENT_CHARSET.name());
		}
		return put;
		
	}
	
	/***
	 * 
	 * @param url
	 * @param entity
	 * @param contentType
	 * @param expectContinue
	 * @return
	 * @throws IOException
	 */
	protected Map<String, String> generatePutHeaders(String url, HttpEntity entity, String contentType, boolean expectContinue) throws IOException
	{
		Map<String, String> headers = new HashMap<String, String>();
		if (contentType != null)
		{
			headers.put(HttpHeaders.CONTENT_TYPE, contentType);
		}
		if (expectContinue)
		{
			headers.put(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
		}
	    return headers;	
	}
	
	
	protected HttpPropFind generatePrincipalCollectionSetEntity(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("0");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setPrincipalCollectionSet(new PrincipalCollectionSet());
		body.setProp(prop);
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
	    return entity;	
	}
	
	protected HttpPropFind generateGetPrincipalsEntity(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("1");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setDisplayname(new Displayname());
		prop.setResourcetype(new Resourcetype());
		prop.setPrincipalURL(new PrincipalURL());
		body.setProp(prop);
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
	    return entity;	
	}
	protected HttpPropFind generateListEntity(String url, int depth, boolean allProp) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth(Integer.toString(depth));
		Propfind body = new Propfind();
		if (allProp)
			body.setAllprop(new Allprop());
		else {
			Prop prop = new Prop();
			ObjectFactory objectFactory = new ObjectFactory();
			prop.setGetcontentlength(objectFactory.createGetcontentlength());
			prop.setGetlastmodified(objectFactory.createGetlastmodified());
			prop.setCreationdate(objectFactory.createCreationdate());
			prop.setDisplayname(objectFactory.createDisplayname());
			prop.setGetcontenttype(objectFactory.createGetcontenttype());
			prop.setResourcetype(objectFactory.createResourcetype());
			prop.setGetetag(objectFactory.createGetetag());

			body.setProp(prop);
		}
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
		return entity;
	}
	
	protected HttpPropPatch generatePatchEntity(String url, Map<QName, String> setProps, List<QName> removeProps) throws IOException
	{
		HttpPropPatch entity = new HttpPropPatch(url);
		// Build WebDAV <code>PROPPATCH</code> entity.
		Propertyupdate body = new Propertyupdate();
		// Add properties
		{
			Set set = new Set();
			body.getRemoveOrSet().add(set);
			Prop prop = new Prop();
			// Returns a reference to the live list
			List<Element> any = prop.getAny();
			for (Map.Entry<QName, String> entry : setProps.entrySet())
			{
				Element element = SardineUtil.createElement(entry.getKey());
				element.setTextContent(entry.getValue());
				any.add(element);
			}
			set.setProp(prop);
		}
		// Remove properties
		{
			Remove remove = new Remove();
			body.getRemoveOrSet().add(remove);
			Prop prop = new Prop();
			// Returns a reference to the live list
			List<Element> any = prop.getAny();
			for (QName entry : removeProps)
			{
				Element element = SardineUtil.createElement(entry);
				any.add(element);
			}
			remove.setProp(prop);
		}
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), UTF_8));
	    return entity;	
	}
	
	protected DavAcl processGetAclReponses(List<Response> responses)
	{
		if (responses.isEmpty())
		{
			return null;
		}
		else
		{
			return new DavAcl(responses.get(0));
		}
	}

	protected DavQuota processGetQuotaResponses(List<Response> responses)
	{
		if (responses.isEmpty())
		{
			return null;
		}
		else
		{
			return new DavQuota(responses.get(0));
		}
	}

	protected List<DavPrincipal> processGetPrincipalsResponses(List<Response> responses)
	{
		if (responses.isEmpty())
		{
			return null;
		}
		else
		{
			List<DavPrincipal> collections = new ArrayList<DavPrincipal>();
			for (Response r : responses)
			{
				if (r.getPropstat() != null)
				{
					for (Propstat propstat : r.getPropstat())
					{
						if (propstat.getProp() != null
								&& propstat.getProp().getResourcetype() != null
								&& propstat.getProp().getResourcetype().getPrincipal() != null)
						{
							collections.add(new DavPrincipal(DavPrincipal.PrincipalType.HREF,
									r.getHref().get(0),
									propstat.getProp().getDisplayname().getContent().get(0)));
						}
					}
				}
			}
			return collections;
		}
	}

	protected List<String> processGgetPrincipalCollectionSetResponses(List<Response> responses)
	{
		if (responses.isEmpty())
		{
			return null;
		}
		else
		{
			List<String> collections = new ArrayList<String>();
			for (Response r : responses)
			{
				if (r.getPropstat() != null)
				{
					for (Propstat propstat : r.getPropstat())
					{
						if (propstat.getProp() != null
								&& propstat.getProp().getPrincipalCollectionSet() != null
								&& propstat.getProp().getPrincipalCollectionSet().getHref() != null)
						{
							collections.addAll(propstat.getProp().getPrincipalCollectionSet().getHref());
						}
					}
				}
			}
			return collections;
		}
	}

	
	protected List<DavResource> processListResponses(List<Response> responses) {
		List<DavResource> resources = new ArrayList<DavResource>(responses.size());
		for (Response response : responses)
		{
			try
			{
				resources.add(new DavResource(response));
			}
			catch (URISyntaxException e)
			{
				log.warn(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
			}
		}
		return resources;
	}


}
