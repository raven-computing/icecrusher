/* 
 * Copyright (C) 2020 Raven Computing
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

package com.raven.icecrusher.net;

/**
 * Specifies all resources available over the network.
 *
 */
public enum ResourceLocator {

    LATEST_VERSION("GET", "https://www.raven-computing.com/products/icecrusher/releases/latest/release.xml"),
    SEND_FEEDBACK("POST", "https://www.raven-computing.com/api/icecrusher/feedbacks"),
    UPDATE_PACKAGE_LINUX_APP("GET", "https://www.raven-computing.com/assets/file/products/icecrusher/update_linux_app.zip"),
    UPDATE_PACKAGE_LINUX_FULL("GET", "https://www.raven-computing.com/assets/file/products/icecrusher/update_linux_full.zip"),
    UPDATE_PACKAGE_WINDOWS_APP("GET", "https://www.raven-computing.com/assets/file/products/icecrusher/update_windows_app.zip"),
    UPDATE_PACKAGE_WINDOWS_FULL("GET", "https://www.raven-computing.com/assets/file/products/icecrusher/update_windows_full.zip"),
    UPDATE_INSTRUCT_LINUX("GET", "https://www.raven-computing.com/assets/file/products/icecrusher/update.sh"),
    UPDATE_INSTRUCT_WINDOWS("GET", "https://www.raven-computing.com/assets/file/products/icecrusher/update.bat"),
    RELEASE_NOTES("GET", "https://www.raven-computing.com/products/icecrusher/release-notes.html"),
    UPDATE_SHOW_IN_BROWSER("GET", "https://github.com/raven-computing/icecrusher");

    ResourceLocator(final String method, final String url){
        this.method = method;
        this.url = url;
    }

    private String method;
    private String url;

    public String getMethod(){
        return this.method;
    }

    public String getUrl(){
        return this.url;
    }

}
