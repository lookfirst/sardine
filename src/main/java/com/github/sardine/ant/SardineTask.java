package com.github.sardine.ant;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.ant.command.Copy;
import com.github.sardine.ant.command.CreateDirectory;
import com.github.sardine.ant.command.Delete;
import com.github.sardine.ant.command.Exists;
import com.github.sardine.ant.command.RecursiveGet;
import com.github.sardine.ant.command.Move;
import com.github.sardine.ant.command.Put;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Sardine ant Task
 *
 * @author jonstevens
 */
public class SardineTask extends Task
{
	/** Commands. */
	private List<Command> commands = new ArrayList<Command>();

	/** Attribute failOnError. */
	private boolean failOnError = false;

	/** Attribute username. */
	private String username = null;

	/** Attribute password. */
	private String password = null;

	/** Attribute domain for NTLM authentication. */
	private String domain = null;

	/** Attribute workstation for NTLM authentication. */
	private String workstation = null;

	/** Attribute ignoreCookies. */
	private boolean ignoreCookies = false;

	/** Attribute preemptiveAuthenticationHost. */
	private String preemptiveAuthenticationHost;

	/** Reference to sardine impl. */
	private Sardine sardine = null;

	/** Add a copy command. */
	public void addCopy(Copy copy) {
		addCommand(copy);
	}

	/** Add a createDirectory command. */
	public void addCreateDirectory(CreateDirectory createDirectory) {
		addCommand(createDirectory);
	}

	/** Add a delete command. */
	public void addDelete(Delete delete) {
		addCommand(delete);
	}

	/** Add a delete command. */
	public void addExists(Exists exists) {
		addCommand(exists);
	}

	/** Add a move command. */
	public void addMove(Move move) {
		addCommand(move);
	}

	/** Add a put command. */
	public void addPut(Put put) {
		addCommand(put);
	}
	
	/** Add a recursive get command. */
	public void addRecursiveGet(RecursiveGet get) {
		addCommand(get);
	}

	/** Internal addCommand implementation. */
	private void addCommand(Command command) {
		command.setTask(this);
		commands.add(command);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws BuildException {
		try {
			if (domain == null && workstation == null) {
				sardine = SardineFactory.begin(username, password);
			} else {
				sardine = SardineFactory.begin();
				sardine.setCredentials(username, password, domain, workstation);
			}

			if (ignoreCookies) {
				sardine.ignoreCookies();
			}

			if (preemptiveAuthenticationHost != null && !preemptiveAuthenticationHost.isEmpty()) {
				sardine.enablePreemptiveAuthentication(preemptiveAuthenticationHost);
			}

			for (Command command: commands) {
				command.executeCommand();
			}
		} catch (Exception e) {
			throw new BuildException("failed: " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Set the fail on error behavior.
	 *
	 * @param failonerror <code>true</code> to fail on the first error; <code>false</code> to just log errors
	 *        and continue
	 */
	public void setFailonerror(boolean failonerror) {
		this.failOnError = failonerror;
	}

	/**
	 * Returns the fail on error behavior.
	 *
	 * @return <code>true</code> to fail on the first error; <code>false</code> to just log errors and
	 *         continue
	 */
	public boolean isFailonerror() {
		return failOnError;
	}

	/**
	 * Setter for attribute username.
	 *
	 * @param username used for authentication
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Setter for attribute password.
	 *
	 * @param password used for authentication
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Setter for attribute domain for NTLM authentication.
	 *
	 * @param domain used for NTLM authentication
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Setter for attribute workstation for NTLM authentication.
	 *
	 * @param workstation used for NTLM authentication
	 */
	public void setWorkstation(String workstation) {
		this.workstation = workstation;
	}

	/**
	 * Setter for attribute ignoreCookies.
	 *
	 * @param ignoreCookies to ignore cookies.
	 */
	public void setIgnoreCookies(boolean ignoreCookies) {
		this.ignoreCookies = ignoreCookies;
	}

	/**
	 * Setter for attribute preemptiveAuthenticationHost.
	 *
	 * @param host name of the host to acivate the preemptive authentication for
	 */
	public void setPreemptiveAuthenticationHost(String host) {
		this.preemptiveAuthenticationHost = host;
	}

	/**
	 * Returns the sardine impl.
	 *
	 * @return the sardine impl
	 */
	public Sardine getSardine() {
		return sardine;
	}
}
