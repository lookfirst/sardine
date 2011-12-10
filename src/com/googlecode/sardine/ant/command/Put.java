package com.googlecode.sardine.ant.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	private List<FileSet> filesets = new ArrayList<FileSet>();

	/** */
	private File file = null;

	/** */
	private String contentType;

	/** */
	@Override
	public void execute() throws IOException
	{
		Project p = this.getProject();

		if (this.file != null)
		{
			this.process(this.file);
		}
		else
		{
			for (FileSet fileset : this.filesets)
			{
				File dir = fileset.getDir(p);
				DirectoryScanner ds = fileset.getDirectoryScanner(p);

				for (String f : ds.getIncludedFiles())
				{
					File absolute = new File(dir, f);
					if (absolute.isFile())
					{
						this.process(absolute);
					}
				}
			}
		}
	}

	/**
	 * Process an individual file with sardine.put()
	 */
	protected void process(File file) throws IOException
	{
		this.getTask().getSardine().put(this.url, new FileInputStream(file), contentType);
	}

	/** */
	@Override
	protected void validateAttributes()
	{
		if (this.url == null)
		{
			throw new IllegalArgumentException("url cannot be null");
		}
		if (this.file == null && this.filesets.size() == 0)
		{
			throw new IllegalArgumentException("Need to define the file attribute or add a fileset.");
		}
		if (this.file != null && !this.file.exists())
		{
			throw new IllegalArgumentException("Could not find file: " + this.file);
		}
	}

	/** */
	public void setUrl(String url)
	{
		this.url = url;
	}

	/** */
	public void setFile(File file)
	{
		this.file = file;
	}

	/** */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/** */
	public void addConfiguredFileset(FileSet value)
	{
		this.filesets.add(value);
	}
}
