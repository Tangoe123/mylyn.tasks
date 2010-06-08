/*******************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.notifications;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotification;
import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotificationPopup;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.TaskScalingHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

/**
 * @author Rob Elves
 * @author Mik Kersten
 */
public class TaskListNotificationPopup extends AbstractNotificationPopup {

	private static final String NOTIFICATIONS_HIDDEN = Messages.TaskListNotificationPopup_more;

	private static final int NUM_NOTIFICATIONS_TO_DISPLAY = 4;

	private List<AbstractNotification> notifications;

	public TaskListNotificationPopup(Shell parent) {
		super(parent.getDisplay());
	}

	public void setContents(List<AbstractNotification> notifications) {
		this.notifications = notifications;
	}

	public List<AbstractNotification> getNotifications() {
		return new ArrayList<AbstractNotification>(notifications);
	}

	@Override
	protected void createTitleArea(Composite parent) {
		super.createTitleArea(parent);
	}

	@Override
	protected Color getTitleForeground() {
		return TasksUiPlugin.getDefault().getFormColors(Display.getDefault()).getColor(IFormColors.TITLE);

	}

	@Override
	protected void createContentArea(Composite parent) {
		int count = 0;
		for (final AbstractNotification notification : notifications) {
			Composite notificationComposite = new Composite(parent, SWT.NO_FOCUS);
			GridLayout gridLayout = new GridLayout(2, false);
			GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(notificationComposite);
			notificationComposite.setLayout(gridLayout);
			notificationComposite.setBackground(parent.getBackground());

			if (count < NUM_NOTIFICATIONS_TO_DISPLAY) {
				final Label notificationLabelIcon = new Label(notificationComposite, SWT.NO_FOCUS);
				notificationLabelIcon.setBackground(parent.getBackground());
				notificationLabelIcon.setImage(notification.getNotificationKindImage());
				if (!(notification instanceof TaskListNotificationReminder)) {
					final AbstractTask task = (AbstractTask) notification.getAdapter(AbstractTask.class);
					if (task != null) {
						notificationLabelIcon.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseUp(MouseEvent e) {
								TasksUiPlugin.getTaskDataManager().setTaskRead(task, true);
								notificationLabelIcon.setImage(null);
								notificationLabelIcon.setToolTipText(null);
							}
						});
						notificationLabelIcon.setToolTipText(Messages.TaskListNotificationPopup_Mark_Task_Read);
					}
				}

				final TaskScalingHyperlink itemLink = new TaskScalingHyperlink(notificationComposite, SWT.BEGINNING | SWT.NO_FOCUS);
				GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(itemLink);

				itemLink.setText(CommonUiUtil.toLabel(notification.getLabel()));
				itemLink.setImage(notification.getNotificationImage());
				itemLink.setBackground(parent.getBackground());
				itemLink.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						notification.open();
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if (window != null) {
							Shell windowShell = window.getShell();
							if (windowShell != null) {
								if (windowShell.getMinimized()) {
									windowShell.setMinimized(false);
								}

								windowShell.open();
								windowShell.forceActive();
							}
						}
					}
				});

				String descriptionText = null;
				if (notification.getDescription() != null) {
					descriptionText = notification.getDescription();
				}
				if (descriptionText != null && !descriptionText.trim().equals("")) { //$NON-NLS-1$
					Label descriptionLabel = new Label(notificationComposite, SWT.NO_FOCUS);
					descriptionLabel.setText(CommonUiUtil.toLabel(descriptionText));
					descriptionLabel.setBackground(parent.getBackground());
					GridDataFactory.fillDefaults()
							.span(2, SWT.DEFAULT)
							.grab(true, false)
							.align(SWT.FILL, SWT.TOP)
							.applyTo(descriptionLabel);
				}
			} else {
				int numNotificationsRemain = notifications.size() - count;
				TaskScalingHyperlink remainingHyperlink = new TaskScalingHyperlink(notificationComposite, SWT.NO_FOCUS);
				remainingHyperlink.setBackground(parent.getBackground());

				remainingHyperlink.setText(numNotificationsRemain + " " + NOTIFICATIONS_HIDDEN); //$NON-NLS-1$
				GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(remainingHyperlink);
				remainingHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						TasksUiUtil.openTasksViewInActivePerspective().setFocus();
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if (window != null) {
							Shell windowShell = window.getShell();
							if (windowShell != null) {
								windowShell.setMaximized(true);
								windowShell.open();
							}
						}
					}
				});
				break;
			}
			count++;
		}
	}

}
