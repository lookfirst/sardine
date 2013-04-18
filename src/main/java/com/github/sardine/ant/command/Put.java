package com.github.sardine.ant.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import com.github.sardine.Sardine;
import com.github.sardine.ant.Command;


/**
 * A nice ant wrapper around sardine.put().
 *
 * @author Jon Stevens
 */
public class Put extends Command {

	/** The destination URL as a string. */
	private String fUrlString;

	/** The parsed destination URL. */
	private URL fDest;

	/** An sets of source files. */
	private List<FileSet> fSrcFileSets = new ArrayList<FileSet>();

	/** A single source file. */
	private File fSrcFile = null;

	/** The optional content type of the single source file. */
	private String fContentType;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		long fileCounter = 0;

		if (fSrcFile != null) {
			process(fSrcFile, fDest, false);
			fileCounter++;
		} else {
			String urlString = fDest.toString();
			// URL has to be a directory when working with file sets
			urlString = urlString.endsWith("/") ? urlString : (urlString + '/');
			URL baseUrl = new URL(urlString);
			// to prevent unnecessary dir checks
			Set<URL> alreadyCreated = new HashSet<URL>();
			File currentParentDir = null;
			for (Iterator<FileSet> setIterator = fSrcFileSets.iterator(); setIterator.hasNext();) {
				FileSet fileSet = setIterator.next();
				File dir = fileSet.getDir(getProject());
				log("putting from " + dir + " to " + baseUrl);
				String[] files = fileSet.getDirectoryScanner(getProject()).getIncludedFiles();
				for (int idx = 0; idx < files.length; idx++) {
					String fileName = files[idx].replace('\\', '/'); // no Windows backslashes in the URL
					File parentDir = new File(fileName).getParentFile();
					if (parentDir == null || !parentDir.equals(currentParentDir)) {
						checkOrCreateDir(baseUrl, parentDir, alreadyCreated);
						currentParentDir = parentDir;
					}
					File srcFile = new File(dir, fileName);
					URL destUrl = new URL(baseUrl, fileName);
					boolean expectContinue = setIterator.hasNext() || idx + 1 < files.length;
					process(srcFile, destUrl, expectContinue);
					fileCounter++;
				}
			}
		}
		log("putting of " + fileCounter + " file(s) completed");
	}

	/**
	 * Check and if necessary create the parent directory for files to put. Thus it is possible to put whole
	 * directory trees, even if the subdirecories of the tree do not yet exist.
	 *
	 * @param baseUrl is the root which must already exist, parent directories to this URL will not be created
	 *        automatically
	 * @param dir to check for and create if it does not yet exist
	 * @param createdDirs a cache for already created directories which saves several
	 *        {@link Sardine#exists(String)} calls
	 * @throws IOException
	 */
	private void checkOrCreateDir(URL baseUrl, File dir, Set<URL> createdDirs) throws IOException {
		if (dir != null) {
			checkOrCreateDir(baseUrl, dir.getParentFile(), createdDirs);
		}
		URL dirUrl = dir == null ? baseUrl : new URL(baseUrl, dir.getPath().replace('\\', '/'));
		if (createdDirs.contains(dirUrl)) {
			return;
		}
		if (!getSardine().exists(dirUrl.toString())) {
			log("creating directory " + dirUrl, Project.MSG_VERBOSE);
			getSardine().createDirectory(dirUrl.toString());
			createdDirs.add(dirUrl);
		}
	}

	/**
	 * Process an individual file with sardine.put()
	 */
	private void process(File file, URL dest, boolean expectContinue) throws Exception {
		log("putting " + file + " to " + dest + " with expectContinue=" + expectContinue, Project.MSG_VERBOSE);
		getSardine().put(dest.toString(), new FileInputStream(file), fContentType, expectContinue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (fUrlString == null)
			throw new NullPointerException("url must not be null");
		fDest = new URL(fUrlString);

		if (fSrcFile == null && fSrcFileSets.size() == 0)
			throw new NullPointerException("Need to define either the file attribute or add a fileset.");

		if (fSrcFile != null && !fSrcFile.isFile())
			throw new Exception(fSrcFile + " is not a file");
	}

	/** Set destination URL. */
	public void setUrl(String urlString) {
		fUrlString = urlString;
	}

	/** Set source file. */
	public void setFile(File file) {
		fSrcFile = file;
	}

	/** Set optional content type of the source file. */
	public void setContentType(String contentType) {
		fContentType = contentType;
	}

	/** Add a source file set. */
	public void addConfiguredFileset(FileSet value) {
		fSrcFileSets.add(value);
	}
}
