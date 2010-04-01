package com.googlecode.sardine.ant;

import org.apache.tools.ant.ProjectComponent;


/**
 * Base class for a Command which represents a sardine command
 * such as sardine.put().
 *
 * @author jonstevens
 */
public abstract class Command extends ProjectComponent
{
	private SardineTask task = null;

	public abstract void execute() throws Exception;

	protected abstract void validateAttributes() throws Exception;

	/**
	 *
	 * @throws Exception
	 */
	public void executeCommand() throws Exception
	{
		try
		{
			this.validateAttributes();
			this.execute();
		}
		catch (Exception e)
		{
			if (this.task.isFailonerror())
			{
				throw new Exception(e);
			}
			else
			{
				this.task.log(e.getMessage());
			}
		}
	}

	/**
	 * Sets the SardineTask
	 */
	public void setTask(SardineTask task)
	{
		this.task = task;
	}

	/**
	 * Gets the SardineTask
	 */
	public SardineTask getTask()
	{
		return this.task;
	}
}
