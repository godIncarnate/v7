/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.user;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.q3c.v7.base.guice.uiscope.UIScoped;
import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.shiro.LoginStatusListener;
import uk.co.q3c.v7.base.shiro.V7SecurityManager;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent;
import uk.co.q3c.v7.base.view.V7ViewChangeListener;
import uk.co.q3c.v7.demo.dao.DemoUsageLogDAO;
import uk.co.q3c.v7.demo.usage.DemoUsageLog;

import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;

@UIScoped
public class DemoUsage implements LoginStatusListener, V7ViewChangeListener {
	private static Logger log = LoggerFactory.getLogger(DemoUsage.class);
	private final List<String> logs = new ArrayList<>();
	private final Provider<DemoUsageLogDAO> daoPro;

	@Inject
	protected DemoUsage(V7SecurityManager securityManager, V7Navigator navigator, Provider<DemoUsageLogDAO> daoPro) {
		super();
		this.daoPro = daoPro;
		securityManager.addListener(this);
		navigator.addViewChangeListener(this);
	}

	@Override
	public void updateStatus(Subject subject) {
		if (subject.isAuthenticated()) {
			makeEntry("login");
		} else {
			makeEntry("logout");
		}

	}

	@Override
	public boolean beforeViewChange(V7ViewChangeEvent event) {
		// do nothing but don't block later listeners
		return true;
	}

	@Override
	public void afterViewChange(V7ViewChangeEvent event) {
		//
		throw new RuntimeException("not yet implemented");
	}

	private void makeEntry(String eventType) {
		DemoUsageLogDAO dao = daoPro.get();
		DemoUsageLog entry = dao.newEntity();
		entry.setDateTime(DateTime.now());
		WebBrowser browser = Page.getCurrent().getWebBrowser();
		entry.setLocaleString(browser.getLocale().toString());
		entry.setSourceIP(browser.getAddress());
		entry.setEvent(eventType);
	}

}
