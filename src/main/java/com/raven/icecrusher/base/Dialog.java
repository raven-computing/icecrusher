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

package com.raven.icecrusher.base;

/**
 * Enumerates all dialogs and their respective FXML Controller file.
 *
 */
public enum Dialog {

    CREATE                      ("CreateDialog.fxml"),
    CREATE_DIALOG_COLUMN_ITEM   ("ColumnItem.fxml"),
    FILTER                      ("FilterDialog.fxml"),
    SAVE                        ("SaveDialog.fxml"),
    ADD_COLUMN                  ("AddColumnDialog.fxml"),
    RENAME_COLUMN               ("RenameColumnDialog.fxml"),
    SORT                        ("SortDialog.fxml"),
    STATS                       ("StatsDialog.fxml"),
    UPDATE                      ("UpdateDialog.fxml");

    public String fxml;

    Dialog(final String fxml){
        this.fxml = fxml;
    }
}
