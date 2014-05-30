/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.ibm.sample.wcm.healthcheck;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.workplace.wcm.api.DocumentLibrary;
import com.ibm.workplace.wcm.api.LibraryTextComponent;
import com.ibm.workplace.wcm.api.Version;
import com.ibm.workplace.wcm.api.VersionCatalog;
import com.ibm.workplace.wcm.api.VirtualPortalContext;
import com.ibm.workplace.wcm.api.VirtualPortalScopedAction;
import com.ibm.workplace.wcm.api.WCM_API;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.exceptions.AuthorizationException;
import com.ibm.workplace.wcm.api.exceptions.DocumentLockedException;
import com.ibm.workplace.wcm.api.exceptions.DocumentNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.DocumentSaveException;
import com.ibm.workplace.wcm.api.exceptions.DuplicateChildException;
import com.ibm.workplace.wcm.api.exceptions.OperationFailedException;
import com.ibm.workplace.wcm.api.exceptions.QueryServiceException;
import com.ibm.workplace.wcm.api.exceptions.ServiceNotAvailableException;
import com.ibm.workplace.wcm.api.exceptions.VersioningException;
import com.ibm.workplace.wcm.api.exceptions.VirtualPortalNotFoundException;
import com.ibm.workplace.wcm.api.exceptions.WCMException;
import com.ibm.workplace.wcm.api.query.Query;
import com.ibm.workplace.wcm.api.query.QueryService;
import com.ibm.workplace.wcm.api.query.ResultIterator;
import com.ibm.workplace.wcm.api.query.Selectors;

public class HealthContent {
	private static final String CLASS_NAME = HealthContent.class.getName();
	private static final Logger logger = Logger.getLogger(CLASS_NAME);
	private String libraryName;
	private String healthItemName;
	private Workspace roWorkspace;
	private Workspace rwWorkspace;
	private VirtualPortalContext vpContext;
	private String vpPath;

	public HealthContent(String library, String healthItem, String vpPath) {
		this.libraryName = library;
		this.healthItemName = healthItem;
		this.vpPath = vpPath;
		this.setVpContext(vpPath);
	}

	/**
	 * Get the current text in the health item. The text should be a time stamp but there
	 * is no validation done to assure that.
	 * @return the text from the item
	 */
	public String check() {
		final String METHOD = "check";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		String rc;
		if (vpContext != null) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.logp(Level.FINEST, CLASS_NAME, METHOD, "using vp context " + vpPath);
			}
			try {
				final String[] vprc = new String[1];
				WCM_API.getRepository().executeInVP(vpContext, new VirtualPortalScopedAction() {

					@Override
					public void run() throws WCMException {
						vprc[0] = scopedCheck();
					}
				});
				rc = vprc[0];
			} catch (WCMException e) {
				rc = null;
				logger.warning("Health Check could not complete " + e.getMessage());
			}
		}
		else {
			rc = scopedCheck();
		}

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD, rc);
		return rc;
	}

	/**
	 * Implement the "check" function in a vp scope
	 * @return
	 */
	private String scopedCheck() {
		String timestamp = null;
		final String METHOD = "scopedCheck";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		try {
			roWorkspace = WCM_API.getRepository().getSystemWorkspace();

			LibraryTextComponent text = getHealthItem();

			if (text != null) {
				timestamp = text.getText();
				if (timestamp.trim().isEmpty()) {
					timestamp = "Health Check Item found but it is empty";
				}
			}
			else {
				timestamp = "No Timestamp found";
			}

			WCM_API.getRepository().endWorkspace();
		} catch (ServiceNotAvailableException e) {
			logger.warning("Health Check could not get timestamp " + e.getMessage());
		} catch (OperationFailedException e) {
			logger.warning("Health Check could not get timestamp " + e.getMessage());
		}

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD, timestamp);

		return timestamp;
	}

	/**
	 * Update the health check text item with the current date and time
	 * @throws OperationFailedException
	 * @throws ServiceNotAvailableException
	 */
	public void update() {
		final String METHOD = "update";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		if (vpContext != null) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.logp(Level.FINEST, CLASS_NAME, METHOD, "using vp context " + vpPath);
			}
			try {
				WCM_API.getRepository().executeInVP(vpContext, new VirtualPortalScopedAction() {

					@Override
					public void run() throws WCMException {
						scopedUpdate();
					}
				});
			} catch (WCMException e) {
				e.printStackTrace();
			}
		}
		else {
			scopedUpdate();
		}

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD);
	}

	/**
	 * Implement the "update" function in a vp scope
	 */
	private void scopedUpdate() {
		final String METHOD = "scopedUpdate";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		try {
			roWorkspace = WCM_API.getRepository().getSystemWorkspace();

			LibraryTextComponent text = getHealthItem();

			if (text != null) {
				String timestamp = text.getText();

				if (logger.isLoggable(Level.FINEST)) {
					logger.logp(Level.FINEST, CLASS_NAME, METHOD, "current time stamp is " + timestamp);
				}

					Date now = new Date();
					String updatedTimestamp = DateFormat.getInstance().format(now);
					text.setText(updatedTimestamp);

					if (logger.isLoggable(Level.FINEST)) {
						logger.logp(Level.FINEST, CLASS_NAME, METHOD, "updated time stamp is " + updatedTimestamp);
					}

					// Save the item
					roWorkspace.save(text);
			}

			WCM_API.getRepository().endWorkspace();
		} catch (ServiceNotAvailableException e) {
			logger.warning("Health Check update could not complete " + e.getMessage());
		} catch (OperationFailedException e) {
			logger.warning("Health Check update could not complete " + e.getMessage());
		} catch (DocumentSaveException e) {
			logger.warning("Health Check update could not complete " + e.getMessage());
		} catch (AuthorizationException e) {
			logger.warning("Health Check update could not complete " + e.getMessage());
		} catch (DuplicateChildException e) {
			logger.warning("Health Check update could not complete " + e.getMessage());
		}

		// remove any versions
		removeVersions();

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD);
	}

	/**
	 * Return the REST url for the item. The "wps"prefix is assumed here. If the prefix on
	 * the server is different the returned url will need to be adjusted.
	 * @return REST url for the health check item
	 */
	public String genRestUrl() {
		final String METHOD = "genRestUrl";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		String rc;
		if (vpContext != null) {
			try {
				final String[] vprc = new String[1];
				WCM_API.getRepository().executeInVP(vpContext, new VirtualPortalScopedAction() {

					@Override
					public void run() throws WCMException {
						vprc[0] = scopedGenRestUrl();
					}
				});
				rc = vprc[0];
			} catch (WCMException e) {
				rc = null;
				logger.warning("Health Check url generation could not complete " + e.getMessage());
			}
		}
		else {
			rc = scopedGenRestUrl();
		}
		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD, rc);
		return rc;
	}

	/**
	 * Implement the "genRestUrl" function in a vp scope
	 * @return url for the health check item
	 */
	private String scopedGenRestUrl() {
		final String METHOD = "scopedGenRestUrl";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		StringBuffer url = new StringBuffer();

		try {
			roWorkspace = WCM_API.getRepository().getSystemWorkspace();

			LibraryTextComponent text = getHealthItem();

			if (text != null) {
				url.append("/wps/mycontenthandler/");
				if (vpPath != null && !vpPath.isEmpty()) {
					url.append(vpPath);
					url.append("/");
				}
				url.append("!ut/p/wcmrest/LibraryTextComponent/");
				url.append(text.getId().getId());
			}
			else {
				url.append("UNKNOWN");
			}

			WCM_API.getRepository().endWorkspace();
		} catch (ServiceNotAvailableException e) {
			logger.warning("Health Check could not get REST url" + e.getMessage());
		} catch (OperationFailedException e) {
			logger.warning("Health Check could not get REST url" + e.getMessage());
		}

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD, url.toString());
		return url.toString();
	}

	/**
	 * Find the health check item. The item must be a Text component.
	 * @return the text component
	 */
	private LibraryTextComponent getHealthItem() {
		if (rwWorkspace != null)
			return getHealthItem(rwWorkspace);
		else
			return getHealthItem(roWorkspace);
	}

	/**
	 * Use the given workspace to find the health check item.
	 * @param workspace
	 * @return
	 */
	private LibraryTextComponent getHealthItem(Workspace workspace) {
		final String METHOD = "getHealthItem";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);
		LibraryTextComponent item = null;

		try {
			if (logger.isLoggable(Level.FINEST)) {
				logger.logp(Level.FINEST, CLASS_NAME, METHOD, "searching for " + healthItemName + " in " + libraryName);
			}

			DocumentLibrary library = findDocumentLibrary(libraryName, workspace);

			if (library != null) {
				QueryService queryService = workspace.getQueryService();
				Query query = queryService.createQuery(LibraryTextComponent.class);
				query.addSelector(Selectors.nameEquals(healthItemName));
				query.addSelector(Selectors.libraryEquals(library));
				@SuppressWarnings("unchecked")
				ResultIterator<LibraryTextComponent> textComps = queryService.execute(query);

				if (textComps.hasNext()) {
					if (logger.isLoggable(Level.FINEST)) {
						logger.logp(Level.FINEST, CLASS_NAME, METHOD, "found text item to update " + healthItemName);
					}
					item = textComps.next();
				}
				else if (logger.isLoggable(Level.WARNING)) {
					logger.warning("could not find Health Check item " + healthItemName);
					logger.logp(Level.FINEST, CLASS_NAME, METHOD, "user is: " + workspace.getUserProfile().getDistinguishedName());
				}
			}
		} catch (QueryServiceException e) {
			logger.warning("Health Check could not complete " + e.getMessage());
		}

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD, item);
		return item;
	}

	/**
	 * @param libName
	 * @throws WCMException
	 */
	private DocumentLibrary findDocumentLibrary(final String libName, final Workspace wksp) {
		final String METHOD = "findDocumentLibrary";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		DocumentLibrary hclib = wksp.getDocumentLibrary(libraryName);

		if (logger.isLoggable(Level.FINEST) && (hclib != null)) {
			logger.logp(Level.FINEST, CLASS_NAME, METHOD, "found library " + hclib.getName());
		}

		// issue warning if the library is not found
		if (hclib == null) {
			logger.warning("Health Check update could not find library " + libraryName);
		}

		// print more debug information if we do not find the library
		if (logger.isLoggable(Level.FINEST) && (hclib == null)) {
			logger.logp(Level.FINEST, CLASS_NAME, METHOD, "user is: " + wksp.getUserProfile().getDistinguishedName());
			Iterator<DocumentLibrary> iterator = wksp.getDocumentLibraries();
			while (iterator.hasNext()) {
				DocumentLibrary lib = iterator.next();
				logger.logp(Level.FINEST, CLASS_NAME, METHOD, "found this library " + lib.getName());
			}
		}

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD);
		return hclib;
	}

	/**
	 * @throws
	 */
	private void setVpContext(String vpPath) {
		try {
			if (vpPath != null && !vpPath.isEmpty()) {
				vpContext = WCM_API.getRepository().generateVPContextFromContextPath(vpPath);
			}
		} catch (VirtualPortalNotFoundException e) {
			logger.severe("Health Check error " + e.getMessage());
		}
	}

	/**
	 * Remove all the versions from the health check item. This is important since the
	 * item is expected to be updated frequently. This method requires privileges on the item and
	 * needs a logged in user to execute.
	 */
	private void removeVersions() {
		final String METHOD = "removeVersions";
		if (logger.isLoggable(Level.FINEST))
			logger.entering(CLASS_NAME, METHOD);

		try {
			rwWorkspace = WCM_API.getRepository().getWorkspace();

			LibraryTextComponent item = getHealthItem();

			if (item != null) {
				// remove any previous versions
				VersionCatalog versionCatalog = item.getVersionCatalog();
				Iterator<Version> versions = versionCatalog.all();
				while (versions.hasNext()) {
					Version version = versions.next();
					if (logger.isLoggable(Level.FINEST)) {
						logger.logp(Level.FINEST, CLASS_NAME, METHOD, "deleting version " + DateFormat.getInstance().format(version.date()));
					}
					try {
						versionCatalog.remove(version);
					} catch (VersioningException e) {
						// No error message needed here.
						if (logger.isLoggable(Level.FINEST)) {
							logger.logp(Level.FINEST, CLASS_NAME, METHOD, "deleting version " + e.getMessage());
						}
					} catch (DocumentLockedException e) {
						if (logger.isLoggable(Level.FINEST)) {
							logger.logp(Level.FINEST, CLASS_NAME, METHOD, "Docuent locked. Could not delete version.");
						}
					} catch (AuthorizationException e) {
						if (logger.isLoggable(Level.FINEST)) {
							logger.logp(Level.FINEST, CLASS_NAME, METHOD, "User not authorized. Could not delete version.");
						}
					} catch (DocumentNotFoundException e) {
						if (logger.isLoggable(Level.FINEST)) {
							logger.logp(Level.FINEST, CLASS_NAME, METHOD, "Docuent not found. Could not delete version.");
						}
					}
				}
			}

			WCM_API.getRepository().endWorkspace();
		} catch (ServiceNotAvailableException e) {
			logger.warning("Health Check version removal could not complete " + e.getMessage());
		} catch (OperationFailedException e) {
			logger.warning("Health Check version removal could not complete " + e.getMessage());
		}

		if (logger.isLoggable(Level.FINEST))
			logger.exiting(CLASS_NAME, METHOD);
	}
}
