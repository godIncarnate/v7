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
package uk.co.q3c.v7.base.config;

import static com.google.common.base.Preconditions.*;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

/**
 * A base class to define configuration files to be loaded into a {@link InheritingConfiguration} (for example
 * {@link ApplicationConfiguration}.
 * <p>
 * An integer index is used to specify the order in which the files are assess (see {@link InheritingConfiguration} for
 * an explanation)
 * <p>
 * You can use multiple modules based on this class (or create your own to populate an equivalent MapBinder) and Guice
 * will merge the map binders together. It is up to the developer to ensure that indexes are unique (but do not need to
 * bee contiguous).
 * <p>
 * Alternatively, it may be easier to use just one module and specify the files all in one place.
 * 
 * @author David Sowerby
 * 
 */
public abstract class ConfigurationModuleBase extends AbstractModule {
	private MapBinder<Integer, IniFileConfig> iniFileConfigs;

	@Override
	protected void configure() {
		iniFileConfigs = MapBinder.newMapBinder(binder(), Integer.class, IniFileConfig.class);
		bindConfigs();
	}

	/**
	 * Override this with calls to {@link #addConfig(String, int, boolean)} to specify the configuration files to use.
	 */
	protected abstract void bindConfigs();

	/**
	 * Adds an ini file configuration at the specified index. A config will override properties with the same key from a
	 * config at a lower index.
	 * 
	 * @see InheritingConfiguration
	 * @param filename
	 * @param index
	 * @param optional
	 */
	protected void addConfig(String filename, int index, boolean optional) {
		checkNotNull(filename);
		IniFileConfig ifc = new IniFileConfig(filename, optional);
		iniFileConfigs.addBinding(new Integer(index)).toInstance(ifc);
	}

}
