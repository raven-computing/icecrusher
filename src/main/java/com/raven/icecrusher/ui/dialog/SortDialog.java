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
import com.raven.icecrusher.ui.dialog.SortDialogController.DialogListener;

import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A Dialog which lets the user sort a <code>DataFrame</code> 
 * by a <code>Column</code>
 *
 */
public class SortDialog extends EditorDialog {

	private SortDialogController controller;

	public SortDialog(StackPane root){
		super(root, null);
        final Layout layout = Layout.of(Dialog.SORT);
        final Parent parent = layout.load();
        controller = layout.getController();
        controller.setRootContainer(root);
		setContent((Region)parent);
	}
	
	public void setOnSort(DialogListener listener){
		controller.setSortListener(listener);
	}
	
	public void setColumns(final String[] columns){
		controller.setColumns(columns);
	}
	
}
