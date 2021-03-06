/*******************************************************************************
 * Copyright (c) 2013 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.bugzilla.rest.core;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpResponse;
import org.eclipse.mylyn.internal.bugzilla.rest.core.response.data.ErrorResponse;

import com.google.gson.reflect.TypeToken;

public class BugzillaRestValidateRequest extends BugzillaRestAuthenticatedGetRequest<ErrorResponse> {

	public BugzillaRestValidateRequest(BugzillaRestHttpClient client) {
		super(client, "", new TypeToken<ErrorResponse>() {
		});
	}

	@Override
	protected void doValidate(CommonHttpResponse response, IOperationMonitor monitor) throws IOException,
	BugzillaRestException {
		// since 4.5.5 we get an HttpStatus.SC_NOT_FOUND instead of an HttpStatus.SC_BAD_REQUEST
		validate(response, response.getStatusCode() == HttpStatus.SC_NOT_FOUND
				? HttpStatus.SC_NOT_FOUND
						: HttpStatus.SC_BAD_REQUEST, monitor);
	}

}
