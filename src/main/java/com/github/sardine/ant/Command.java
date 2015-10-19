package com.github.sardine.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;

import com.github.sardine.Sardine;


/**
 * Base class for a Command which represents a sardine command
 * such as sardine.put().
 *
 * @author jonstevens
 */
public abstract class Command extends ProjectComponent
{

	/** Parent task. */
	private SardineTask sardineTask = null;

	/**
	 * This is called prior to {@link #execute()} in order to enable the command implementation to validate
	 * the provided attributes.
	 *
	 * @throws Exception if the command is misconfigured
	 */
	protected abstract void validateAttributes() throws Exception;

	/**
	 * Execute the command.
	 *
	 * @throws Exception if the command failed
	 */
	protected abstract void execute() throws Exception;

	/**
	 * Check the command attribute and execute it.
	 *
	 * @throws Exception if the command is misconfigured or failed for some other reason
	 */
	public final void executeCommand() throws Exception {
		try {
			validateAttributes();
			execute();
		} catch (Exception e) {
			e.printStackTrace();
			if (sardineTask.isFailonerror()) {
				throw e;
			}
			sardineTask.log(getClass().getSimpleName() + " failed: " + e.getLocalizedMessage(), e,
					Project.MSG_ERR);
		}
	}

	/**
	 * Sets the SardineTask
	 */
	public final void setTask(SardineTask task) {
		sardineTask = task;
	}

	/**
	 * Returns the Sardine for this command.
	 *
	 * @return the Sardine for this command
	 */
	protected final Sardine getSardine() {
		return sardineTask.getSardine();
	}
}
