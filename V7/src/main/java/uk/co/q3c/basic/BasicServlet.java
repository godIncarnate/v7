package uk.co.q3c.basic;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;

@Singleton
public class BasicServlet extends VaadinServlet implements SessionInitListener {

	private static final long serialVersionUID = -3701290344778297217L;
	/**
	 * Cannot use constructor inject. Container expects servlet to have no-arg public constructor
	 */
	@Inject
	private BasicProvider basicProvider;

	@Override
	protected void servletInitialized() {
		getService().addSessionInitListener(this);
	}

	@Override
	public void sessionInit(SessionInitEvent event) throws ServiceException {
		event.getSession().addUIProvider(basicProvider);
	}

}