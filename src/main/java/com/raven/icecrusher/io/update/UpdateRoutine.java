/* 
 * Copyright (C) 2021 Raven Computing
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
 * Interface defining interim stages for update procedures.<br>
 * An <code>UpdateExecutor</code> will call the methods defined in this interface
 * at the appropriate time according to the activation cycle.
 *
 */
public interface UpdateRoutine {

    /**
     * Called when the underlying UpdateExecutor has finished with the update 
     * package download
     * 
     * @param success Indicates whether the update package download step was successful
     */
    public void onPackageDownloaded(boolean success);

    /**
     * Called when the underlying UpdateExecutor has verified the downloaded update 
     * package
     * 
     * @param isValid Indicates whether the downloaded update package is valid and its 
     *                integrity could be confirmed
     */
    public void onDownloadVerified(boolean isValid);

    /**
     * Called when the underlying UpdateExecutor has extracted the downloaded update 
     * package to the temporary update location
     * 
     * @param success Indicates whether the update package extraction was successful
     */
    public void onExtracted(boolean success);

    /**
     * Called when the underlying UpdateExecutor has finished with the update 
     * instruction setup
     * 
     * @param success Indicates whether the update instruction was successfully set up
     */
    public void onInstructionsProvided(boolean success);

}
