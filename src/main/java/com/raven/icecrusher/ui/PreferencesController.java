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

package com.raven.icecrusher.ui;

import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXToggleButton;
import com.raven.icecrusher.application.Controller;
import com.raven.icecrusher.io.update.Updater;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.EditorConfiguration;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller class for the preferences activity.
 *
 */
public class PreferencesController extends Controller {

    /**
     * All theme options shown in the appearance combo box
     */
    protected static final ObservableList<String> OPTIONS_THEME = 
            FXCollections.observableArrayList("Dark Theme", "Light Theme");

    @FXML
    private JFXToggleButton prefRememberTabs;

    @FXML
    private JFXToggleButton prefShowIndex;

    @FXML
    private JFXToggleButton prefClearAfterRowAdd;

    @FXML
    private JFXToggleButton prefConfirmRowDeletion;

    @FXML
    private JFXToggleButton prefDialogAlwaysHome;

    @FXML
    private JFXComboBox<String> prefTheme;

    private EditorConfiguration config;
    private boolean isDirty;
    private boolean reloadRequired;
    private boolean themeChanged;
    private boolean currentThemeDark;

    public PreferencesController(){
        this.config = getConfiguration();
    }

    @Override
    public boolean onExitRequested(){
        //ignore exit requests when an update is in progress
        if(Updater.isExecuting()){
            return false;
        }
        if(isDirty){
            this.config.persistConfiguration();
        }
        ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_RELOAD_REQUIRED, reloadRequired);
        bundle.addArgument(Const.BUNDLE_KEY_EXIT_REQUESTED, true);
        bundle.addArgument(Const.BUNDLE_KEY_THEME_CHANGED, themeChanged);
        finishActivity(bundle);
        return false;
    }

    @FXML
    public void initialize(){
        prefRememberTabs.setSelected(config.booleanOf(GLOBAL, CONFIG_RECALL_TABS));
        prefShowIndex.setSelected(config.booleanOf(GLOBAL, CONFIG_SHOW_INDEX_COL));
        prefClearAfterRowAdd.setSelected(config.booleanOf(GLOBAL, CONFIG_CLEAR_AFTER_ROW_ADD));
        prefConfirmRowDeletion.setSelected(config.booleanOf(GLOBAL, CONFIG_CONFIRM_ROW_DELETION));
        prefDialogAlwaysHome.setSelected(config.booleanOf(GLOBAL, CONFIG_DIALOG_ALWAYS_HOME));
        prefTheme.setItems(OPTIONS_THEME);
        prefTheme.getSelectionModel().select((config.booleanOf(GLOBAL, CONFIG_THEME_VIEW_DARK)) 
                ? OPTIONS_THEME.get(0) 
                        : OPTIONS_THEME.get(1));

        currentThemeDark = config.booleanOf(GLOBAL, CONFIG_THEME_VIEW_DARK);
    }

    @FXML
    private void onClose(ActionEvent event){
        if(isDirty){
            this.config.persistConfiguration();
        }
        ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_RELOAD_REQUIRED, reloadRequired);
        bundle.addArgument(Const.BUNDLE_KEY_THEME_CHANGED, themeChanged);
        finishActivity(bundle);
    }

    @FXML
    private void onPreferenceChanged(ActionEvent event){
        if((event.getSource() instanceof JFXComboBox)){
            themePreference();
            return;
        }
        final JFXToggleButton btn = (JFXToggleButton) event.getSource();
        switch(btn.getId()){
        case "prefRememberTabs":
            config.set(GLOBAL, CONFIG_RECALL_TABS, btn.isSelected());
            break;
        case "prefShowIndex":
            config.set(GLOBAL, CONFIG_SHOW_INDEX_COL, btn.isSelected());
            reloadRequired = true;
            break;
        case "prefClearAfterRowAdd":
            config.set(GLOBAL, CONFIG_CLEAR_AFTER_ROW_ADD, btn.isSelected());
            break;
        case "prefConfirmRowDeletion":
            config.set(GLOBAL, CONFIG_CONFIRM_ROW_DELETION, btn.isSelected());
            break;
        case "prefDialogAlwaysHome":
            final boolean isSelected = btn.isSelected();
            config.set(GLOBAL, CONFIG_DIALOG_ALWAYS_HOME, isSelected);
            if(isSelected){
                config.set(WINDOW, CONFIG_WINDOW_DIALOG_DIR, Const.KEY_USER_HOME);
            }
            break;
        }
        isDirty = true;
    }

    public void themePreference(){
        final boolean selectedDark = (prefTheme.getSelectionModel().getSelectedItem()
                .equals(OPTIONS_THEME.get(0)));

        themeChanged = (selectedDark ^ currentThemeDark);
        config.set(GLOBAL, CONFIG_THEME_VIEW_DARK, selectedDark);
        isDirty = true;
    }

}
