package com.github.sardine.ant;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.ant.command.Copy;
import com.github.sardine.ant.command.CreateDirectory;
import com.github.sardine.ant.command.Delete;
import com.github.sardine.ant.command.Exists;
import com.github.sardine.ant.command.Move;
import com.github.sardine.ant.command.Put;

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
	public void execute()
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
