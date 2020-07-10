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
	String serverUrl;
	/**
	 * Remote directory path
	 */
	String remoteDirectory;
	/**
	 * Local directory path
	 */
	String localDirectory;

	/**
	 * true if existent local files will be overwritten; otherwise, false.
	 */
	boolean overwriteFiles = false;

	/**
	 * true if existent local files will be skipped; otherwise, false.
	 */
	boolean skipExistingFiles = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		StringBuilder sb = new StringBuilder();

		if (serverUrl == null) {
			sb.append("[serverUrl] must not be null\n");
		}
		if (remoteDirectory == null) {
			sb.append("[remoteDirectory] must not be null\n");
		}
		if (localDirectory == null) {
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
		URI remoteDirectoryUrl = new URI(serverUrl + '/').resolve(remoteDirectory);

		String remoteDirectoryPath = remoteDirectoryUrl.getPath();

		List<DavResource> resource = getSardine().list(remoteDirectoryUrl.toString(), -1);

		for (DavResource davResource : resource) {
			if (!davResource.isDirectory()) {
				String filePathRelativeToRemoteDirectory = davResource.getPath().replace(remoteDirectoryPath, "");
				Path localFilePath = Paths.get(localDirectory, filePathRelativeToRemoteDirectory);

				if (skipExistingFiles && Files.exists(localFilePath)) {
					log("skipping download of already existing file " + localFilePath);
					continue;
				}

				Files.createDirectories(localFilePath.getParent());

				log("downloading " + filePathRelativeToRemoteDirectory + " to " + localFilePath);

				InputStream ioStream = getSardine().get(serverUrl + davResource.getHref().toString());
				try {
					if (overwriteFiles) {
						Files.copy(ioStream, localFilePath, StandardCopyOption.REPLACE_EXISTING);
					} else {
						Files.copy(ioStream, localFilePath);
					}

				} finally {
					ioStream.close();
				}
			}
		}
		log("downloaded files to " + localDirectory);
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public void setRemoteDirectory(String remoteDirectory) {
		this.remoteDirectory = remoteDirectory;
	}

	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}

	public void setOverwriteFiles(boolean overwriteFiles) {
		this.overwriteFiles = overwriteFiles;
	}

	public void setSkipExistingFiles(boolean skipExistingFiles) {
		this.skipExistingFiles = skipExistingFiles;
	}
}
