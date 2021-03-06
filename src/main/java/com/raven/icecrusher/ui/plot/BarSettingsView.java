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

package com.raven.icecrusher.ui.plot;

/**
 * A SettingsView used for Bar Charts.
 *
 */
public class BarSettingsView extends SettingsView {
    
    private String mode = "";

    public BarSettingsView(int index){
        super(index);
        addRemoveButtonToSettings();
        addTextFieldToSettings();
        addColorPickerToSettings();
    }

    public BarSettingsView(int index, String text, String mode){
        super(index);
        this.mode = mode;
        addRemoveButtonToSettings();
        addTextFieldToSettings(text);
        addColorPickerToSettings(text);
        setEditText(text);
    }
    
    @Override
    public String editTextCacheKey(final String text){
        return "SettingsView.textField.text." + mode + "." + text;
    }
    
    @Override
    public String colorPickerCacheKey(final String text){
        return "SettingsView.colorPicker.color." + mode + "." + text;
    }
}
