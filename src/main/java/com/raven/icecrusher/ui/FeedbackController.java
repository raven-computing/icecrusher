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

package com.raven.icecrusher.ui;

import java.net.HttpURLConnection;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.raven.icecrusher.application.Controller;
import com.raven.icecrusher.io.update.Updater;
import com.raven.icecrusher.net.NetworkService;
import com.raven.icecrusher.net.Parcel;
import com.raven.icecrusher.net.ResourceLocator;
import com.raven.icecrusher.net.NetworkResult.Status;
import com.raven.icecrusher.ui.view.Filters.Filter;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.Feedback;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

/**
 * Controller class for the feedback activity.
 *
 */
public class FeedbackController extends Controller implements Filter {

    @FXML
    private Label txtSize;

    @FXML
    private JFXTextArea txtFeedback;

    @FXML
    private JFXTextField txtEmail;

    @FXML
    private JFXButton btnSend;

    private JFXSpinner sp;
    private NetworkService service;

    public FeedbackController(){ }

    @FXML
    public void initialize(){ }

    @Override
    public void onStart(ArgumentBundle bundle){
        this.txtFeedback.setTextFormatter(new TextFormatter<>(this));
        for(final Node n : getRootNode().getChildren()){
            if(n instanceof JFXSpinner){
                this.sp = (JFXSpinner) n;
                break;
            }
        }
    }

    @Override
    public boolean onExitRequested(){
        //ignore exit requests when an update is in progress
        if(Updater.isExecuting()){
            return false;
        }
        final ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_EXIT_REQUESTED, true);
        finishActivity(bundle);
        return false;
    }

    @Override
    public Change apply(Change t){
        final String s = t.getControlNewText();
        if(s.length() <= 250){
            this.txtSize.setText(String.format("%s / %s", s.length(), 250));
            return t;
        }else{
            return null;
        }
    }

    @FXML
    private void onSend(ActionEvent event){
        if((txtFeedback.getText() == null) || (txtFeedback.getText().isEmpty())){
            OneShotSnackbar.showFor(getRootNode(), "Please provide a feedback");
            return;
        }
        this.btnSend.setText("Sending...");
        setLoadingIndication(true);
        final Feedback feedback = new Feedback(txtFeedback.getText(), txtEmail.getText());
        feedback.setAppVersion(Const.APPLICATION_VERSION);
        final Parcel parcel = new Parcel(Feedback.serialize(feedback));
        parcel.addHeader("Content-Type", Feedback.contentType());
        this.service = NetworkService.getService(ResourceLocator.SEND_FEEDBACK, parcel);

        this.service.setOnResult((result) -> {
            setLoadingIndication(false);
            if(result.getSatus() == Status.SUCCESS){
                if((result.getResponseCode() == HttpURLConnection.HTTP_OK) 
                        || (result.getResponseCode() == HttpURLConnection.HTTP_CREATED)){

                    btnSend.setText("Sent");
                    OneShotSnackbar.showFor(getRootNode(),
                            "Thank you for your feedback!");
                    
                    finishActivity();
                }else if(result.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
                    btnSend.setText("Send");
                    OneShotSnackbar.showFor(getRootNode(),
                            "Your feedback contains invalid characters");
                    
                }else if(result.getResponseCode() == HttpURLConnection.HTTP_ENTITY_TOO_LARGE){
                    btnSend.setText("Send");
                    OneShotSnackbar.showFor(getRootNode(),
                            "The feedback text or E-Mail you entered is too long");
                    
                }else{
                    btnSend.setText("Try again");
                    OneShotSnackbar.showFor(getRootNode(), "Unable to send feedback");
                }
            }else if(result.getSatus() == Status.FAILURE){
                btnSend.setText("Try again");
                OneShotSnackbar.showFor(getRootNode(), "Unable to send feedback");
            }
            service = null;
        });
        this.service.connect();
    }

    @FXML
    private void onClose(ActionEvent event){
        finishActivity();
    }

    private void setLoadingIndication(final boolean value){
        if(sp != null){
            this.sp.setVisible(value);
        }
        getLocalRootNode().setDisable(value);
    }

}
