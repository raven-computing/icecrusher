/* 
 * Copyright (C) 2019 Raven Computing
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raven.icecrusher.io.update;

/**
 * Provides concrete implementations of the <code>UpdateExecutor</code> interface.<br>
 * Call <code>ExecutorFactory.getUpdateExecutor()</code> to get an UpdateExecutor 
 * implementation suitable for the underlying platform/operating system in use.
 *
 */
public class ExecutorFactory {

	private ExecutorFactory(){ }
	
	/**
	 * Gets an UpdateExecutor for performing application updates
	 * 
	 * @param updateInfo The <code>UpdateInfo</code> object holding the necessary update 
	 *                   data polled over the network
	 * @param updateRoutine The <code>UpdateRoutine</code> to be used by the returned UpdateExecutor
	 * @return An <code>UpdateExecutor</code> for the underlying platform
	 */
	protected static UpdateExecutor getUpdateExecutor(final UpdateInfo updateInfo, final UpdateRoutine updateRoutine){
		if((updateInfo == null) || (updateRoutine == null)){
			throw new IllegalArgumentException("Update arguments must not be null");
		}
		final String os = System.getProperty("os.name");
		if((os != null) && (!os.isEmpty())){
			final String name = os.toLowerCase();
			if(name.contains("linux")){
				return new LinuxUpdateExecutor(updateInfo, updateRoutine);
			}else if(name.contains("windows") || name.contains("win")){
				return new WindowsUpdateExecutor(updateInfo, updateRoutine);
			}
		}
		return null;
	}

}
