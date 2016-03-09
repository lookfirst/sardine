package com.github.sardine;

import com.github.sardine.model.Response;

import javax.xml.namespace.QName;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Describes a file on a remote server.
 */
public class DavFile extends DavResource {

    protected DavFile(String href, Date creation, Date modified, String contentType, Long contentLength, String etag, String displayName, List<QName> resourceTypes, String contentLanguage, List<QName> supportedReports, Map<QName, String> customProps) throws URISyntaxException {
        super(href, creation, modified, contentType, contentLength, etag, displayName, resourceTypes, contentLanguage, supportedReports, customProps);
    }

    public DavFile(Response response) throws URISyntaxException {
        super(response);
    }

    public static DavFile fromDavResource(DavResource davResource) {
        if (davResource.isDirectory()) {
            throw new RuntimeException("ERROR: " + davResource.getDisplayName() + " is a folder");
        }

        try {
            return new DavFile(
                    davResource.getHref().toString(),
                    davResource.getCreation(),
                    davResource.getModified(),
                    davResource.getContentType(),
                    davResource.getContentLength(),
                    davResource.getEtag(),
                    davResource.getDisplayName(),
                    davResource.getResourceTypes(),
                    davResource.getContentLanguage(),
                    davResource.getSupportedReports(),
                    davResource.getCustomPropsNS());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

}
