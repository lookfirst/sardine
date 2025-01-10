/*
 * Copyright 2009-2011 Jon Stevens et al.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.sardine.impl;

import com.github.sardine.*;
import com.github.sardine.impl.handler.ExistsResponseHandler;
import com.github.sardine.impl.handler.LockResponseHandler;
import com.github.sardine.impl.handler.MultiStatusResponseHandler;
import com.github.sardine.impl.handler.VoidResponseHandler;
import com.github.sardine.impl.io.ContentLengthInputStream;
import com.github.sardine.impl.io.HttpMethodReleaseInputStream;
import com.github.sardine.impl.methods.*;
import com.github.sardine.model.*;
import com.github.sardine.report.SardineReport;
import com.github.sardine.report.VersionTreeReport;
import com.github.sardine.util.SardineUtil;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.SchemePortResolver;
import org.apache.hc.client5.http.auth.*;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.IgnoreCookieSpecFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.VersionInfo;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static com.github.sardine.util.SardineUtil.createQNameWithDefaultNamespace;

/**
 * Implementation of the Sardine interface. This is where the meat of the Sardine library lives.
 *
 * @author jonstevens
 */
public class SardineImpl implements Sardine
{
	private static final Logger log = Logger.getLogger(DavResource.class.getName());

	/**
	 * Value for "Expect" HTTP header
	 */
	private static final String EXPECT_CONTINUE = "100-Continue";

	/**
	 * Default content charset originally used by v4 of HTTP client.
	 */
	private static final String DEF_CONTENT_CHARSET = StandardCharsets.ISO_8859_1.name();
	private static final String HTTP_MAJOR_VERSION = "HTTP_MAJOR_VERSION";

	/**
	 * HTTP client implementation
	 */
	protected CloseableHttpClient client;

	/**
	 * HTTP client configuration
	 */
	private HttpClientBuilder builder;

	/**
	 * Local context with authentication cache. Make sure the same context is used to execute
	 * logically related requests.
	 */
	protected HttpClientContext context = HttpClientContext.create();

	/**
	 * Access resources with no authentication
	 */
	public SardineImpl()
	{
		this.builder = this.configure(null, null);
		this.client = this.builder.build();
	}

	/**
	 * Access resources with Bearer authorization
	 */
	public SardineImpl(String bearerAuth)
	{
		Header bearerHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerAuth);
		this.builder = this.configure(null, null).setDefaultHeaders(Collections.singletonList(bearerHeader));
		this.client = this.builder.build();
	}

	/**
	 * Supports standard authentication mechanisms
	 *
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	public SardineImpl(String username, char[] password)
	{
		this.builder = this.configure(null, this.createDefaultCredentialsProvider(username, password, null, null));
		this.client = this.builder.build();
	}

	/**
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 * @param selector Proxy configuration
	 */
	public SardineImpl(String username, char[] password, ProxySelector selector)
	{
		this.builder = this.configure(selector, this.createDefaultCredentialsProvider(username, password, null, null));
		this.client = this.builder.build();
	}

	/**
	 * @param builder Custom client configuration
	 */
	public SardineImpl(HttpClientBuilder builder)
	{
		this.builder = builder;
		this.client = this.builder.build();
	}

	/**
	 * @param builder  Custom client configuration
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	public SardineImpl(HttpClientBuilder builder, String username, char[] password)
	{
		this.builder = builder;
		this.setCredentials(username, password);
		this.client = this.builder.build();
	}

	/**
	 * Add credentials to any scope. Supports Basic, Digest and NTLM authentication methods.
	 *
	 * @param username Use in authentication header credentials
	 * @param password Use in authentication header credentials
	 */
	@Override
	public void setCredentials(String username, char[] password)
	{
		this.setCredentials(username, password, "", "");
	}

	/**
	 * @param username    Use in authentication header credentials
	 * @param password    Use in authentication header credentials
	 * @param domain      NTLM authentication
	 * @param workstation NTLM authentication
	 */
	@Override
	public void setCredentials(String username, char[] password, String domain, String workstation)
	{
		this.setCredentials(this.createDefaultCredentialsProvider(username, password, domain, workstation));
	}

	public void setCredentials(CredentialsProvider provider)
	{
		this.context.setCredentialsProvider(provider);
	}

	private CredentialsProvider createDefaultCredentialsProvider(String username, char[] password, String domain, String workstation)
	{
		CredentialsStore provider = new BasicCredentialsProvider();
		if (username != null)
		{
			provider.setCredentials(
					new AuthScope(null, null, -1, null, StandardAuthScheme.NTLM),
					new NTCredentials(username, password, workstation, domain));
			provider.setCredentials(
					new AuthScope(null, null, -1, null, StandardAuthScheme.BASIC),
					new UsernamePasswordCredentials(username, password));
			provider.setCredentials(
					new AuthScope(null, null, -1, null, StandardAuthScheme.DIGEST),
					new UsernamePasswordCredentials(username, password));
			provider.setCredentials(
					new AuthScope(null, null, -1, null, StandardAuthScheme.SPNEGO),
					new NTCredentials(username, password, workstation, domain));
			provider.setCredentials(
					new AuthScope(null, null, -1, null, StandardAuthScheme.KERBEROS),
					new UsernamePasswordCredentials(username, password));
		}
		return provider;
	}

	/**
	 * Adds handling of GZIP compression to the client.
	 */
	@Override
	public void enableCompression()
	{
		// Adding content encoding (gzip) support
		this.builder.addRequestInterceptorLast((httpRequest, entityDetails, context) -> {
			httpRequest.addHeader("Accept-Encoding", "gzip");
		});

		this.builder.addResponseInterceptorLast((httpResponse, entityDetails, context) -> {
			ClassicHttpResponse classicHttpResponse = (ClassicHttpResponse) httpResponse;
			HttpEntity entity = classicHttpResponse.getEntity();

			if (httpResponse.containsHeader("Content-Encoding") &&
					"gzip".equalsIgnoreCase(httpResponse.getHeader("Content-Encoding").getValue())) {
				// Decompressing the gzip content
				ClassicHttpResponse.class.cast(httpResponse).setEntity(
						new GzipDecompressingEntity(entity));
			}
		});

		this.client = this.builder.build();
	}

	/**
	 * Disable GZIP compression header.
	 */
	@Override
	public void disableCompression()
	{
		this.builder.disableContentCompression();
		this.client = this.builder.build();
	}

	/**
	 * Ignores cookies by always returning the IgnoreSpecFactory regardless of the cookieSpec value being looked up.
	 */
	@Override
	public void ignoreCookies()
	{
		this.builder.setDefaultCookieSpecRegistry(new Lookup<CookieSpecFactory>()
		{
			@Override
			public CookieSpecFactory lookup(String name)
			{
				return new IgnoreCookieSpecFactory();
			}
		});
		this.client = this.builder.build();
	}

	@Override
	public void enablePreemptiveAuthentication(String hostname)
	{
		enablePreemptiveAuthentication(hostname, -1, -1);
	}

	@Override
	public void enablePreemptiveAuthentication(URL url)
	{
		final String host = url.getHost();
		final int port = url.getPort();
		final String protocol = url.getProtocol();
		final int httpPort;
		final int httpsPort;
		if ("https".equals(protocol))
		{
			httpsPort = port;
			httpPort = -1;
		}
		else if ("http".equals(protocol))
		{
			httpPort = port;
			httpsPort = -1;
		}
		else
		{
			throw new IllegalArgumentException("Unsupported protocol " + protocol);
		}
		enablePreemptiveAuthentication(host, httpPort, httpsPort);
	}

	@Override
	public void enablePreemptiveAuthentication(String hostname, int httpPort, int httpsPort)
	{
		enablePreemptiveAuthentication(hostname, httpPort, httpsPort, StandardCharsets.ISO_8859_1);
	}

	public void enablePreemptiveAuthentication(String hostname, int httpPort, int httpsPort, Charset credentialsCharset)
	{
		AuthCache cache = this.context.getAuthCache();
		if (cache == null)
		{
			// Add AuthCache to the execution context
			cache = new BasicAuthCache();
			this.context.setAuthCache(cache);

		}
		// Generate Basic preemptive scheme object and stick it to the local execution context
		BasicScheme basicAuth = new BasicScheme(credentialsCharset);
		// Configure HttpClient to authenticate preemptively by prepopulating the authentication data cache.
		cache.put(new HttpHost("http", hostname, httpPort), basicAuth);
		cache.put(new HttpHost("https", hostname, httpsPort), basicAuth);
	}

	@Override
	public void disablePreemptiveAuthentication()
	{
		this.context.removeAttribute(HttpClientContext.AUTH_CACHE);
	}

	@Override
	public List<DavResource> getResources(String url) throws IOException
	{
		return this.list(url);
	}

	@Override
	public List<DavResource> list(String url) throws IOException
	{
		return this.list(url, 1);
	}

	@Override
	public List<DavResource> list(String url, int depth) throws IOException
	{
		return list(url, depth, true);
	}

	@Override
	public List<DavResource> list(String url, int depth, boolean allProp) throws IOException
	{
		if (allProp)
		{
			Propfind body = new Propfind();
			body.setAllprop(new Allprop());
			return propfind(url, depth, body);
		}
		else
		{
			return list(url, depth, Collections.<QName>emptySet());
		}
	}

	@Override
	public List<DavResource> versionsList(String url) throws IOException {
		return versionsList(url, 0);
	}

	@Override
	public List<DavResource> versionsList(String url, int depth) throws IOException {
		return versionsList(url, depth, Collections.<QName>emptySet());
	}

	@Override
	public List<DavResource> versionsList(String url, int depth, java.util.Set<QName> props) throws IOException {
		return report(url, depth, new VersionTreeReport(props));
	}

	@Override
	public List<DavResource> list(String url, int depth, java.util.Set<QName> props) throws IOException
	{
		Propfind body = new Propfind();
		Prop prop = new Prop();
		ObjectFactory objectFactory = new ObjectFactory();
		prop.setGetcontentlength(objectFactory.createGetcontentlength());
		prop.setGetlastmodified(objectFactory.createGetlastmodified());
		prop.setCreationdate(objectFactory.createCreationdate());
		prop.setDisplayname(objectFactory.createDisplayname());
		prop.setGetcontenttype(objectFactory.createGetcontenttype());
		prop.setResourcetype(objectFactory.createResourcetype());
		prop.setGetetag(objectFactory.createGetetag());
		addCustomProperties(prop, props);
		body.setProp(prop);
		return propfind(url, depth, body);
	}

	@Override
	public List<DavResource> propfind(String url, int depth, java.util.Set<QName> props) throws IOException
	{
		Propfind body = new Propfind();
		Prop prop = new Prop();
		addCustomProperties(prop, props);
		body.setProp(prop);
		return propfind(url, depth, body);
	}

	private void addCustomProperties(Prop prop, java.util.Set<QName> props)
	{
		List<Element> any = prop.getAny();
		for (QName entry : props)
		{
			Element element = SardineUtil.createElement(entry);
			any.add(element);
		}
	}

	protected List<DavResource> propfind(String url, int depth, Propfind body) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth(depth < 0 ? "infinity" : Integer.toString(depth));
		entity.setEntity(new StringEntity(SardineUtil.toXml(body), StandardCharsets.UTF_8));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
		List<DavResource> resources = new ArrayList<DavResource>(responses.size());
		for (Response response : responses)
		{
			try
			{
				resources.add(new DavResource(response));
			}
			catch (URISyntaxException e)
			{
				log.warning(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
			}
		}
		return resources;
	}

	public <T> T report(String url, int depth, SardineReport<T> report) throws IOException
	{
		HttpReport entity = new HttpReport(url);
		entity.setDepth(depth < 0 ? "infinity" : Integer.toString(depth));
		entity.setEntity(new StringEntity(report.toXml(), StandardCharsets.UTF_8));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		return report.fromMultistatus(multistatus);
	}

	public List<DavResource> search(String url, String language, String query) throws IOException
	{
		HttpSearch search = new HttpSearch(url);
		SearchRequest searchBody = new SearchRequest(language, query);
		String body = SardineUtil.toXml(searchBody);
		search.setEntity(createEntity(body));
		Multistatus multistatus = this.execute(search, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
		List<DavResource> resources = new ArrayList<DavResource>(responses.size());
		for (Response response : responses)
		{
			try
			{
				resources.add(new DavResource(response));
			}
			catch (URISyntaxException e)
			{
				log.warning(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
			}
		}
		return resources;
	}

	@Override
	public void setCustomProps(String url, Map<String, String> set, List<String> remove) throws IOException
	{
		this.patch(url, SardineUtil.toQName(set), SardineUtil.toQName(remove));
	}

	@Override
	public List<DavResource> patch(String url, Map<QName, String> setProps) throws IOException
	{
		return this.patch(url, setProps, Collections.<QName>emptyList());
	}

	/**
	 * Creates a {@link com.github.sardine.model.Propertyupdate} element containing all properties to set from setProps and all properties to
	 * remove from removeProps. Note this method will use a {@link com.github.sardine.util.SardineUtil#CUSTOM_NAMESPACE_URI} as
	 * namespace and {@link com.github.sardine.util.SardineUtil#CUSTOM_NAMESPACE_PREFIX} as prefix.
	 */
	@Override
	public List<DavResource> patch(String url, Map<QName, String> setProps, List<QName> removeProps) throws IOException
	{
		List<Element> setPropsElements = new ArrayList<Element>();
		for (Entry<QName, String> entry : setProps.entrySet())
		{
			Element element = SardineUtil.createElement(entry.getKey());
			element.setTextContent(entry.getValue());
			setPropsElements.add(element);
		}
		return this.patch(url, setPropsElements, removeProps);
	}

	/**
	 * Creates a {@link com.github.sardine.model.Propertyupdate} element containing all properties to set from setProps and all properties to
	 * remove from removeProps. Note this method will use a {@link com.github.sardine.util.SardineUtil#CUSTOM_NAMESPACE_URI} as
	 * namespace and {@link com.github.sardine.util.SardineUtil#CUSTOM_NAMESPACE_PREFIX} as prefix.
	 */
	@Override
	public List<DavResource> patch(String url, List<Element> setProps, List<QName> removeProps) throws IOException
	{
		return this.patch(url, setProps, removeProps, Collections.emptyMap());
	}

	@Override
	public List<DavResource> patch(String url, List<Element> setProps, List<QName> removeProps, Map<String, String> headers) throws IOException
	{
		HttpPropPatch patch = new HttpPropPatch(url);
		for (Map.Entry<String, String> h : headers.entrySet())
		{
			patch.addHeader(new BasicHeader(h.getKey(), h.getValue()));
		}
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
			for (Element element : setProps)
			{
				any.add(element);
			}
			set.setProp(prop);
		}
		// Remove properties
		{
			if (!removeProps.isEmpty()) {
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
		}
		entity.setEntity(createEntity(body));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
		List<DavResource> resources = new ArrayList<DavResource>(responses.size());
		for (Response response : responses)
		{
			try
			{
				resources.add(new DavResource(response));
			}
			catch (URISyntaxException e)
			{
				log.warning(String.format("Ignore resource with invalid URI %s", response.getHref().get(0)));
			}
		}
		return resources;
	}

	@Override
	public String lock(String url) throws IOException
	{
		HttpLock entity = new HttpLock(url);
		Lockinfo body = new Lockinfo();
		Lockscope scopeType = new Lockscope();
		scopeType.setExclusive(new Exclusive());
		body.setLockscope(scopeType);
		Locktype lockType = new Locktype();
		lockType.setWrite(new Write());
		body.setLocktype(lockType);
		entity.setEntity(createEntity(body));
		// Return the lock token
		return this.execute(entity, new LockResponseHandler());
	}

	@Override
	public String refreshLock(String url, String token, String file) throws IOException
	{
		HttpLock entity = new HttpLock(url);
		entity.setHeader("If", "<" + file + "> (<" + token + ">)");
		return this.execute(entity, new LockResponseHandler());
	}

	@Override
	public void unlock(String url, String token) throws IOException
	{
		HttpUnlock entity = new HttpUnlock(url, token);
		this.execute(entity, new VoidResponseHandler());
	}

	@Override
	public void addToVersionControl(String url) throws IOException {
		this.execute(new HttpVersionControl(url), new VoidResponseHandler());
	}

	@Override
	public void checkout(String url) throws IOException {
		this.execute(new HttpCheckout(url), new VoidResponseHandler());
	}

	@Override
	public void checkin(String url) throws IOException {
		this.execute(new HttpCheckin(url), new VoidResponseHandler());
	}

	@Override
	public void setAcl(String url, List<DavAce> aces) throws IOException
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
		entity.setEntity(createEntity(body));
		this.execute(entity, new VoidResponseHandler());
	}


	@Override
	public DavAcl getAcl(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("0");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setOwner(new Owner());
		prop.setGroup(new Group());
		prop.setAcl(new Acl());
		body.setProp(prop);
		entity.setEntity(createEntity(body));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
		if (responses.isEmpty())
		{
			return null;
		}
		else
		{
			return new DavAcl(responses.get(0));
		}
	}

	@Override
	public DavQuota getQuota(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("0");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setQuotaAvailableBytes(new QuotaAvailableBytes());
		prop.setQuotaUsedBytes(new QuotaUsedBytes());
		body.setProp(prop);
		entity.setEntity(createEntity(body));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
		if (responses.isEmpty())
		{
			return null;
		}
		DavResource resource;
		try
		{
			resource = new DavResource(responses.get(0));
		}
		catch (URISyntaxException e)
		{
			log.warning(String.format("Invalid URI %s", responses.get(0).getHref().get(0)));
			return null;
		}
		if (resource.getStatusCode() == 200) return new DavQuota(responses.get(0));
        return null;
	}

	@Override
	public List<DavPrincipal> getPrincipals(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("1");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setDisplayname(new Displayname());
		prop.setResourcetype(new Resourcetype());
		prop.setPrincipalURL(new PrincipalURL());
		body.setProp(prop);
		entity.setEntity(createEntity(body));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
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

	@Override
	public List<String> getPrincipalCollectionSet(String url) throws IOException
	{
		HttpPropFind entity = new HttpPropFind(url);
		entity.setDepth("0");
		Propfind body = new Propfind();
		Prop prop = new Prop();
		prop.setPrincipalCollectionSet(new PrincipalCollectionSet());
		body.setProp(prop);
		entity.setEntity(createEntity(body));
		Multistatus multistatus = this.execute(entity, new MultiStatusResponseHandler());
		List<Response> responses = multistatus.getResponse();
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

	@Override
	public void enableHttp2() {
		this.context.setAttribute(HTTP_MAJOR_VERSION, 2);
	}

	@Override
	public ContentLengthInputStream get(String url) throws IOException
	{
		return this.get(url, Collections.<String, String>emptyMap());
	}

	@Override
	public ContentLengthInputStream get(String url, String version) throws IOException {
		List<DavResource> versionHistory = propfind(url, 0, Collections.singleton(createQNameWithDefaultNamespace("version-history")));
		String storageUrl = versionHistory.get(0).getCustomProps().get("version-history");
		return this.get(storageUrl + version);
	}

	@Override
	public ContentLengthInputStream get(String url, Map<String, String> headers) throws IOException
	{
		List<Header> list = new ArrayList<Header>();
		for (Map.Entry<String, String> h : headers.entrySet())
		{
			list.add(new BasicHeader(h.getKey(), h.getValue()));
		}
		return this.get(url, list);
	}

	public ContentLengthInputStream get(String url, List<Header> headers) throws IOException
	{
		HttpGet get = new HttpGet(url);
		for (Header header : headers)
		{
			get.addHeader(header);
		}
		// Must use #execute without handler, otherwise the entity is consumed
		// already after the handler exits.
		ClassicHttpResponse response = this.execute(get);
		VoidResponseHandler handler = new VoidResponseHandler();
		try
		{
			handler.handleResponse(response);
			// Will abort the read when closed before EOF.
			return new ContentLengthInputStream(new HttpMethodReleaseInputStream(response), response.getEntity().getContentLength());
		}
		catch (IOException ex)
		{
			get.abort();
			throw ex;
		}
	}

	@Override
	public void put(String url, byte[] data) throws IOException
	{
		this.put(url, data, null);
	}

	@Override
	public void put(String url, byte[] data, String contentType) throws IOException
	{
		ByteArrayEntity entity = new ByteArrayEntity(data, contentType != null ? ContentType.create(contentType) : null);
		this.put(url, entity, contentType, true);
	}

	@Override
	public void put(String url, InputStream dataStream) throws IOException
	{
		this.put(url, dataStream, null);
	}

	@Override
	public void put(String url, InputStream dataStream, String contentType) throws IOException
	{
		this.put(url, dataStream, contentType, true);
	}

	@Override
	public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException
	{
		// A length of -1 means "go until end of stream"
		put(url, dataStream, contentType, expectContinue, -1);
	}

	@Override
	public void put(String url, InputStream dataStream, String contentType, boolean expectContinue, long contentLength) throws IOException
	{
		InputStreamEntity entity = new InputStreamEntity(dataStream, contentLength, contentType != null ? ContentType.create(contentType) : null);
		this.put(url, entity, contentType, expectContinue);
	}

	@Override
	public void put(String url, InputStream dataStream, String contentType, Map<String, String> headers) throws IOException
	{
		List<Header> list = new ArrayList<Header>();
		for (Map.Entry<String, String> h : headers.entrySet())
		{
			list.add(new BasicHeader(h.getKey(), h.getValue()));
		}
		this.put(url, dataStream, contentType, list);
	}

	public void put(String url, InputStream dataStream, String contentType, List<Header> headers) throws IOException
	{
		// A length of -1 means "go until end of stream"
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1, contentType != null ? ContentType.create(contentType) : null);
		this.put(url, entity, headers);
	}

	/**
	 * Upload the entity using <code>PUT</code>
	 *
	 * @param url            Resource
	 * @param entity         The entity to read from
	 * @param contentType    Content Type header
	 * @param expectContinue Add <code>Expect: continue</code> header
	 */
	public void put(String url, HttpEntity entity, String contentType, boolean expectContinue) throws IOException
	{
		List<Header> headers = new ArrayList<Header>();
		if (contentType != null)
		{
			headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, contentType));
		}
		if (expectContinue)
		{
			headers.add(new BasicHeader(HttpHeaders.EXPECT, EXPECT_CONTINUE));
		}
		this.put(url, entity, headers);
	}

	/**
	 * Upload the entity using <code>PUT</code>
	 *
	 * @param url     Resource
	 * @param entity  The entity to read from
	 * @param headers Headers to add to request
	 */
	public void put(String url, HttpEntity entity, List<Header> headers) throws IOException
	{
		this.put(url, entity, headers, new VoidResponseHandler());
	}

	public <T> T put(String url, HttpEntity entity, List<Header> headers, HttpClientResponseHandler<T> handler) throws IOException
	{
		HttpPut put = new HttpPut(url);
		put.setEntity(entity);
		for (Header header : headers)
		{
			put.addHeader(header);
		}
		if (entity.getContentType() == null && !put.containsHeader(HttpHeaders.CONTENT_TYPE))
		{
			put.addHeader(HttpHeaders.CONTENT_TYPE, DEF_CONTENT_CHARSET);
		}
		try
		{
			return this.execute(put, handler);
		}
		catch (HttpResponseException e)
		{
			if (e.getStatusCode() == HttpStatus.SC_EXPECTATION_FAILED)
			{
				// Retry with the Expect header removed
				put.removeHeaders(HttpHeaders.EXPECT);
				if (entity.isRepeatable())
				{
					return this.execute(put, handler);
				}
			}
			throw e;
		}
	}

	@Override
	public void put(String url, File localFile, String contentType) throws IOException
	{
		//don't use ExpectContinue for repeatable FileEntity, some web server (IIS for example) may return 400 bad request after retry
		put(url, localFile, contentType, false);
	}

	@Override
	public void put(String url, File localFile, String contentType, boolean expectContinue) throws IOException
	{
		FileEntity content = new FileEntity(localFile, contentType != null ? ContentType.create(contentType) : null);
		this.put(url, content, contentType, expectContinue);
	}

	@Override
	public void delete(String url) throws IOException
	{
		this.delete(url, Collections.emptyMap());
	}

	@Override
	public void delete(String url, Map<String, String> headers) throws IOException
	{
		HttpDelete delete = new HttpDelete(url);
		this.execute(delete, new VoidResponseHandler());
	}

	@Override
	public void move(String sourceUrl, String destinationUrl) throws IOException
	{
		this.move(sourceUrl, destinationUrl, true);
	}

	@Override
	public void move(String sourceUrl, String destinationUrl, boolean overwrite) throws IOException
	{
		this.move(sourceUrl, destinationUrl, overwrite, Collections.emptyMap());
	}

	@Override
	public void move(String sourceUrl, String destinationUrl, boolean overwrite, Map<String, String> headers) throws IOException
	{
		HttpMove move = new HttpMove(sourceUrl, destinationUrl, overwrite);
		for (Map.Entry<String, String> h : headers.entrySet())
		{
			move.addHeader(new BasicHeader(h.getKey(), h.getValue()));
		}
		this.execute(move, new VoidResponseHandler());
	}

	@Override
	public void copy(String sourceUrl, String destinationUrl) throws IOException
	{
		copy(sourceUrl, destinationUrl, true);
	}

	@Override
	public void copy(String sourceUrl, String destinationUrl, boolean overwrite) throws IOException
	{
		this.copy(sourceUrl, destinationUrl, overwrite, Collections.emptyMap());
	}

	@Override
	public void copy(String sourceUrl, String destinationUrl, boolean overwrite, Map<String, String> headers) throws IOException
	{
		HttpCopy copy = new HttpCopy(sourceUrl, destinationUrl, overwrite);
		for (Map.Entry<String, String> h : headers.entrySet())
		{
			copy.addHeader(new BasicHeader(h.getKey(), h.getValue()));
		}
		this.execute(copy, new VoidResponseHandler());
	}

	@Override
	public void createDirectory(String url) throws IOException
	{
		HttpMkCol mkcol = new HttpMkCol(url);
		this.execute(mkcol, new VoidResponseHandler());
	}

	@Override
	public boolean exists(String url) throws IOException
	{
		HttpHead head = new HttpHead(url);
		return this.execute(head, new ExistsResponseHandler());
	}

	/**
	 * Validate the response using the response handler. Aborts the request if there is an exception.
	 *
	 * @param <T>             Return type
	 * @param request         Request to execute
	 * @param responseHandler Determines the return type.
	 * @return parsed response
	 */
	protected <T> T execute(HttpUriRequestBase request, HttpClientResponseHandler<T> responseHandler)
			throws IOException
	{
		return execute(context, request, responseHandler);
	}

	/**
	 * No validation of the response. Aborts the request if there is an exception.
	 *
	 * @param request Request to execute
	 * @return The response to check the reply status code
	 */
	protected ClassicHttpResponse execute(HttpUriRequestBase request)
			throws IOException
	{
		return execute(context, request, null);
	}

	/**
	 * Common method as single entry point responsible fo request execution
	 * @param context clientContext to be used when executing request
	 * @param request Request to execute
	 * @param responseHandler can be null if you need raw HttpResponse or not null response handler for result handling.
	 * @param <T> will return raw HttpResponse when responseHandler is null or value reslved using provided ResponseHandler instance
	 * @return value resolved using response handler or raw HttpResponse when responseHandler is null
	 */
	protected <T> T execute(HttpClientContext context, HttpUriRequestBase request, HttpClientResponseHandler<T> responseHandler)
			throws IOException
	{
		Integer httpMajorVersion = (Integer) context.getAttribute(HTTP_MAJOR_VERSION);

		if (httpMajorVersion != null){
			request.setVersion(new ProtocolVersion("HTTP", httpMajorVersion, 0));
		}
		HttpContext requestLocalContext = new HttpClientContext(context);
		try
		{
			if (responseHandler != null)
			{
				return this.client.execute(request, requestLocalContext, responseHandler);
			}
			else
			{
				return (T) this.client.execute(request, requestLocalContext);
			}
		}
		catch (HttpResponseException e)
		{
			// Don't abort if we get this exception, caller may want to repeat request.
			throw e;
		}
		catch (IOException e)
		{
			request.abort();
			throw e;
		}
		finally
		{
			context.setAttribute(HttpClientContext.USER_TOKEN, requestLocalContext.getAttribute(HttpClientContext.USER_TOKEN));
		}
	}

	@Override
	public void shutdown() throws IOException
	{
		this.client.close();
	}

	/**
	 * Creates a client with all of the defaults.
	 *
	 * @param selector    Proxy configuration or null
	 * @param credentials Authentication credentials or null
	 */
	protected HttpClientBuilder configure(ProxySelector selector, CredentialsProvider credentials)
	{
		Registry<ConnectionSocketFactory> schemeRegistry = this.createDefaultSchemeRegistry();
		HttpClientConnectionManager cm = this.createDefaultConnectionManager(schemeRegistry);
		String version = Version.getSpecification();
		if (version == null)
		{
			version = VersionInfo.UNAVAILABLE;
		}
		return HttpClients.custom()
				.setUserAgent("Sardine/" + version)
				.setDefaultCredentialsProvider(credentials)
				.setRedirectStrategy(this.createDefaultRedirectStrategy())
				.setDefaultRequestConfig(RequestConfig.custom()
						// Only selectively enable this for PUT but not all entity enclosing methods
						.setExpectContinueEnabled(false).build())
				.setConnectionManager(cm)
				.setRoutePlanner(this.createDefaultRoutePlanner(this.createDefaultSchemePortResolver(), selector));
	}

	protected DefaultSchemePortResolver createDefaultSchemePortResolver()
	{
		return new DefaultSchemePortResolver();
	}

	protected RedirectStrategy createDefaultRedirectStrategy()
	{
		return new DefaultRedirectStrategy();
	}

	/**
	 * Creates a new registry for default ports with socket factories.
	 */
	protected Registry<ConnectionSocketFactory> createDefaultSchemeRegistry()
	{
		return RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", this.createDefaultSocketFactory())
				.register("https", this.createDefaultSecureSocketFactory())
				.build();
	}

	/**
	 * @return Default socket factory
	 */
	protected ConnectionSocketFactory createDefaultSocketFactory()
	{
		return PlainConnectionSocketFactory.getSocketFactory();
	}

	/**
	 * @return Default SSL socket factory
	 */
	protected ConnectionSocketFactory createDefaultSecureSocketFactory()
	{
		return SSLConnectionSocketFactory.getSocketFactory();
	}

	/**
	 * Use fail fast connection manager when connections are not released properly.
	 *
	 * @param schemeRegistry Protocol registry
	 * @return Default connection manager
	 */
	protected HttpClientConnectionManager createDefaultConnectionManager(Registry<ConnectionSocketFactory> schemeRegistry)
	{
		return new PoolingHttpClientConnectionManager(schemeRegistry);
	}

	/**
	 * Override to provide proxy configuration
	 *
	 * @param resolver Protocol registry
	 * @param selector Proxy configuration
	 * @return ProxySelectorRoutePlanner configured with schemeRegistry and selector
	 */
	protected HttpRoutePlanner createDefaultRoutePlanner(SchemePortResolver resolver, ProxySelector selector)
	{
		return new SystemDefaultRoutePlanner(resolver, selector);
	}

	/**
	 * Common logic to create an HTTP entity for a JAXB element.
	 *
	 * @param jaxbElement the JAXB element.
	 * @return the HTTP entity.
	 * @throws IOException if an error occurs serialising the element.
	 */
	private static HttpEntity createEntity(Object jaxbElement) throws IOException
	{
		return new StringEntity(SardineUtil.toXml(jaxbElement), StandardCharsets.UTF_8);
	}
}
