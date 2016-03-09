package com.github.sardine;

import com.github.sardine.model.Response;

import javax.xml.namespace.QName;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Describes a directory on a remote server.
 */
public class DavFolder extends DavResource implements Iterable<DavResource> {

    //region Variables
    private final List<DavFolder> davFolders;
    private final List<DavFile> davFiles;
    //endregion

    //region Constructors
    protected DavFolder(String href, Date creation, Date modified, String contentType, Long contentLength, String etag, String displayName, List<QName> resourceTypes, String contentLanguage, List<QName> supportedReports, Map<QName, String> customProps) throws URISyntaxException {
        super(href, creation, modified, contentType, contentLength, etag, displayName, resourceTypes, contentLanguage, supportedReports, customProps);
        this.davFolders = new ArrayList<DavFolder>();
        this.davFiles = new ArrayList<DavFile>();
    }

    protected DavFolder(String href, Date creation, Date modified, String contentType, Long contentLength, String etag, String displayName, List<QName> resourceTypes, String contentLanguage, List<QName> supportedReports, Map<QName, String> customProps, DavResource... davResources) throws URISyntaxException {
        this(href, creation, modified, contentType, contentLength, etag, displayName, resourceTypes, contentLanguage, supportedReports, customProps);
        if (davResources != null) {
            for (DavResource davResource : davResources) {
                this.routeDavResource(davResource);
            }
        }
    }

    protected DavFolder(Response response) throws URISyntaxException {
        super(response);
        this.davFolders = new ArrayList<DavFolder>();
        this.davFiles = new ArrayList<DavFile>();
    }
    //endregion

    public static DavFolder fromDavResource(DavResource davResource) {
        if (!davResource.isDirectory()) {
            throw new RuntimeException("ERROR: " + davResource.getDisplayName() + " is not a folder");
        }
        try {
            return new DavFolder(
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
                    davResource.getCustomPropsNS(),
                    null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    //region Getters
    public List<DavFolder> getDavFolders() {
        return this.davFolders;
    }

    public List<DavFile> getDavFiles() {
        return this.davFiles;
    }
    //endregion

    //region Public Methods

    /**
     * Adds all the DavResources in the DavFolder
     *
     * @param davResources The DavResources
     */
    public void addResources(List<DavResource> davResources) {
        for (DavResource davResource : davResources) {
            this.routeDavResource(davResource);
        }
    }

    /**
     * Add a DavResource in the DavFolder
     *
     * @param davResource The DavResource to add
     */
    public void addResource(DavResource davResource) {
        if (davResource.isDirectory()) {
            this.addFolder(DavFolder.fromDavResource(davResource));
            return;
        }
        this.addFile(DavFile.fromDavResource(davResource));
    }

    /**
     * Add a DavFolder as child
     *
     * @param davFolder The DavFolder
     */
    public void addFolder(DavFolder davFolder) {
        this.davFolders.add(davFolder);
    }

    /**
     * Add a DavFile as child
     *
     * @param davFile The DavFile
     */
    public void addFile(DavFile davFile) {
        this.davFiles.add(davFile);
    }
    //endregion

    private void routeDavResource(DavResource davResource) {
        String[] rootSegments = this.getHref().getPath().split("/");
        String[] resourceSegments = davResource.getHref().getPath().split("/");

        for (int i = 0; i < rootSegments.length; i++) {
            if (!rootSegments[i].equals(resourceSegments[i])) {
                throw new RuntimeException("ERROR: Invalid href");
            }
        }

        for (DavFolder davFolder : davFolders) {
            if (davFolder.getName().equals(resourceSegments[rootSegments.length])) {
                davFolder.routeDavResource(davResource);
                return;
            }
        }

        this.addResource(davResource);
    }

    //region Overrides

    @Override
    public boolean isDirectory() {
        return true;
    }

    /**
     * Iterates all DavFolders and DavFiles
     *
     * @return Iterator for DavResources inside
     */
    @Override
    public Iterator<DavResource> iterator() {
        List<DavResource> davResources = new LinkedList<DavResource>(this.davFiles);
        davResources.addAll(this.davFolders);
        return davResources.iterator();
    }

    //endregion

}
