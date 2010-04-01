package com.googlecode.sardine.ant.command;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import com.googlecode.sardine.ant.Command;

/**
 * A nice ant wrapper around sardine.put().
 *
 * @author Jon Stevens
 */
public class Put extends Command
{
	/** */
	private String url;

	/** */
	List<FileSet> filesets = new ArrayList<FileSet>();

	/** */
	File file = null;

	/** */
	@Override
	public void execute() throws Exception
	{
		Project p = this.getProject();

		if (this.file != null)
		{
			this.process(this.file);
		}
		else
		{
			for (FileSet fileset: this.filesets)
			{
				File dir = fileset.getDir(p);
				DirectoryScanner ds = fileset.getDirectoryScanner(p);

				for (String file: ds.getIncludedFiles())
				{
					File absolute = new File(dir, file);
					if (absolute.isFile())
						this.process(absolute);
				}
			}
		}
	}

	/**
	 * Process an individual file with sardine.put()
	 */
	protected void process(File file) throws Exception
	{
		this.getTask().getSardine().put(this.url, new FileInputStream(file));
	}

	/** */
	@Override
	protected void validateAttributes() throws Exception
	{
		if (this.url == null)
			throw new NullPointerException("url cannot be null");

		if (this.file == null && this.filesets.size() == 0)
			throw new NullPointerException("Need to define the file attribute or add a fileset.");

		if (this.file != null && !this.file.exists())
			throw new Exception("Could not find file: " + this.file);
	}

	/** */
	public void setUrl(String url)
	{
		this.url = url;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	/** */
	public void addConfiguredFileset(FileSet value)
	{
		this.getTask().log("here");
		this.filesets.add(value);
	}
}
