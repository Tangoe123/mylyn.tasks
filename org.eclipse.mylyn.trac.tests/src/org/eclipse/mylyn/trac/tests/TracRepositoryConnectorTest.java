/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.trac.tests;

import java.net.MalformedURLException;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylar.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylar.internal.trac.MylarTracPlugin;
import org.eclipse.mylar.internal.trac.TracRepositoryConnector;
import org.eclipse.mylar.internal.trac.TracRepositoryQuery;
import org.eclipse.mylar.internal.trac.TracTask;
import org.eclipse.mylar.internal.trac.core.ITracClient;
import org.eclipse.mylar.internal.trac.core.InvalidTicketException;
import org.eclipse.mylar.internal.trac.core.ITracClient.Version;
import org.eclipse.mylar.internal.trac.model.TracSearch;
import org.eclipse.mylar.internal.trac.model.TracTicket;
import org.eclipse.mylar.internal.trac.model.TracTicket.Key;
import org.eclipse.mylar.internal.trac.ui.wizard.TracRepositorySettingsPage;
import org.eclipse.mylar.tasks.core.AbstractQueryHit;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.TaskList;
import org.eclipse.mylar.tasks.core.TaskRepository;
import org.eclipse.mylar.tasks.ui.AbstractRepositoryConnector;
import org.eclipse.mylar.tasks.ui.TaskRepositoryManager;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.trac.tests.support.TestFixture;
import org.eclipse.mylar.trac.tests.support.XmlRpcServer.TestData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Steffen Pingel
 */
public class TracRepositoryConnectorTest extends TestCase {

	private TestData data;

	private TaskRepository repository;

	private TaskRepositoryManager manager;

	private TracRepositoryConnector connector;

	private TaskList tasklist;

	protected void setUp() throws Exception {
		super.setUp();

		manager = TasksUiPlugin.getRepositoryManager();
		manager.clearRepositories();

		tasklist = TasksUiPlugin.getTaskListManager().getTaskList();

		data = TestFixture.initializeRepository1();
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		// TestFixture.cleanupRepository1();
	}

	protected void init(Version version) {
		String kind = MylarTracPlugin.REPOSITORY_KIND;

		repository = new TaskRepository(kind, Constants.TEST_REPOSITORY1_URL);
		repository.setAuthenticationCredentials(Constants.TEST_REPOSITORY1_USERNAME,
				Constants.TEST_REPOSITORY1_USERNAME);
		repository.setTimeZoneId(ITracClient.TIME_ZONE);
		repository.setCharacterEncoding(ITracClient.CHARSET);
		repository.setVersion(version.name());

		manager.addRepository(repository);

		AbstractRepositoryConnector abstractConnector = manager.getRepositoryConnector(kind);
		assertEquals(abstractConnector.getRepositoryType(), kind);

		connector = (TracRepositoryConnector) abstractConnector;
		connector.setForceSyncExec(true);
	}

	public void testGetRepositoryUrlFromTaskUrl() {
		TracRepositoryConnector connector = new TracRepositoryConnector();
		assertEquals("http://host/repo", connector.getRepositoryUrlFromTaskUrl("http://host/repo/ticket/1"));
		assertEquals("http://host", connector.getRepositoryUrlFromTaskUrl("http://host/ticket/2342"));
		assertEquals(null, connector.getRepositoryUrlFromTaskUrl("http://host/repo/2342"));
		assertEquals(null, connector.getRepositoryUrlFromTaskUrl("http://host/repo/ticket-2342"));
	}

	public void testCreateTaskFromExistingKeyXmlRpc() {
		init(Version.XML_RPC);
		createTaskFromExistingKey();
	}

	public void testCreateTaskFromExistingKeyTrac09() {
		init(Version.TRAC_0_9);
		createTaskFromExistingKey();
	}

	protected void createTaskFromExistingKey() {
		String id = data.tickets.get(0).getId() + "";
		ITask task = connector.createTaskFromExistingKey(repository, id);
		assertNotNull(task);
		assertEquals(TracTask.class, task.getClass());
		assertTrue(task.getDescription().contains("summary1"));
		assertEquals(repository.getUrl() + ITracClient.TICKET_URL + id, task.getUrl());

		task = connector.createTaskFromExistingKey(repository, "does not exist");
		assertNull(task);

		task = connector.createTaskFromExistingKey(repository, Integer.MAX_VALUE + "");
		assertNull(task);
	}

	public void testClientManagerChangeTaskRepositorySettings() throws MalformedURLException {
		init(Version.TRAC_0_9);
		ITracClient client = connector.getClientManager().getRepository(repository);
		assertEquals(Version.TRAC_0_9, client.getVersion());

		EditRepositoryWizard wizard = new EditRepositoryWizard(repository);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();

		((TracRepositorySettingsPage) wizard.getSettingsPage()).setTracVersion(Version.XML_RPC);
		assertTrue(wizard.performFinish());

		client = connector.getClientManager().getRepository(repository);
		assertEquals(Version.XML_RPC, client.getVersion());
	}

	public void testPerformQueryXmlRpc() {
		init(Version.XML_RPC);
		performQuery();
	}

	public void testPerformQueryTrac09() {
		init(Version.TRAC_0_9);
		performQuery();
	}

	protected void performQuery() {
		TracSearch search = new TracSearch();
		search.addFilter("milestone", "m1");
		search.addFilter("milestone", "m2");
		search.setOrderBy("id");

		String queryUrl = Constants.TEST_REPOSITORY1_URL + ITracClient.QUERY_URL + search.toUrl();
		TracRepositoryQuery query = new TracRepositoryQuery(Constants.TEST_REPOSITORY1_URL, queryUrl, "description",
				tasklist);

		MultiStatus queryStatus = new MultiStatus(MylarTracPlugin.PLUGIN_ID, IStatus.OK, "Query result", null);
		List<AbstractQueryHit> result = connector.performQuery(query, new NullProgressMonitor(), queryStatus);

		assertTrue(queryStatus.isOK());
		assertEquals(3, result.size());
		assertEquals(data.tickets.get(0).getId() + "", result.get(0).getId());
		assertEquals(data.tickets.get(1).getId() + "", result.get(1).getId());
		assertEquals(data.tickets.get(2).getId() + "", result.get(2).getId());
	}

	public void testUpdateTaskDetails() throws InvalidTicketException {
		TracTicket ticket = new TracTicket(123);
		ticket.putBuiltinValue(Key.DESCRIPTION, "mydescription");
		ticket.putBuiltinValue(Key.PRIORITY, "mypriority");
		ticket.putBuiltinValue(Key.SUMMARY, "mysummary");
		ticket.putBuiltinValue(Key.TYPE, "mytype");

		TracTask task = new TracTask("", "", true);
		TracRepositoryConnector.updateTaskDetails(Constants.TEST_REPOSITORY1_URL, task, ticket, false);

		assertEquals(Constants.TEST_REPOSITORY1_URL + ITracClient.TICKET_URL + "123", task.getUrl());
		assertEquals("123: mysummary", task.getDescription());
		assertEquals("P3", task.getPriority());
		assertEquals("mytype", task.getTaskType());
	}

	public void testUpdateTaskDetailsSummaryOnly() throws InvalidTicketException {
		TracTicket ticket = new TracTicket(456);
		ticket.putBuiltinValue(Key.SUMMARY, "mysummary");

		TracTask task = new TracTask("", "", true);
		TracRepositoryConnector.updateTaskDetails(Constants.TEST_REPOSITORY1_URL, task, ticket, false);

		assertEquals(Constants.TEST_REPOSITORY1_URL + ITracClient.TICKET_URL + "456", task.getUrl());
		assertEquals("456: mysummary", task.getDescription());
		assertEquals("P3", task.getPriority());
		assertEquals(null, task.getTaskType());
	}

}