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
 * A SettingsView used for Line- and Area Charts.
 *
 */
public class LineSettingsView extends SettingsView {

    public LineSettingsView(int index){
        super(index);
        addRemoveButtonToSettings();
        addTextFieldToSettings();
        addColorPickerToSettings();
    }

    public LineSettingsView(int index, String text){
        super(index);
        addRemoveButtonToSettings();
        addTextFieldToSettings(text);
        addColorPickerToSettings(text);
        setEditText(text);
    }
}
