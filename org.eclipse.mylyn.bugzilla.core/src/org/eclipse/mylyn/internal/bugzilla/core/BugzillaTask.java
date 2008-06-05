/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/*
 * Created on 14-Jan-2005
 */
package org.eclipse.mylyn.internal.bugzilla.core;

import org.eclipse.mylyn.tasks.core.AbstractTask;

/**
 * @author Mik Kersten
 * @author Rob Elves
 */
public class BugzillaTask extends AbstractTask {

	private String severity;

	private String product;

	public BugzillaTask(String repositoryUrl, String id, String label) {
		super(repositoryUrl, id, label);
		setUrl(BugzillaClient.getBugUrlWithoutLogin(repositoryUrl, id));
	}

	@Override
	public String getTaskKind() {
		return IBugzillaConstants.BUGZILLA_TASK_KIND;
	}

	@Override
	public String toString() {
		return "Bugzilla task: " + getHandleIdentifier();
	}

	@Override
	public String getConnectorKind() {
		return BugzillaCorePlugin.REPOSITORY_KIND;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	@Override
	public boolean isLocal() {
		// ignore
		return false;
	}

}