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

package com.raven.icecrusher.ui.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller class for the {@link UpdateDialog}.
 *
 */
public class UpdateDialogController {

    /**
     * Listener interface for the <code>UpdateDialog</code>.
     *
     */
    public interface DialogListener {

        /**
         * Called when the user presses the action button
         * 
         */
        void onAction(JFXButton actionButton);
    }

    @FXML
    private Label labelTitle;

    @FXML
    private Label labelMessage;

    @FXML
    private Label labelProgressMsg;

    @FXML
    private Label labelProgressValue;

    @FXML
    private JFXProgressBar progressBar;

    @FXML
    private JFXButton btnAction;

    private DialogListener delegate;

    public String getTitle(){
        return this.labelTitle.getText();
    }

    public void setTitle(final String title){
        this.labelTitle.setText(title);
    }

    public String getMessage(){
        return this.labelMessage.getText();
    }

    public void setMessage(final String message){
        this.labelMessage.setText(message);
    }

    public String getProgressMessage(){
        return this.labelProgressMsg.getText();
    }

    public void setProgressMessage(final String message){
        this.labelProgressMsg.setText(message);
    }

    public String getProgressValue(){
        return this.labelProgressValue.getText();
    }

    public void setProgressValue(final String value){
        this.labelProgressValue.setText(value);
    }

    public JFXProgressBar getProgressBar(){
        return this.progressBar;
    }

    public Label getProgressValueLabel(){
        return this.labelProgressValue;
    }

    public void setDialogListener(DialogListener delegate){
        this.delegate = delegate;
    }

    public void setActionButtonText(final String text){
        this.btnAction.setText(text);
    }

    public void setActionButtonDisabled(final boolean value){
        this.btnAction.setDisable(value);
    }

    @FXML
    private void initialize(){ }

    @FXML
    private void onAction(ActionEvent event){
        if(delegate != null){
            delegate.onAction(btnAction);
        }
    }
}
