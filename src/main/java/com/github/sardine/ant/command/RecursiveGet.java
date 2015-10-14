package com.github.sardine.ant.command;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.github.sardine.DavResource;
import com.github.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.list() and sardine.get().
 * 
 * @author andreafonti
 */
public class RecursiveGet extends Command {

	/**
	 * Webdav server url
	 */
	String fServerUrl;
	/**
	 * Remote directory path
	 */
	String fRemoteDirectory;
	/**
	 * Local directory path
	 */
	String fLocalDirectory;

	/**
	 * true if existent local files will be overwritten; otherwise, false.
	 */
	boolean fOverwriteFiles = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		StringBuilder sb = new StringBuilder();

		if (fServerUrl == null) {
			sb.append("[serverUrl] must not be null\n");
		}
		if (fRemoteDirectory == null) {
			sb.append("[remoteDirectory] must not be null\n");
		}
		if (fLocalDirectory == null) {
			sb.append("[localDirectory] must not be null\n");
		}

		if (sb.length() > 0) {
			throw new IllegalArgumentException(sb.substring(0, sb.length() - 1));
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {

		// add an extra leading slash, if it will be swallowed by resolve if
		// duplicated
		URI remoteDirectoryUrl = new URI(fServerUrl + '/').resolve(fRemoteDirectory);

		String remoteDirectoryPath = remoteDirectoryUrl.getPath();

		List<DavResource> resource = getSardine().list(remoteDirectoryUrl.toString(), -1);

		for (DavResource davResource : resource) {
			if (!davResource.isDirectory()) {
				String filePathRelativeToRemoteDirectory = davResource.getPath().replace(remoteDirectoryPath, "");
				Path localFilePath = Paths.get(fLocalDirectory, filePathRelativeToRemoteDirectory);

				Files.createDirectories(localFilePath.getParent());

				log("downloading " + filePathRelativeToRemoteDirectory + " to " + localFilePath);

				String remoteFileUrl = new URI(fServerUrl + '/').resolve(davResource.getPath()).toString();
				InputStream ioStream = getSardine().get(remoteFileUrl);
				try {
					if (fOverwriteFiles) {
						Files.copy(ioStream, localFilePath, StandardCopyOption.REPLACE_EXISTING);
					} else {
						Files.copy(ioStream, localFilePath);
					}

				} finally {
					ioStream.close();
				}
			}
		}
		log("downloaded files to " + fLocalDirectory);
	}

	public void setServerUrl(String url) {
		this.fServerUrl = url;
	}

	public void setRemoteDirectory(String resource) {
		this.fRemoteDirectory = resource;
	}

	public void setLocalDirectory(String destination) {
		this.fLocalDirectory = destination;
	}

	public void setOverwriteFiles(boolean overwriteFiles) {
		this.fOverwriteFiles = overwriteFiles;
	}

}
