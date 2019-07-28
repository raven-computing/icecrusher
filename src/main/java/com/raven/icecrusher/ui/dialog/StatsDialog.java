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

package com.raven.icecrusher.ui.dialog;

import com.raven.icecrusher.application.Layout;
import com.raven.icecrusher.base.Dialog;
import com.raven.icecrusher.util.ColumnStats;

import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A Dialog which lets the user view basic statistics about a numerical Column.
 *
 */
public class StatsDialog extends EditorDialog {

    private StatsDialogController controller;

    public StatsDialog(StackPane root, ColumnStats stats){
        super(root, null);
        final Layout layout = Layout.of(Dialog.STATS);
        final Parent parent = layout.load();
        controller = layout.getController();
        controller.setCloseListener(() -> close());
        controller.setColumnStats(stats);
        setContent((Region)parent);
    }

}
