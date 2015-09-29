[![Build Status](https://travis-ci.org/lookfirst/sardine.png)](https://travis-ci.org/lookfirst/sardine)

Sardine is useful for interacting with a webdav server and is much easier to programmatically manage remote files than with FTP.

I looked at the other Java webdav clients out there [slide](http://jakarta.apache.org/slide/), [Jackrabbit](http://jackrabbit.apache.org/) and [webdavclient4j](http://sourceforge.net/projects/webdavclient4j/). 
None of them do things quite the way I wanted.

The [UsageGuide](https://github.com/lookfirst/sardine/wiki/UsageGuide) documents how to use Sardine. If you are hungry, this is an appetizer for retrieving a directory listing from a remote webdav server:

```java
Sardine sardine = SardineFactory.begin();
List<DavResource> resources = sardine.list("http://yourdavserver.com/adirectory/");
for (DavResource res : resources)
{
     System.out.println(res);
}
```

Sardine is focused on being a useful library for common use cases. I also need it to support the latest version of [HttpClient](http://httpcomponents.apache.org/). It abstracts away the connection details and provides easy to use methods to accomplish webdav'y actions.

There is a [SardineTask](https://github.com/lookfirst/sardine/wiki/SardineTask) so that you can use Sardine directly in your Ant scripts.

Sardine uses JAXB to process XML responses from the webdav server. The generated code for this is based on the excellent webdav.xsd contained in the [Apache Wink](http://wink.apache.org/) project.

Sardine is fully stable and is being used in production on a very high traffic site (140+ concurrent connections 24/7). Click the Issues tab to submit requests. Most development is just adding new use cases. Check back often for new releases.

Questions? Please ask on our [mailing list](https://groups.google.com/forum/#!forum/sardine-dav). Issues? File an issue in the github issue tracker.

Sardine available under the Apache License 2.0.

If you like this library, I'd appreciate if you would blog/tweet about it. If you don't like it, well...
