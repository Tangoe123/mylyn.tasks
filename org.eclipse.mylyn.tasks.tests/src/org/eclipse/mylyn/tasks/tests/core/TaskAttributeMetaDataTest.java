/*******************************************************************************
 * Copyright (c) 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.core;

import junit.framework.TestCase;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.tests.connector.MockRepositoryConnector;

/**
 * @author Benjamin Muskalla
 */
public class TaskAttributeMetaDataTest extends TestCase {

	private TaskData data;

	@Override
	protected void setUp() throws Exception {
		TaskRepository taskRepository = new TaskRepository("kind", "url");
		data = new TaskData(new TaskAttributeMapper(taskRepository), MockRepositoryConnector.CONNECTOR_KIND,
				MockRepositoryConnector.REPOSITORY_URL, "taskid");
	}

	public void testInitialRequiredAttribute() {
		TaskAttribute attribute = new TaskAttribute(data.getRoot(), "attributeId");
		boolean required = attribute.getMetaData().isRequired();
		assertFalse(required);
	}

	public void testLifecycleRequiredAttribute() {
		TaskAttribute attribute = new TaskAttribute(data.getRoot(), "attributeId");
		attribute.getMetaData().setRequired(true);
		assertTrue(attribute.getMetaData().isRequired());
		attribute.getMetaData().setRequired(false);
		assertFalse(attribute.getMetaData().isRequired());
	}

}
