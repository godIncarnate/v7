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
package uk.co.q3c.v7.base.services;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.jodatime.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.co.q3c.v7.base.services.Service.Status;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mycila.testing.junit.MycilaJunitRunner;
import com.mycila.testing.plugin.guice.GuiceContext;

/**
 * Combined testing for all the Service components - almost functional testing
 * 
 * Dependency map is:
 * 
 * d - a<br>
 * - - a1<br>
 * - - b<br>
 * - - c-a<br>
 * - - --b<br>
 * <br>
 * <br>
 * 
 * @author David Sowerby
 * 
 */
@RunWith(MycilaJunitRunner.class)
@GuiceContext({ ServicesMonitorModule.class })
public class ServiceTest {
	static Status a1_startStatus = Status.STARTED;
	static Status a1_stopStatus = Status.STOPPED;
	static boolean a1_exceptionOnStart = false;
	static boolean b_exceptionOnStart = false;
	static boolean a1_exceptionOnStop = false;

	static class ChangeMonitor implements ServiceChangeListener {
		int statusChangeCount;
		List<String> dependencyChanges = new ArrayList<>();

		@Override
		public void serviceStatusChange(Service service, Status fromStatus, Status toStatus) {
			statusChangeCount++;
			String chg = service.getName() + ":" + fromStatus + ":" + toStatus;
			dependencyChanges.add(chg);
		}

	}

	static class MockService extends AbstractService {

		int startCalls;
		int stopCalls;

		@Override
		public String getName() {
			return ServiceUtils.unenhancedClass(this).getSimpleName();
		}

		@Override
		public String getDescription() {
			return getName();
		}

		@Override
		public void doStart() throws Exception {
			startCalls++;
		}

		@Override
		public void doStop() throws Exception {
			stopCalls++;
		}
	}

	/**
	 * Keep this singleton annotation - it detected a problem with listener firing becoming re-entrant, and the iterator
	 * consequently failing.
	 * 
	 * @author David Sowerby
	 * 
	 */
	@Singleton
	static class MockServiceA extends MockService {

	}

	static class MockServiceA1 extends MockService {

		@Override
		public void doStart() {
			if (a1_exceptionOnStart) {
				throw new NullPointerException("Mock exception on start");
			} else {
				startCalls++;
			}
		}

		@Override
		public void doStop() {
			if (a1_exceptionOnStop) {
				throw new NullPointerException("Mock exception on stop");
			} else {
				stopCalls++;
			}
		}

	}

	static class MockServiceB extends MockService {

		@Dependency(requiredAtStart = false)
		private final MockServiceA a;

		@Inject
		public MockServiceB(MockServiceA a) {
			super();
			this.a = a;

		}

		@Override
		public void doStart() throws Exception {
			if (b_exceptionOnStart) {
				throw new ServiceException("Service B failed");
			}
		}

	}

	static class MockServiceC extends MockService {

		@Dependency
		private final MockServiceA a;
		private final MockServiceB b;

		@Inject
		public MockServiceC(MockServiceA a, MockServiceB b) {
			super();
			this.a = a;
			this.b = b;

		}
	}

	static class MockServiceD extends MockService {

		@Dependency(stopOnStop = false)
		private final MockServiceA a;

		@Dependency(requiredAtStart = true, startOnRestart = true, stopOnStop = true)
		private final MockServiceA1 a1;

		@Dependency(requiredAtStart = false, startOnRestart = false, stopOnStop = false)
		private final MockServiceB b;
		private final MockServiceC c;

		@Inject
		public MockServiceD(MockServiceA a, MockServiceA1 a1, MockServiceB b, MockServiceC c) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
			this.a1 = a1;

		}

		@Override
		public void doStart() throws Exception {
			super.doStart();
		}

	}

	@Inject
	MockServiceD serviced;

	@Inject
	Injector injector;

	ChangeMonitor changeMonitor;

	@Inject
	ServicesMonitor monitor;

	@Before
	public void setup() {
		a1_startStatus = Status.STARTED;
		a1_stopStatus = Status.STOPPED;
		a1_exceptionOnStart = false;
		a1_exceptionOnStop = false;
		changeMonitor = new ChangeMonitor();
	}

	@Test
	public void start_without_errors() throws Exception {

		// given
		// when
		serviced.start();
		// then
		assertThat(serviced.a.isStarted()).isTrue();
		assertThat(serviced.a1.isStarted()).isTrue();
		assertThat(serviced.b.isStarted()).isFalse();
		assertThat(serviced.c.isStarted()).isFalse();
		assertThat(serviced.a.startCalls).isEqualTo(1);
		assertThat(serviced.getStatus()).isEqualTo(Status.STARTED);

	}

	@Test
	public void start_when_already_started() throws Exception {

		// given
		injector.getInstance(MockServiceA.class).start();

		// when
		serviced.start();
		// then
		assertThat(serviced.a.isStarted()).isTrue();
		assertThat(serviced.a1.isStarted()).isTrue();
		assertThat(serviced.b.isStarted()).isFalse();
		assertThat(serviced.c.isStarted()).isFalse();
		assertThat(serviced.a.startCalls).isEqualTo(1); // hasn"t been called again
		assertThat(serviced.getStatus()).isEqualTo(Status.STARTED);

		// listeners

	}

	@Test(expected = ServiceException.class)
	public void start_required_dependency_throws_exception() throws Exception {

		// given
		a1_exceptionOnStart = true;
		// when
		serviced.start();
		// then

	}

	@Test
	public void start_required_dependency_throws_exception_check_status() throws Exception {

		// given
		a1_exceptionOnStart = true;
		// when
		try {
			serviced.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// then
		assertThat(serviced.a.isStarted()).isTrue();
		assertThat(serviced.a1.isStarted()).isFalse();
		assertThat(serviced.a1.getStatus()).isEqualTo(Status.FAILED_TO_START);
		assertThat(serviced.b.isStarted()).isFalse();
		assertThat(serviced.c.isStarted()).isFalse();
		assertThat(serviced.getStatus()).isEqualTo(Status.DEPENDENCY_FAILED);
		assertThat(serviced.startCalls).isEqualTo(0);
	}

	@Test
	public void start_optional_dependency_throws_exception() throws Exception {

		// given
		b_exceptionOnStart = true;
		// when
		serviced.start();
		// then
		assertThat(serviced.a.isStarted()).isTrue();
		assertThat(serviced.a1.isStarted()).isTrue();
		assertThat(serviced.b.isStarted()).isFalse();
		assertThat(serviced.b.getStatus()).isEqualTo(Status.FAILED_TO_START);
		assertThat(serviced.c.isStarted()).isFalse();
		assertThat(serviced.a.startCalls).isEqualTo(1);
		assertThat(serviced.getStatus()).isEqualTo(Status.STARTED);

	}

	@Test
	public void startOnRestart_true() throws Exception {

		// given
		a1_exceptionOnStart = true;
		try {
			serviced.start();
		} catch (Exception e) {

		}
		a1_exceptionOnStart = false;
		// when
		serviced.a1.start();
		// then
		assertThat(serviced.a1.isStarted()).isTrue();
		assertThat(serviced.isStarted()).isTrue();
	}

	@Test
	public void startOnRestart_false() throws Exception {

		// given
		a1_exceptionOnStart = true;
		try {
			serviced.start();
		} catch (Exception e) {

		}
		a1_exceptionOnStart = false;
		// when
		serviced.a1.start();
		// then
		assertThat(serviced.a1.isStarted()).isTrue();
		assertThat(serviced.isStarted()).isTrue();
	}

	@Test
	public void stopOnStop_True() throws Exception {

		// given
		serviced.start();
		// when
		serviced.a1.stop();
		// then
		assertThat(serviced.isStarted()).isFalse();
	}

	@Test
	public void stopOnStop_False() throws Exception {

		// given
		serviced.start();
		// when
		serviced.a.stop();
		// then
		assertThat(serviced.isStarted()).isTrue();
		assertThat(serviced.b.isStarted()).isFalse();
	}

	/**
	 * With this test structure there should be registered instances of a,a1,bx2,c and d
	 * 
	 * @throws Exception
	 */
	@Test
	public void monitorHasRegisteredServices() throws Exception {

		// given
		ServicesMonitor monitor = injector.getInstance(ServicesMonitor.class);
		// when
		serviced.start();
		// then
		ImmutableList<Service> registeredServices = monitor.getRegisteredServices();
		for (Service service : registeredServices) {
			System.out.println(service.getName());
		}
		assertThat(registeredServices).containsOnly(serviced, serviced.a, serviced.b, serviced.a1, serviced.c,
				serviced.c.b);

	}

	@Test
	public void monitorLogsStatusChange() throws Exception {

		// given
		ServicesMonitor monitor = injector.getInstance(ServicesMonitor.class);
		// when
		serviced.start();
		// then
		ServiceStatus status = monitor.getServiceStatus(serviced);
		assertThat(status.getCurrentStatus()).isEqualTo(Status.STARTED);
		assertThat(status.getLastStartTime()).isNotNull().isBeforeOrEqualTo(DateTime.now());
		assertThat(status.getLastStopTime()).isNull();
		assertThat(status.getStatusChangeTime()).isNotNull().isEqualTo(status.getLastStartTime());
		assertThat(status.getPreviousStatus()).isEqualTo(Status.INITIAL);
		DateTime startTime = status.getLastStartTime();
		// when
		serviced.stop();
		// then
		status = monitor.getServiceStatus(serviced);
		assertThat(status.getCurrentStatus()).isEqualTo(Status.STOPPED);
		assertThat(status.getLastStartTime()).isNotNull().isEqualTo(startTime); // shouldn't have changed
		assertThat(status.getLastStopTime()).isNotNull().isBeforeOrEqualTo(DateTime.now()).isAfter(startTime);
		assertThat(status.getStatusChangeTime()).isNotNull().isEqualTo(status.getLastStopTime());
		assertThat(status.getPreviousStatus()).isEqualTo(Status.STARTED);
	}

	/**
	 * A dependency should have a listener automatically added by any Service using it
	 * 
	 * @throws Exception
	 */
	@Test
	public void serviceMonitorsDependencyStatusChange() throws Exception {

		// given
		serviced.start();
		// when
		serviced.a.stop();
		// then
		assertThat(monitor.getServiceStatus(serviced).getCurrentStatus()).isEqualTo(Status.STARTED);
		assertThat(monitor.getServiceStatus(serviced.a).getCurrentStatus()).isEqualTo(Status.STOPPED);
	}
}
