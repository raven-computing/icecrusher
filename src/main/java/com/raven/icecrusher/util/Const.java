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

package com.raven.icecrusher.util;

import java.text.SimpleDateFormat;

import static com.raven.icecrusher.application.Resources.*;

/**
 * This class holds various global application constants.
 *
 */
public final class Const {

    /** Indicates whether this application runs in debug mode **/
    public static final boolean DEBUG = false;

    /** The name of this application **/
    public static final String APPLICATION_NAME = property("project.name");

    /** The version of this application **/
    public static final String APPLICATION_VERSION = property("project.version");

    /** The developer of this application **/
    public static final String APPLICATION_DEVELOPER = property("project.developer");

    /** The E-Mail of this application's developer **/
    public static final String APPLICATION_DEVELOPER_EMAIL = property("project.developer.email");

    /** The copyright notice of this application **/
    public static final String APPLICATION_COPYRIGHT = property("project.copyright");

    /** The internal ID of the runtime used by this application **/
    public static final int APPLICATION_RUNTIME_ID = integerProperty("application.runtime.id");

    /** The internal ID of the update instruction script for Linux/Debian **/
    public static final int UPDATE_LINUX_INSTRUCTION_ID = integerProperty("update.linux.instruction.id");

    /** The internal ID of the update instruction script for Windows **/
    public static final int UPDATE_WINDOWS_INSTRUCTION_ID = integerProperty("update.windows.instruction.id");

    /** Resource directory for layout files **/
    public static final String DIR_LAYOUT = "/layout/";

    /** Resource directory for icons **/
    public static final String DIR_ICON = "/icon/";

    /** Resource directory for config files **/
    public static final String DIR_CONFIGS = "/config/";

    /** Resource directory for stylesheets **/
    public static final String DIR_CSS = "/css/";

    /** Resource directory for update instruction files **/
    public static final String DIR_UPDATE = "/update/";
    
    /** Resource directory for license files **/
    public static final String DIR_LICENSE = "/license/";

    public static final String BUNDLE_KEY_RELOAD_REQUIRED = "view.reload.true";
    public static final String BUNDLE_KEY_EXIT_REQUESTED = "application.exit.true";
    public static final String BUNDLE_KEY_THEME_CHANGED = "theme.changed.true";
    public static final String BUNDLE_KEY_HISTORY_LIST = "config.tabs.history";
    public static final String BUNDLE_KEY_DATAFRAME = "payload.DataFrame";
    public static final String BUNDLE_KEY_EDITORFILE = "payload.EditorFile";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_ENCODED = new SimpleDateFormat("yyyyMMdd");

    public static final String KEY_USER_HOME = "user.home";

    /** Specification in milliseconds for how long the 'new version' notification should be visible **/
    public static final long TIME_SHOW_UPDATE_NOTIFICATION = 20000;

    /** The threshold expressed in the number of rows within a DataFrame, from which to perform 
     *  DataFrame related operations concurrently **/
    public static final int DF_PARALLELISM_THRESHOLD = 500000;

}
