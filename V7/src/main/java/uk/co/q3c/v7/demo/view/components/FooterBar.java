package uk.co.q3c.v7.demo.view.components;

import javax.inject.Inject;

import uk.co.q3c.v7.base.guice.uiscope.UIScoped;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

@UIScoped
public class FooterBar extends HorizontalLayout {

	private final Label infoLabel;
	private final Label msgLabel;

	@Inject
	protected FooterBar() {
		super();
		setSpacing(true);
		infoLabel = new Label();
		infoLabel
				.setValue("  Because the footer bar is @UIScoped, it can be injected into any component to show user messages.  Last user message:   ");
		msgLabel = new Label();
		// msgLabel.addStyleName(ChameleonTheme.LABEL_H4); chameleon styles
		// broken in beta 11
		Label padLabel = new Label(" ");
		padLabel.setWidth("20px");
		Label padLabel2 = new Label(" ");
		padLabel2.setWidth("20px");

		addComponent(infoLabel);
		// addComponent(padLabel);
		addComponent(msgLabel);
	}

	public void setUserMessage(String msg) {
		msgLabel.setValue(msg);
	}

}
