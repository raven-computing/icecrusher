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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.raven.common.io.ConfigurationFile;
import com.raven.common.io.ConfigurationFileHandler;

/**
 * Handles the entire configuration and all configurable properties, as
 * well as the persisting of those configurations to the local filesystem.<br>
 * This class cannot be instantiated directly. Use <code>getInstance()</code> to
 * obtain a reference to an instance of this class.
 *
 */
public class EditorConfiguration {

    /**
     * Enum listing all sections used in the configuration file.
     *
     */
    public enum Section {

        GLOBAL("Global"),
        WINDOW("Window"),
        UPDATER("Updater"),
        PLOT("Plot");

        private String key;

        Section(String key){
            this.key = key;
        }
    }

    public static final String CONFIG_RECALL_TABS = "recall.tabs";
    public static final String CONFIG_SHOW_INDEX_COL = "show.index_col";
    public static final String CONFIG_CLEAR_AFTER_ROW_ADD = "clear.after.row_add";
    public static final String CONFIG_CONFIRM_ROW_DELETION = "confirm.row.deletion";
    public static final String CONFIG_DIALOG_ALWAYS_HOME = "dialog.always.at_home";
    public static final String CONFIG_THEME_VIEW_DARK = "dfview.theme.dark";
    public static final String CONFIG_CACHE_SESSION_SIZE = "cache.session.size";

    public static final String CONFIG_WINDOW_WIDTH = "width";
    public static final String CONFIG_WINDOW_HEIGHT = "height";
    public static final String CONFIG_WINDOW_DIALOG_DIR = "dialog.io.dir";

    public static final String CONFIG_AUTO_UPDATE_CHECK = "check.auto";
    public static final String CONFIG_LAST_UPDATE_CHECK = "check.last";
    public static final String CONFIG_UPDATE_AVAILABLE = "check.available";
    public static final String CONFIG_USE_HTTPS = "use.https";
    public static final String CONFIG_DATA_STALE_THRESHOLD = "check.stale.threshold.days";
    
    public static final String CONFIG_TITLE_POSITION = "title.position";
    public static final String CONFIG_LEGEND_POSITION = "legend.position";
    public static final String CONFIG_GRID_VISIBLE = "grid.visible";
    public static final String CONFIG_BACKGROUND_WHITE = "background.white";
    public static final String CONFIG_XYCHART_DATAPOINTS = "xychart.show_datapoints";
    public static final String CONFIG_BARCHART_MODE = "barchart.mode";
    public static final String CONFIG_PIECHART_SHOW_ABS = "piechart.show_abs";
    public static final String CONFIG_PIECHART_SHOW_PERC = "piechart.show_perc";

    /** Directory for user specific configuration files **/
    private static final String CONFIG_DIR = System.getProperty(Const.KEY_USER_HOME)
            +"/.config/"+Const.APPLICATION_NAME.toLowerCase()+"/";

    private static final String TEMPLATE_FILE = Const.DIR_CONFIGS+"global_template.config";
    private static final String CONFIG_FILE = "editor.config";
    private static final String HISTORY_FILE = "recall";

    private static EditorConfiguration instance;

    private ConfigurationFileHandler fileHandler;
    private ConfigurationFile config;
    private ConfigurationFile recall;
    private History history;
    private boolean configChanged;

    private EditorConfiguration(){
        final File file = new File(CONFIG_DIR+CONFIG_FILE);
        this.fileHandler = new ConfigurationFileHandler(CONFIG_DIR+CONFIG_FILE);
        try{
            if(!file.exists()){
                this.config = copyTemplate();
            }else{
                this.config = fileHandler.read();
            }
        }catch(IOException ex){
            ExceptionHandler.showDialog(ex);
        }
    }

    /**
     * Gets a reference to an <code>EditorConfiguration</code> instance
     * 
     * @return An <code>EditorConfiguration</code> object that can be used to query or 
     *         change configurations
     */
    public static EditorConfiguration getConfiguration(){
        if(instance == null){
            instance = new EditorConfiguration();
        }
        return instance;
    }

    /**
     * Returns the value of the configuration in the specified Section with the specified key
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to access
     * @return The value of the configuration with the specified key, or null if the specified
     *         section does not hold a configuration with the specified key
     */
    public String valueOf(final Section SECTION, final String CONFIGURATION){
        return this.config.getSection(SECTION.key).valueOf(CONFIGURATION);
    }

    /**
     * Returns the value of the configuration in the specified Section with the specified value
     * directly converted to a boolean
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to access
     * @return The value of the configuration with the specified key. Might return false if the
     *         configuration accessed is not a boolean or was not found
     */
    public boolean booleanOf(final Section SECTION, final String CONFIGURATION){
        return Boolean.valueOf(config.getSection(SECTION.key).valueOf(CONFIGURATION));
    }

    /**
     * Returns the value of the configuration in the specified Section with the specified value
     * directly converted to a double
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to access
     * @return The value of the configuration with the specified key. Might return 0 (zero)
     *         if the configuration accessed is not a double or was not found
     */
    public double doubleOf(final Section SECTION, final String CONFIGURATION){
        try{
            return Double.valueOf(config.getSection(SECTION.key).valueOf(CONFIGURATION));
        }catch(NumberFormatException ex){
            return 0.0;
        }
    }
    
    /**
     * Returns the value of the configuration in the specified Section with the specified value
     * directly converted to an integer
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to access
     * @return The value of the configuration with the specified key. Might return 0 (zero)
     *         if the configuration accessed is not an integer or was not found
     */
    public int integerOf(final Section SECTION, final String CONFIGURATION){
        try{
            return Integer.valueOf(config.getSection(SECTION.key).valueOf(CONFIGURATION));
        }catch(NumberFormatException ex){
            return 0;
        }
    }
    
    /**
     * Returns the value of the configuration denoting a memory size value in the specified
     * Section with the specified value directly converted to an integer in bytes
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to access
     * @return The value of the memory size configuration (in bytes) with the specified key.
     *         Might return 0 (zero) if the configuration accessed is not an integer, was not
     *         found or the entry was incorrectly formatted
     */
    public int memoryOf(final Section SECTION, final String CONFIGURATION){
        String val = config.getSection(SECTION.key).valueOf(CONFIGURATION);
        if((val == null) || val.isEmpty()){
            return 0;
        }
        try{
            val = val.toUpperCase();
            int factor = 1;
            if(val.endsWith("KB")){
                factor = 1000;
                val = val.substring(0, val.length()-2);
            }else if(val.endsWith("MB")){
                factor = 1000000;
                val = val.substring(0, val.length()-2);
            }
            return Integer.valueOf(val) * factor;
        }catch(Exception ex){
            return 0;
        }
    }

    /**
     * Sets the value of the configuration in the specified Section with the specified key to the 
     * specified value
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to set
     * @param value The new value of the configuration to set
     */
    public void set(final Section SECTION, final String CONFIGURATION, final String value){
        configChanged = true;
        this.config.getSection(SECTION.key).set(CONFIGURATION, value);
    }

    /**
     * Sets the value of the configuration in the specified Section with the specified key to the 
     * specified boolean value
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to set
     * @param value The new value of the configuration to set
     */
    public void set(final Section SECTION, final String CONFIGURATION, final boolean value){
        configChanged = true;
        this.config.getSection(SECTION.key).set(CONFIGURATION, String.valueOf(value));
    }

    /**
     * Sets the value of the configuration in the specified Section with the specified key to the 
     * specified double value
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to set
     * @param value The new value of the configuration to set
     */
    public void set(final Section SECTION, final String CONFIGURATION, final double value){
        configChanged = true;
        this.config.getSection(SECTION.key).set(CONFIGURATION, String.valueOf(value));
    }
    
    /**
     * Sets the value of the configuration in the specified Section with the specified key to the 
     * specified integer value
     * 
     * @param SECTION The <code>Section</code> of the configuration to access
     * @param CONFIGURATION The key of the configuration to set
     * @param value The new value of the configuration to set
     */
    public void set(final Section SECTION, final String CONFIGURATION, final int value){
        configChanged = true;
        this.config.getSection(SECTION.key).set(CONFIGURATION, String.valueOf(value));
    }

    /**
     * Gets the history object associated with the recall file from the filesystem
     * 
     * @return The <code>History</code> object of the recall file, or null if the file
     *         does not exist or an error occurred
     */
    public History getHistory(){
        if(recall == null){
            try{
                final File file = new File(CONFIG_DIR+HISTORY_FILE);
                if(file.exists()){
                    this.recall = readHistoryFile();
                }else{
                    return null;
                }
            }catch(IOException ex){
                ExceptionHandler.handle(ex);
                return null;
            }
        }
        if(history == null){
            this.history = History.fromFile(recall);
        }
        return this.history;
    }

    /**
     * Sets the history of the EditorConfiguration to the specified <code>History</code>
     * 
     * @param history The <code>History</code> object to use
     */
    public void setHistory(final History history){
        this.history = history;
        this.recall = history.getRecallFile();
    }

    /**
     * Persists the configuration as is to the local filesystem
     */
    public void persistConfiguration(){
        if(configChanged){
            try{
                this.fileHandler.write(config);
                configChanged = false;
            }catch(IOException ex){
                ExceptionHandler.handle(ex);
            }
        }
    }

    /**
     * Persists the recall file (if set) to the local filesystem
     */
    public void persistHistory(){
        if(recall != null){
            try{
                new ConfigurationFileHandler(CONFIG_DIR+HISTORY_FILE).write(recall);
            }catch(IOException ex){
                ExceptionHandler.handle(ex);
            }
        }
    }

    /**
     * Deletes the recall file from the local filesystem
     * 
     * @return True if and only if the recall file was successfully deleted. False otherwise
     */
    public boolean deleteRecallFile(){
        final File recallFile = new File(CONFIG_DIR+HISTORY_FILE);
        if(recallFile.exists()){
            return recallFile.delete();
        }
        return false;
    }

    /**
     * Copies the internal configuration template file to the application configuration directory 
     * and therefore overwrites any existing config file with default values. Any configuration 
     * value in the new config that also exists in the old configuration gets restored and 
     * transferred to the new config. Values which do not exist in the new configuration are 
     * explicitly not transferred. The internal configuration gets set to the updated values.
     * 
     * <p>This method can be used after an application update to reflect changes in the application 
     * configuration file and its structure but also keeping any unchanged configurations from 
     * prior versions if they are still applicable in the new version
     */
    public void transferAll(){
        try{
            transferFields();
        }catch(Exception ex){
            //if an exception occurred during transfer,
            //then use this as a fallback to restore 
            //the new default configuration
            try{
                this.config = copyTemplate();
            }catch(IOException except){
                ExceptionHandler.handle(ex);
            }
        }
    }

    private void transferFields() throws Exception{
        final ConfigurationFile template = copyTemplate();
        for(final Field field : EditorConfiguration.class.getDeclaredFields()){
            if(Modifier.isPublic(field.getModifiers()) 
                    && Modifier.isStatic(field.getModifiers()) 
                    && Modifier.isFinal(field.getModifiers()) 
                    && field.getType().getSimpleName().equals("String") 
                    && field.getName().startsWith("CONFIG")){

                String key = null;
                try{
                    key = (String)field.get(null);
                }catch(IllegalArgumentException | IllegalAccessException ex){
                    continue;
                }
                if(key != null){
                    String oldValue = null;
                    Section temp = null;//cache
                    //look for this key in the old config
                    for(final Section sec : Section.values()){
                        ConfigurationFile.Section section = config.getSection(sec.key);
                        if(section != null){
                            final String value = section.valueOf(key);
                            if((value != null) && (!value.isEmpty())){
                                oldValue = value;
                                temp = sec;
                                break;
                            }
                        }
                    }
                    //transfer the old value to the new config 
                    if((oldValue != null) && (temp != null)){
                        String newValue = template.getSection(temp.key).valueOf(key);
                        if((newValue != null) && (!newValue.isEmpty())){
                            template.getSection(temp.key).set(key, oldValue);
                        }
                    }
                }
            }
        }
        config = template;
        configChanged = true;
        persistConfiguration();
    }

    private ConfigurationFile copyTemplate() throws IOException{
        ConfigurationFile template = null;
        BufferedInputStream is = null;
        BufferedOutputStream os = null;
        try{
            final File configDir = new File(CONFIG_DIR);
            if(!configDir.exists()){
                if(!configDir.mkdirs()){
                    throw new IOException("Failed to create config directory");
                }
            }
            is = new BufferedInputStream(getClass().getResourceAsStream(TEMPLATE_FILE));
            os = new BufferedOutputStream(new FileOutputStream(new File(CONFIG_DIR+CONFIG_FILE)));
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            int i = 0;
            while((i = is.read()) != -1){
                baos.write(i);
            }
            os.write(baos.toByteArray());
            os.close();
            template = new ConfigurationFileHandler(CONFIG_DIR+CONFIG_FILE).read();
        }catch(IOException ex){
            throw ex;
        }finally{
            if(is != null){ is.close(); }
            if(os != null){ os.close(); }
        }
        return template;
    }

    private ConfigurationFile readHistoryFile() throws IOException{
        return new ConfigurationFileHandler(CONFIG_DIR+HISTORY_FILE).read();
    }

}
