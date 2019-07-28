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

import com.jfoenix.controls.JFXProgressBar;
import com.raven.icecrusher.application.Layout;
import com.raven.icecrusher.base.Dialog;
import com.raven.icecrusher.ui.dialog.UpdateDialogController.DialogListener;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A Dialog which lets the user view information about the application update process.
 *
 */
public class UpdateDialog extends EditorDialog {

    private UpdateDialogController controller;

    public UpdateDialog(StackPane root){
        super(root, null);
        loadContent();
    }

    public UpdateDialog(StackPane dialogContainer, Region content,
            DialogTransition transitionType){

        super(dialogContainer, content, transitionType);
        loadContent();
    }

    public String getTitle(){
        return this.controller.getTitle();
    }

    public void setTitle(final String title){
        this.controller.setTitle(title);
    }

    public String getMessage(){
        return this.controller.getMessage();
    }

    public void setMessage(final String message){
        this.controller.setMessage(message);
    }

    public String getProgressMessage(){
        return this.controller.getProgressMessage();
    }

    public void setProgressMessage(final String message){
        this.controller.setProgressMessage(message);
    }

    public String getProgressValue(){
        return this.controller.getProgressValue();
    }

    public void setProgressValue(final String value){
        this.controller.setProgressValue(value);
    }

    public JFXProgressBar getProgressBar(){
        return this.controller.getProgressBar();
    }

    public Label getProgressValueLabel(){
        return this.controller.getProgressValueLabel();
    }

    public void setDialogListener(DialogListener delegate){
        this.controller.setDialogListener(delegate);
    }

    public void setActionButtonText(final String text){
        this.controller.setActionButtonText(text);
    }

    public void setActionButtonDisabled(final boolean value){
        this.controller.setActionButtonDisabled(value);
    }

    private void loadContent(){
        final Layout layout = Layout.of(Dialog.UPDATE);
        final Parent parent = layout.load();
        controller = layout.getController();
        setContent((Region)parent);
        setOverlayClose(false);
    }

}
