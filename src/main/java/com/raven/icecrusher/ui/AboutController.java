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

import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

import com.jfoenix.controls.JFXButton;
import com.raven.icecrusher.Editor;
import com.raven.icecrusher.application.Controller;
import com.raven.icecrusher.io.update.Updater;
import com.raven.icecrusher.io.update.Version;
import com.raven.icecrusher.io.update.Updater.UpdateHandler;
import com.raven.icecrusher.util.Const;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller class for the about activity.
 *
 */
public class AboutController extends Controller implements UpdateHandler {
	
	@FXML
	private Label labelApp;
	
	@FXML
	private Label labelVersion;
	
	@FXML
	private Label labelDevel;
	
	@FXML
	private Label labelContact;
	
	@FXML
	private Label labelCopyright;
	
	@FXML
	private JFXButton btnUpdate;
	
	private Updater updater;

	public AboutController(){ }
	
	@FXML
	public void initialize(){
		labelApp.setText(Const.APPLICATION_NAME);
		labelVersion.setText(Const.APPLICATION_VERSION);
		labelDevel.setText(Const.APPLICATION_DEVELOPER);
		labelContact.setText(Const.APPLICATION_DEVELOPER_EMAIL);
		labelCopyright.setText("Copyright " + '\u00A9' + " " 
		            + Const.APPLICATION_COPYRIGHT + ", " 
				    + Const.APPLICATION_DEVELOPER);
		
		Platform.runLater(() -> btnUpdate.requestFocus());
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
	
	@FXML
	private void onClose(ActionEvent event){
		finishActivity();
	}
	
	@FXML
	private void onUpdate(ActionEvent event){
		if(updater == null){
			this.updater = new Updater();
			this.btnUpdate.setText("Checking for updates...");
			this.updater.checkForUpdates(this);
		}
	}

	@Override
	public void onResolve(Version version){
		if(version != null){
			final int i = Version.current().compareTo(version);
			if(i == 0){
				getConfiguration().set(UPDATER, CONFIG_UPDATE_AVAILABLE, false);
				OneShotSnackbar.showFor(getRootNode(), "This application is up to date", 6000);
				this.btnUpdate.setText("You're up to date");
			}else if(i < 0){
				getConfiguration().set(UPDATER, CONFIG_UPDATE_AVAILABLE, true);
				OneShotSnackbar.showFor(getRootNode(), "Version "+version+" is now available", 
						(Editor.isNative() ? "Update" : "Show"),
						Const.TIME_SHOW_UPDATE_NOTIFICATION, (event) -> {
							
							OneShotSnackbar.closeIfVisible();
							if(Editor.isNative()){
								updater.performUpdate(this);
							}else{
								Updater.showInBrowser();
							}
						});
				this.btnUpdate.setText("v"+version+" available");
			}
		}else{
			OneShotSnackbar.showFor(getRootNode(), "Unable to check for updates", 6000);
			this.btnUpdate.setText("Disconnected");
		}
	}
	
}
