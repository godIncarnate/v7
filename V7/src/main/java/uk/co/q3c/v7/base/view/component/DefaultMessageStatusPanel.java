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
package uk.co.q3c.v7.base.view.component;

import com.google.inject.Inject;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class DefaultMessageStatusPanel extends Panel implements MessageStatusPanel {

	private Label label;
	private HorizontalLayout layout;

	@Inject
	protected DefaultMessageStatusPanel() {
		super();
		build();
	}

	private void build() {
		layout = new HorizontalLayout();
		label = new Label("Message bar");
		layout.addComponent(label);
		this.setContent(layout);

	}

}
