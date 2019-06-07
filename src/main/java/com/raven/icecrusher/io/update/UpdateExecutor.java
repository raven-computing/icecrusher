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

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Interface for carrying out platform specific update procedures.<br>
 * Use <code>ExecutorFactory.getUpdateExecutor()</code> to get hold of
 * an UpdateExecutor implementation suitable for the underlying 
 * platform/operating system in use.
 *
 */
public interface UpdateExecutor {
	
	/**
	 * Downloads the update package for this application
	 * 
	 * @param indicator The progress indicator which gets refreshed during the 
	 *                  download process for visual feedback to the user
	 * @param label The label for relative progress messages which gets refreshed 
	 *              during the download process for visual feedback to the user
	 */
	public void downloadPackage(ProgressIndicator indicator, Label label);
	
	/**
	 * Verifies the downloaded package integrity by computing a checksum of the data 
	 * downloaded and comparing it to the checksum distributed by the UpdateInfo object
	 * in use
	 */
	public void verifyDownload();
	
	/**
	 * Extracts the downloaded update package to the platform specific temporary 
	 * update location
	 */
	public void extractPackage();
	
	/**
	 * Sets up the platform specific update instruction script and copies it to the 
	 * temporary update location. If the update instructions shipped with this application
	 * are out of date, polls an updated version over the network and copies that instead
	 */
	public void setupInstructions();
	
	/**
	 * Performs final update tasks and executes the platform specific update instructions 
	 * in a seperate process. The entire application will shut itself down when this method 
	 * is called
	 */
	public void doUpdate();
	
	/**
	 * Cancels the current update process, if applicable
	 */
	public void cancel();
}
