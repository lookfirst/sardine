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

package com.github.sardine;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.namespace.QName;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.sardine.DavPrincipal.PrincipalType;
import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.SardineImpl;
import com.github.sardine.util.SardineUtil;

import jakarta.xml.bind.JAXBException;

@Category(IntegrationTest.class)
public class FunctionalSardineTest
{
    @ClassRule
    public static WebDavTestContainer webDavTestContainer = WebDavTestContainer.getInstance();

    @Test
    public void testRead() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        String url = String.format("%s%s", webDavTestContainer.getTestFolderUrl(), "test.txt");
        final InputStream in = sardine.get(url);
        assertNotNull(in);
        in.close();
    }

    @Test
    public void testGetFileContentEncoding() throws Exception
    {
        final AtomicBoolean intercept = new AtomicBoolean();
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.addInterceptorFirst((HttpResponseInterceptor) (r, context) -> {
            if (r.getStatusLine().getStatusCode() == 200) {
                intercept.set(true);
                assertNotNull(r.getHeaders(HttpHeaders.CONTENT_ENCODING));
                assertEquals(1, r.getHeaders(HttpHeaders.CONTENT_ENCODING).length);
                assertEquals("gzip", r.getHeaders(HttpHeaders.CONTENT_ENCODING)[0].getValue());
            }
        });
        Sardine sardine = new SardineImpl(builder);
        sardine.enableCompression();
        final String url = webDavTestContainer.getTestFolderUrl() + "test.txt";
        final InputStream in = sardine.get(url);
        assertNotNull(in);
        try {
            in.close();
        } catch (EOFException e) {
            fail("Issue https://issues.apache.org/jira/browse/HTTPCLIENT-1075 pending");
        }
        assertTrue(intercept.get());
    }

    @Test
    public void testReadEmptyFile() throws Exception
    {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        Sardine sardine = new SardineImpl(builder);
        final String url = webDavTestContainer.getRandomTestFileUrl();
        sardine.put(url, new ByteArrayInputStream(new byte[0]));
        final InputStream in = sardine.get(url);
        assertNotNull(in);
        assertEquals(-1, in.read());
        in.close();
    }

    @Test
    public void testReadCloseNotFullyConsumed() throws Exception
    {
        // 3 requests in total
        final CountDownLatch c = new CountDownLatch(3);
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setConnectionManager(new PoolingHttpClientConnectionManager(Long.MAX_VALUE, TimeUnit.MILLISECONDS) {
            @Override
            public synchronized void releaseConnection(HttpClientConnection conn, Object state, long keepalive, TimeUnit tunit) {
                switch ((int) c.getCount()) {
                    case 3:
                    case 1:
                        // DELETE
                        // PUT
                        assertTrue(conn.isOpen());
                        break;
                    case 2:
                        // GET
                        assertFalse(conn.isOpen());
                        break;
                    default:
                        fail();
                }
                super.releaseConnection(conn, state, keepalive, tunit);
                c.countDown();
            }
        });
        Sardine sardine = new SardineImpl(builder);
        // Make sure the response is not compressed
        sardine.disableCompression();
        final String url = webDavTestContainer.getRandomTestFileUrl();
        try {
            final byte[] content = "sa".getBytes(StandardCharsets.UTF_8);
            assertEquals(2, content.length);
            sardine.put(url, new ByteArrayInputStream(content));
            final InputStream in = sardine.get(url);
            assertNotNull(in);
            assertEquals('s', in.read());
            in.close();
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testReadCloseFullyConsumed() throws Exception
    {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setConnectionManager(new PoolingHttpClientConnectionManager(Long.MAX_VALUE, TimeUnit.MILLISECONDS) {
            @Override
            public synchronized void releaseConnection(HttpClientConnection conn, Object state, long keepalive, TimeUnit tunit) {
                assertTrue(conn.isOpen());
                super.releaseConnection(conn, state, keepalive, tunit);
            }
        });
        Sardine sardine = new SardineImpl(builder);
        // Make sure the response is not compressed
        sardine.disableCompression();
        final String url = webDavTestContainer.getRandomTestFileUrl();
        try {
            final byte[] content = "sa".getBytes(StandardCharsets.UTF_8);
            assertEquals(2, content.length);
            sardine.put(url, new ByteArrayInputStream(content));
            final InputStream in = sardine.get(url);
            assertNotNull(in);
            assertEquals('s', in.read());
            assertEquals('a', in.read());
            in.close();
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testGetFileNotFound() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        InputStream in = null;
        try {
            in = sardine.get("http://sardine.googlecode.com/svn/trunk/NOTFOUND");
            fail("Expected 404");
        } catch (SardineException e) {
            assertEquals(404, e.getStatusCode());
        }
        assertNull(in);
    }

    @Test
    public void testGetTimestamps() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getTestFolderUrl() + "test.txt";
        final List<DavResource> resources = sardine.list(url);
        assertEquals(1, resources.size());
        assertNotNull(resources.iterator().next().getModified());
        assertNotNull(resources.iterator().next().getCreation());
    }

    @Test
    public void testGetLength() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getTestFolderUrl();
        final List<DavResource> resources = sardine.list(url);
        assertTrue(resources.size() > 0);
        assertNotNull(resources.iterator().next().getContentLength());
    }

    @Test
    public void getDavQuota() throws IOException
    {
        final String url = webDavTestContainer.getRandomTestDirectoryUrl();
        Sardine sardine = SardineFactory.begin();
        sardine.createDirectory(url);
        DavQuota davQuota = sardine.getQuota(url);
        assertTrue(davQuota.getQuotaAvailableBytes() > 0);
        assertEquals(0, davQuota.getQuotaUsedBytes());
    }

    @Test(expected = SardineException.class)
    public void testSetAcl() throws IOException
    {
        final String url = webDavTestContainer.getRandomTestDirectoryUrl();
        Sardine sardine = SardineFactory.begin();
        try {
            sardine.createDirectory(url);
            List<DavAce> aces = new ArrayList<>();
            sardine.setAcl(url, aces);
            DavAcl acls = sardine.getAcl(url);
            for (DavAce davace : acls.getAces()) {
                if (davace.getInherited() == null)
                    fail("We have cleared all ACEs, should not have anymore non inherited ACEs");
            }
            aces.clear();
            DavAce ace = new DavAce(new DavPrincipal(PrincipalType.HREF, "/users/someone", null));
            ace.getGranted().add("read");
            aces.add(ace);
            ace = new DavAce(new DavPrincipal(PrincipalType.PROPERTY, new QName("DAV:", "owner", "somespace"), null));
            ace.getGranted().add("read");
            aces.add(ace);
            sardine.setAcl(url, aces);
            int count = 0;
            for (DavAce davace : sardine.getAcl(url).getAces()) {
                if (davace.getInherited() == null) {
                    count++;
                }
            }
            assertEquals("After setting two ACL, should find them back", 2, count);
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testDavOwner() throws IOException
    {
        final String url = webDavTestContainer.getRandomTestDirectoryUrl();
        Sardine sardine = SardineFactory.begin();
        try {
            sardine.createDirectory(url);
            DavAcl acl = sardine.getAcl(url);
            assertNull(acl.getOwner());
            assertNull(acl.getGroup());
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testDavPrincipals() throws IOException
    {
        final String url = webDavTestContainer.getRandomTestDirectoryUrl();
        Sardine sardine = SardineFactory.begin();
        try {
            sardine.createDirectory(url);
            List<String> principals = sardine.getPrincipalCollectionSet(url);
            assertNotNull(principals);
            for (String p : principals) {
                assertNotNull(p);
            }
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testPutRange() throws Exception
    {
        final AtomicBoolean intercept = new AtomicBoolean();
        final HttpClientBuilder client = HttpClientBuilder.create();
        client.addInterceptorFirst((HttpResponseInterceptor) (r, context) -> {
            if (r.getStatusLine().getStatusCode() == 201) {
                intercept.set(true);
            }
        });
        Sardine sardine = new SardineImpl(client);
        // mod_dav supports Range headers for PUT
        final String url = webDavTestContainer.getRandomTestFileUrl();
        sardine.put(url, new ByteArrayInputStream("Te".getBytes(StandardCharsets.UTF_8)));
        try {
            // Append to existing file
            final Map<String, String> header = Collections.singletonMap(HttpHeaders.CONTENT_RANGE,
                    "bytes " + 2 + "-" + 3 + "/" + 4);

            client.addInterceptorFirst((HttpRequestInterceptor) (r, context) -> {
                assertNotNull(r.getHeaders(HttpHeaders.CONTENT_RANGE));
                assertEquals(1, r.getHeaders(HttpHeaders.CONTENT_RANGE).length);
            });
            client.addInterceptorFirst((HttpResponseInterceptor) (r, context) -> assertEquals(204, r.getStatusLine().getStatusCode()));
            sardine.put(url, new ByteArrayInputStream("st".getBytes(StandardCharsets.UTF_8)), header);

            assertEquals("Test", new BufferedReader(new InputStreamReader(sardine.get(url), StandardCharsets.UTF_8)).readLine());

            assertTrue(intercept.get());
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testGetRange() throws Exception
    {
        final AtomicBoolean intercept = new AtomicBoolean();
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.addInterceptorFirst((HttpResponseInterceptor) (r, context) -> {
            if (r.getStatusLine().getStatusCode() == 206) {
                intercept.set(true);
                // Verify partial content response
                assertNotNull(r.getHeaders(HttpHeaders.CONTENT_RANGE));
                assertEquals(1, r.getHeaders(HttpHeaders.CONTENT_RANGE).length);
            }
        });
        Sardine sardine = new SardineImpl(builder);
        // mod_dav supports Range headers for GET
        final String url = webDavTestContainer.getRandomTestFileUrl();
        sardine.put(url, new ByteArrayInputStream("Te".getBytes(StandardCharsets.UTF_8)));
        try {
            // Resume
            final Map<String, String> header = Collections.singletonMap(HttpHeaders.RANGE, String.format("bytes=%d-", 1));
            final InputStream in = sardine.get(url, header);
            assertNotNull(in);
            assertTrue(intercept.get());
        } finally {
            sardine.delete(url);
        }
    }

    @Test(expected = SardineException.class)
    public void testPutExpectContinue() throws Exception
    {
        // Anonymous PUT to restricted resource
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getRandomTestBasicAuthFileUrl();
        try {
            sardine.put(url, new InputStream() {
                @Override
                public int read() {
                    fail("Expected authentication to fail without sending any body");
                    return -1;
                }
            });
            fail("Expected authorization failure");
        } catch (SardineException e) {
            // Expect Authorization Required
            assertEquals(401, e.getStatusCode());
            throw e;
        }
    }

    @Test
    public void testProxyConfiguration() throws Exception
    {
        Sardine sardine = SardineFactory.begin(null, null, ProxySelector.getDefault());
        try {
            final List<DavResource> resources = sardine.list(webDavTestContainer.getTestFolderUrl());
            assertNotNull(resources);
            assertFalse(resources.isEmpty());
        } catch (SardineException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPath() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        List<DavResource> resources = sardine.list(webDavTestContainer.getTestFolderUrl());
        assertFalse(resources.isEmpty());
        DavResource folder = resources.get(0);
        assertEquals("testFolder", folder.getName());
        assertEquals("/webdav/testFolder/", folder.getPath());
        assertEquals(Long.valueOf(-1), folder.getContentLength());
    }

    @Test
    public void testPut() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getRandomTestFileUrl();
        sardine.put(url, new ByteArrayInputStream("Test".getBytes()));
        try {
            assertTrue(sardine.exists(url));
            assertEquals("Test", new BufferedReader(new InputStreamReader(sardine.get(url), StandardCharsets.UTF_8)).readLine());
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testDepth() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getTestFolderUrl();
        List<DavResource> resources = sardine.list(url, 0);
        assertNotNull(resources);
        assertEquals(1, resources.size());
    }

    @Test
    public void testDelete() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getRandomTestFileUrl();
        sardine.put(url, new ByteArrayInputStream("Test".getBytes()));
        sardine.delete(url);
        assertFalse(sardine.exists(url));
    }

    @Test
    public void testMove() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String source = webDavTestContainer.getRandomTestFileUrl();
        final String destination = webDavTestContainer.getRandomTestFileUrl();
        try {
            sardine.put(source, new ByteArrayInputStream("Test".getBytes()));
            assertTrue(sardine.exists(source));
            sardine.move(source, destination); // implicitly overwrite
            assertFalse(sardine.exists(source));
            assertTrue(sardine.exists(destination));
        } finally {
            sardine.delete(destination);
        }
    }

    @Test
    public void testMoveOverwriting() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String source = webDavTestContainer.getRandomTestFileUrl();
        final String destination = webDavTestContainer.getRandomTestFileUrl();
        try {
            sardine.put(source, new ByteArrayInputStream("Test".getBytes()));
            assertTrue(sardine.exists(source));
            sardine.put(destination, new ByteArrayInputStream("Target".getBytes()));
            assertTrue(sardine.exists(destination));
            sardine.move(source, destination, true);
            assertFalse(sardine.exists(source));
            assertTrue(sardine.exists(destination));
        } finally {
            sardine.delete(destination);
        }
    }

    @Test
    public void testMoveFailOnExisting() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String source = webDavTestContainer.getRandomTestFileUrl();
        final String destination = webDavTestContainer.getRandomTestFileUrl();
        try {
            sardine.put(source, new ByteArrayInputStream("Test".getBytes()));
            assertTrue(sardine.exists(source));
            sardine.put(destination, new ByteArrayInputStream("Safe".getBytes()));
            assertTrue(sardine.exists(destination));
            try {
                sardine.move(source, destination, false);
                fail("Expected SardineException");
            } catch (SardineException e) {
                assertEquals(412, e.getStatusCode());
            }
            assertTrue(sardine.exists(source));
            assertTrue(sardine.exists(destination));
        } finally {
            sardine.delete(source);
            sardine.delete(destination);
        }
    }

    @Test
    public void testMkdir() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getRandomTestDirectoryUrl();
        try {
            sardine.createDirectory(url);
            assertTrue(sardine.exists(url));
            final List<DavResource> resources = sardine.list(url);
            assertNotNull(resources);
            assertEquals(1, resources.size());
        } finally {
            sardine.delete(url);
        }
    }

    @Test
    public void testExists() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        assertTrue(sardine.exists(webDavTestContainer.getTestFolderUrl()));
        assertTrue(sardine.exists(webDavTestContainer.getTestFolderUrl() + "test.txt"));
        assertFalse(sardine.exists(webDavTestContainer.getTestFolderUrl() + "nonExisting.txt"));
    }

    @Test
    public void testDirectoryContentType() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getTestFolderUrl();
        final List<DavResource> resources = sardine.list(url);
        assertNotNull(resources);
        assertFalse(resources.isEmpty());
        DavResource file = resources.get(0);
        assertEquals(DavResource.HTTPD_UNIX_DIRECTORY_CONTENT_TYPE, file.getContentType());
    }

    @Test
    public void testFileContentType() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getTestFolderUrl();
        final List<DavResource> resources = sardine.list(url);
        assertFalse(resources.isEmpty());
        DavResource file = resources.stream().filter(davResource -> davResource.getName().equals("test.txt"))
                .findFirst().orElseThrow(() -> new IllegalStateException("test resource not found"));
        assertEquals("text/plain", file.getContentType());
    }

    @Test
    public void testRedirectPermanently() throws Exception
    {
        Sardine sardine = SardineFactory.begin();
        final String url = webDavTestContainer.getTestFolderUrl();
        try {
            // Test extended redirect handler for PROPFIND
            assertNotNull(sardine.list(url));
            // Test another attempt. Must not fail with circular redirect
            assertNotNull(sardine.list(url));
        } catch (SardineException e) {
            // Should handle a 301 response transparently
            fail("Redirect handling failed");
        }
    }

    @Test
    public void testDisallowLoadExternalDtd() throws Exception
    {
        final CountDownLatch entry = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            try {
                String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\"></html>";
                SardineUtil.unmarshal(new ByteArrayInputStream(html.getBytes()));
                fail("Expected parsing failure for invalid namespace");
            } catch (IOException e) {
                // Success
                assertTrue(e.getCause() instanceof JAXBException);
            } finally {
                entry.countDown();
            }
        });
        t.start();
        assertTrue("Timeout for listing resources. Possibly the XML parser is trying to read the DTD in the response.",
                entry.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testMetadata() throws Exception
    {
        final String url = webDavTestContainer.getRandomTestFileUrl();
        Sardine sardine = SardineFactory.begin();
        try {
            sardine.put(url, "Hello".getBytes(StandardCharsets.UTF_8), "text/plain");

            // 2Setup some custom properties, with custom namespaces
            Map<QName, String> newProps = new HashMap<>();
            newProps.put(new QName("http://my.namespace.com", "mykey", "ns1"), "myvalue");
            newProps.put(new QName(SardineUtil.CUSTOM_NAMESPACE_URI,
                    "mykey",
                    SardineUtil.CUSTOM_NAMESPACE_PREFIX), "my&value2");
            newProps.put(new QName("hello", "mykey", "ns2"), "my<value3");
            sardine.patch(url, newProps);

            // Check properties are properly re-read
            List<DavResource> resources = sardine.list(url);
            assertEquals(resources.size(), 1);
            assertEquals(resources.get(0).getContentLength(), (Long) 5L);
            Map<QName, String> props = resources.get(0).getCustomPropsNS();

            for (Map.Entry<QName, String> entry : newProps.entrySet()) {
                assertEquals(entry.getValue(), props.get(entry.getKey()));
            }

            // 4 check i can properly delete some of those added properties
            List<QName> removeProps = new ArrayList<>();
            removeProps.add(new QName("http://my.namespace.com", "mykey", "ns1"));
            sardine.patch(url, Collections.emptyMap(), removeProps);

            props = sardine.list(url).get(0).getCustomPropsNS();
            assertNull(props.get(new QName("http://my.namespace.com", "mykey")));
            assertEquals(props.get(new QName(SardineUtil.CUSTOM_NAMESPACE_URI, "mykey")), "my&value2");
            assertEquals(props.get(new QName("hello", "mykey")), "my<value3");
        } finally {
            sardine.delete(url);
        }
    }
}
