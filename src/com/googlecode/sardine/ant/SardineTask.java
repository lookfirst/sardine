package com.googlecode.sardine.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.ant.command.Copy;
import com.googlecode.sardine.ant.command.CreateDirectory;
import com.googlecode.sardine.ant.command.Delete;
import com.googlecode.sardine.ant.command.Exists;
import com.googlecode.sardine.ant.command.Move;
import com.googlecode.sardine.ant.command.Put;

/**
 * Controller for the Sardine ant Task
 *
 * @author jonstevens
 */
public class SardineTask extends Task
{
	/** */
	private List<Command> commands = new ArrayList<Command>();

	/** */
	private boolean failonerror = false;
	private String username = null;
	private String password = null;
	private Sardine sardine = null;

	/** */
	public void addCopy(Copy copy)
	{
		this.addCommand(copy);
	}

	/** */
	public void addCreateDirectory(CreateDirectory createDirectory)
	{
		this.addCommand(createDirectory);
	}

	/** */
	public void addDelete(Delete delete)
	{
		this.addCommand(delete);
	}

	/** */
	public void addExists(Exists exists)
	{
		this.addCommand(exists);
	}

	/** */
	public void addMove(Move move)
	{
		this.addCommand(move);
	}

	/** */
	public void addPut(Put put)
	{
		this.addCommand(put);
	}

	/** */
	private void addCommand(Command command)
	{
		command.setTask(this);
		this.commands.add(command);
	}

	/** */
	@Override
	public void execute() throws BuildException
	{
		try
		{
			this.sardine = SardineFactory.begin(this.username, this.password);

			for (Command command : this.commands)
			{
				command.executeCommand();
			}
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
	}

	/** */
	public void setFailonerror(boolean failonerror)
	{
		this.failonerror = failonerror;
	}

	/** */
	public boolean isFailonerror()
	{
		return this.failonerror;
	}

	/** */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/** */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/** */
	public Sardine getSardine()
	{
		return this.sardine;
	}
}
