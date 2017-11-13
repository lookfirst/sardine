package com.github.sardine.ant.command;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.StringResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ResourceUtils;

import com.github.sardine.Sardine;
import com.github.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.put().
 *
 * @author Jon Stevens
 */
public class Put extends Command {

	/** The destination URL as a string. */
	private String urlString;

	/** The parsed destination URL. */
	private URL dest;

	/** An sets of source files. */
	private List<FileSet> srcFileSets = new ArrayList<FileSet>();

	/** A single source file. */
	private File srcFile = null;

	/** The optional content type of the single source file. */
	private String contentType;

	/** Settings for put with filtering. */
	private FilterSetCollection filterSets = new FilterSetCollection();
	private Vector<FilterChain> filterChains = new Vector<FilterChain>();
	private String inputEncoding = null;
	private String outputEncoding = null;
	private boolean inMemoryFiltering = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws Exception {
		long fileCounter = 0;

		if (srcFile != null) {
			process(srcFile, dest, false);
			fileCounter++;
		} else {
			String urlString = dest.toString();
			// URL has to be a directory when working with file sets
			urlString = urlString.endsWith("/") ? urlString : (urlString + '/');
			URL baseUrl = new URL(urlString);
			// to prevent unnecessary dir checks
			Set<URL> alreadyCreated = new HashSet<URL>();
			File currentParentDir = null;
			for (Iterator<FileSet> setIterator = srcFileSets.iterator(); setIterator.hasNext();) {
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
	 * directory trees, even if the sub-directories of the tree do not yet exist.
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
		if (filterSets.hasFilters() || !filterChains.isEmpty()) {
			Resource filteredResult;
			File tempFile = null;
			if (inMemoryFiltering) {
				filteredResult = new StringResource();
			} else {
				tempFile = File.createTempFile("filterResult", ".tmp");
				filteredResult = new FileResource(tempFile);
			}
			try {
				ResourceUtils.copyResource(new FileResource(file), filteredResult, filterSets, filterChains, false,
						false, false, inputEncoding, outputEncoding, getProject());
				getSardine().put(dest.toString(), filteredResult.getInputStream(), contentType, expectContinue);
			} finally {
				FileUtils.delete(tempFile);
			}
		} else {
			getSardine().put(dest.toString(), file, contentType, expectContinue);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateAttributes() throws Exception {
		if (urlString == null)
			throw new NullPointerException("url must not be null");
		dest = new URL(urlString);

		if (srcFile == null && srcFileSets.size() == 0)
			throw new NullPointerException("Need to define either the file attribute or add a fileset.");

		if (srcFile != null && !srcFile.isFile())
			throw new Exception(srcFile + " is not a file");
	}

	/** Set destination URL. */
	public void setUrl(String urlString) {
		this.urlString = urlString;
	}

	/** Set source file. */
	public void setFile(File file) {
		this.srcFile = file;
	}

	/** Set optional content type of the source file. */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/** Add a source file set. */
	public void addConfiguredFileset(FileSet value) {
		this.srcFileSets.add(value);
	}

	/**
	 * Add a filterset.
	 *
	 * @return a filter set object.
	 */
	public FilterSet createFilterSet() {
		FilterSet filterSet = new FilterSet();
		filterSets.addFilterSet(filterSet);
		return filterSet;
	}

	/**
	 * Add a FilterChain.
	 *
	 * @return a filter chain object.
	 */
	public FilterChain createFilterChain() {
		FilterChain filterChain = new FilterChain();
		filterChains.addElement(filterChain);
		return filterChain;
	}

	/**
	 * Set the character encoding.
	 * 
	 * @param encoding the character encoding.
	 */
	public void setEncoding(String encoding) {
		inputEncoding = encoding;
		if (outputEncoding == null) {
			outputEncoding = encoding;
		}
	}

	/**
	 * Get the character encoding to be used.
	 * 
	 * @return the character encoding, <code>null</code> if not set.
	 */
	public String getEncoding() {
		return inputEncoding;
	}

	/**
	 * Set the character encoding for output files.
	 * 
	 * @param encoding the output character encoding.
	 */
	public void setOutputEncoding(String encoding) {
		outputEncoding = encoding;
	}

	/**
	 * Get the character encoding for output files.
	 * 
	 * @return the character encoding for output files, <code>null</code> if not set.
	 */
	public String getOutputEncoding() {
		return outputEncoding;
	}

	/**
	 * Get the setting for in memory filtering.
	 * 
	 * @return the setting for in memory filtering
	 */
	public boolean getInMemoryFiltering() {
		return inMemoryFiltering;
	}

	/**
	 * Defines whether the filtering result should be stored in memory or in a temporary file before it becomes
	 * put.
	 * <p>
	 * Due to the filtering implementation of Ant is not done in a streaming manner, it is required to store the
	 * result before it can be put to the destination URL. For small files it is reasonable to hold the result in
	 * memory. Large files will require a according amount of heap memory and thus it is advisable to turn the in
	 * memory buffer off.
	 * 
	 * @param inMemoryFiltering default is <code>true</code>; <code>false</code> will write the results to a
	 *        temporary file
	 */
	public void setInMemoryFiltering(boolean inMemoryFiltering) {
		this.inMemoryFiltering = inMemoryFiltering;
	}
}
