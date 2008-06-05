/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.planner;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.mylyn.internal.tasks.ui.views.DatePickerPanel;
import org.eclipse.mylyn.internal.tasks.ui.views.DatePickerPanel.DateSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Ken Sueda
 * @author Mik Kersten
 * @author Rob Elves
 */
public class DateSelectionDialog extends Dialog {

	private Date reminderDate = null;

	private String title = "Date Selection";

	private Calendar initialCalendar = GregorianCalendar.getInstance();

	private FormToolkit toolkit;
	
	private boolean includeTime = true;

	public DateSelectionDialog(Shell parentShell, String title) {
		this(parentShell, GregorianCalendar.getInstance(), title, true);
	}

	public DateSelectionDialog(Shell parentShell, Calendar initialDate, String title, boolean includeTime) {
		super(parentShell);
		this.includeTime = includeTime; 
		toolkit = new FormToolkit(parentShell.getDisplay());
		if (title != null) {
			this.title = title;
		}
		if (initialDate != null) {
			this.initialCalendar.setTime(initialDate.getTime());
		}
		reminderDate = initialCalendar.getTime();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(toolkit.getColors().getBackground());
		getShell().setText(title);
		DatePickerPanel datePanel = new DatePickerPanel(parent, SWT.NULL, initialCalendar, includeTime);
		datePanel.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					DateSelection dateSelection = (DateSelection) event.getSelection();
					reminderDate = dateSelection.getDate().getTime();
				}
			}
		});
		datePanel.setBackground(toolkit.getColors().getBackground());
		datePanel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		
		return datePanel;
	}

	@Override
	public boolean close() {
		toolkit.dispose();
		return super.close();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(toolkit.getColors().getBackground());
		createButton(parent, IDialogConstants.CLIENT_ID + 1, "Clear", false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (buttonId == IDialogConstants.CLIENT_ID + 1) {
			reminderDate = null;
			okPressed();
		}
	}

	public Date getDate() {
		return reminderDate;
	}
}