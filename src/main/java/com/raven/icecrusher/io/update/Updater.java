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

package com.raven.icecrusher.io.update;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.SwingUtilities;

import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.raven.common.util.Action;
import com.raven.icecrusher.application.Controller;
import com.raven.icecrusher.application.ProxyController;
import com.raven.icecrusher.application.StackedApplication;
import com.raven.icecrusher.net.NetworkResult;
import com.raven.icecrusher.net.NetworkService;
import com.raven.icecrusher.net.ResourceLocator;
import com.raven.icecrusher.ui.FrameController;
import com.raven.icecrusher.ui.OneShotSnackbar;
import com.raven.icecrusher.ui.dialog.Dialogs;
import com.raven.icecrusher.ui.dialog.SaveDialog;
import com.raven.icecrusher.ui.dialog.UpdateDialog;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.EditorConfiguration;
import com.raven.icecrusher.util.ExceptionHandler;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

/**
 * This class is responsible to check for updates, show new versions of this
 * application in the user's default web browser and most importantly, perform
 * application updates.
 *
 */
public class Updater implements UpdateRoutine {

    public interface UpdateHandler {

        void onResolve(Version version);
    }

    private static final String DIALOG_STATE_CLOSE = "Close";
    private static final String DIALOG_STATE_CANCEL = "Cancel";
    private static final String DIALOG_STATE_RESTART = "Restart";

    private static boolean isExecuting = false;

    private UpdateExecutor updateExecutor;
    private UpdateInfo updateInfo;
    private Version version;
    private UpdateDialog dialog;
    private NetworkService service;
    private UpdateHandler handler;

    public Updater(){ }

    /**
     * Checks whether a new version of this application has been released, passing 
     * the result to the specified UpdateHandler. The necessary data is collected 
     * over the network
     * 
     * @param handler The <code>UpdateHandler</code> for the callback
     */
    public void checkForUpdates(final UpdateHandler handler){
        this.handler = handler;
        this.service = NetworkService.getService(ResourceLocator.LATEST_VERSION);
        this.service.setOnResult((result) -> {
            if((result.getSatus() == NetworkResult.Status.SUCCESS)
                    && (result.getResponseCode() == HttpsURLConnection.HTTP_OK)){

                final UpdateInfo info = UpdateInfo.fromXmlString(result.getString());
                if(info != null){
                    updateInfo = info;
                    version = info.getVersion();
                }
                getConfiguration().set(UPDATER, CONFIG_LAST_UPDATE_CHECK, 
                        Const.DATE_FORMAT_ENCODED.format(new Date()));

                push(version);
            }else{
                push(null);
                return;
            }
            service = null;
        });
        this.service.connect();
    }

    /**
     * Indicates whether the internally saved update information is out of date
     * 
     * @return True if the last update check was performed prior to the internally defined 
     *         threshold, false otherwise
     */
    public boolean dataIsStale(){
        final EditorConfiguration config = getConfiguration();
        final String last = config.valueOf(UPDATER, CONFIG_LAST_UPDATE_CHECK);
        if(last.matches("\\d+")){
            final int nowEncoded = Integer.valueOf(Const.DATE_FORMAT_ENCODED
                    .format(new Date()));
            
            final int lastEncoded = Integer.valueOf(last);
            final int threshold = Integer.valueOf(config.valueOf(
                    UPDATER, CONFIG_DATA_STALE_THRESHOLD));
            
            return ((nowEncoded - lastEncoded) >= threshold);
        }
        return true;
    }

    /**
     * Shows the update dialog and starts the update process by initiating
     * the internal update routine.<br>
     * Controllers should check whether the update process is running by calling 
     * {@link Updater#isExecuting()} when they intercept exit requests in order
     * to avoid application shutdowns while the update is still runing.
     * 
     * @param controller The <code>Controller</code> which started the update process
     */
    public void performUpdate(final Controller controller){
        isExecuting = true;
        getConfiguration().persistConfiguration();
        try{
            final ProxyController mainController = controller.getProxyController(
                    FrameController.class);
            
            final ProxyController activeController = StackedApplication.getActiveController();
            final StackPane rootPane = (StackPane) controller.getRootNode();
            final Parent activityRootPane = activeController.getLocalRootNode();
            //make sure the current tabs state is saved
            mainController.call(FrameController.INVOCATION_KEY_SETUP_TABS_HISTORY);
            getConfiguration().persistHistory();
            //save all open tabs if required by the user
            final boolean hasModifiedTabs = (Boolean) mainController.call(
                    FrameController.INVOCATION_KEY_HAS_MODIFIED_TABS);

            if(hasModifiedTabs){
                final SaveDialog dialog = new SaveDialog(rootPane);
                dialog.setOverlayClose(false);
                dialog.setMessage("Do you want to save all modified tabs before updating?");
                dialog.setBackgroundEffect(activityRootPane, Dialogs.getBackgroundBlur());
                dialog.setOnConfirm((save) -> {
                    dialog.close();
                    dialog.setOnDialogClosed((e) -> {
                        if(save){
                            try{
                                final boolean succeeded = (Boolean) mainController.call(
                                        FrameController.INVOCATION_KEY_SAVE_TABS, 
                                        new EventHandler<ActionEvent>(){

                                            @Override
                                            public void handle(ActionEvent event){
                                                doUpdate(rootPane, activityRootPane);
                                            }
                                        });
                                if(!succeeded){
                                    OneShotSnackbar.showFor(rootPane,
                                            "Update has been cancelled");
                                    
                                    return;
                                }

                            }catch(Exception ex){
                                ExceptionHandler.handle(ex);
                            }
                        }else{//user wants to discard all changes
                            doUpdate(rootPane, activityRootPane);
                        }
                    });

                });
                dialog.show();
            }else{
                doUpdate(rootPane, activityRootPane);
            }
        }catch(Exception ex){
            ExceptionHandler.showDialog(ex);
            isExecuting = false;
        }
    }

    /**
     * Indicates whether the update process is running
     * 
     * @return True if the update process has been initiated and is currently runnning,
     *         false if it has never been started or has been cancelled
     */
    public static final boolean isExecuting(){
        return isExecuting;
    }

    public static void showInBrowser(){
        browse(ResourceLocator.UPDATE_SHOW_IN_BROWSER);
    }

    public static void showReleaseNotes(){
        browse(ResourceLocator.RELEASE_NOTES);
    }

    public static void showPostUpdateMessage(final Action action){
        final StackPane pane = action.getArgument(StackPane.class);
        if(pane == null){
            return;
        }
        Platform.runLater(() -> {
            OneShotSnackbar.showFor(pane,
                    Const.APPLICATION_NAME 
                    + " has been updated to version " 
                    + Const.APPLICATION_VERSION,
                    "What's new?",
                    10000, (e) -> {// show for 10 seconds
                        OneShotSnackbar.closeIfVisible();
                        Updater.showReleaseNotes();
                    });
        });
    }

    private void doUpdate(final StackPane rootPane, final Parent activityRootPane){
        this.dialog = new UpdateDialog(rootPane, null, DialogTransition.TOP);
        this.dialog.setBackgroundEffect(activityRootPane, Dialogs.getBackgroundBlur());
        this.dialog.setMessage("Please wait while " + Const.APPLICATION_NAME
                + " is updating.");
        
        this.dialog.setProgressMessage("Preparing...");
        this.dialog.setActionButtonText(DIALOG_STATE_CANCEL);
        this.dialog.getProgressBar().setProgress(0.0);
        this.dialog.setDialogListener((action) -> {
            if(action.getText().equals(DIALOG_STATE_CANCEL)){
                updateExecutor.cancel();
                OneShotSnackbar.showFor(rootPane, "Update has been cancelled");
            }else if(action.getText().equals(DIALOG_STATE_CLOSE)){
                //no action required
            }else if(action.getText().equals(DIALOG_STATE_RESTART)){
                updateExecutor.doUpdate();
            }
            dialog.close();
            isExecuting = false;
        });
        this.dialog.show();
        this.updateExecutor = ExecutorFactory.getUpdateExecutor(this.updateInfo, this);
        if(updateExecutor == null){
            this.dialog.close();
            OneShotSnackbar.showFor(rootPane,
                    "Unable to initialise UpdateExecutor. Unknown operating system");
            
            return;
        }
        this.dialog.setProgressMessage("Downloading...");
        this.updateExecutor.downloadPackage(dialog.getProgressBar(),
                dialog.getProgressValueLabel());
    }

    private static void browse(final ResourceLocator resource){
        //using swing utilities instead of javafx.application.HostServices here
        //because latter has had some issues
        SwingUtilities.invokeLater(() -> {
            try{
                if(Desktop.isDesktopSupported()){
                    Desktop.getDesktop().browse(new URI(resource.getUrl()));
                }
            }catch(IOException | URISyntaxException ex){
                ExceptionHandler.showDialog(ex);
            }
        });
    }

    private void push(final Version version){
        if(handler != null){
            Platform.runLater(() -> handler.onResolve(version));
        }
    }


    //********************************************//
    //            UpdateRoutine impl              //
    //********************************************//

    @Override
    public void onPackageDownloaded(boolean success){
        if(!success){
            this.dialog.setMessage(Const.APPLICATION_NAME + " is unable to download the update package");
            this.dialog.setProgressMessage("Disconnected");
            this.dialog.setActionButtonText(DIALOG_STATE_CANCEL);
            return;
        }
        this.dialog.setProgressMessage("Verifying download...");
        this.dialog.setActionButtonDisabled(true);
        this.updateExecutor.verifyDownload();
    }

    @Override
    public void onDownloadVerified(boolean isValid){
        if(!isValid){
            this.dialog.setMessage(Const.APPLICATION_NAME + " is unable to verify the update package");
            this.dialog.setProgressMessage("Verification failed");
            this.dialog.setActionButtonText(DIALOG_STATE_CLOSE);
            this.dialog.setActionButtonDisabled(false);
            return;
        }
        this.dialog.setProgressMessage("Extracting package...");
        this.updateExecutor.extractPackage();
    }

    @Override
    public void onExtracted(boolean success){
        if(!success){
            this.dialog.setMessage(Const.APPLICATION_NAME + " is unable to extract the update package");
            this.dialog.setProgressMessage("Extraction failed");
            this.dialog.setActionButtonText(DIALOG_STATE_CLOSE);
            this.dialog.setActionButtonDisabled(false);
            return;
        }
        this.dialog.setProgressMessage("Setting up instructions...");
        this.updateExecutor.setupInstructions();
    }

    @Override
    public void onInstructionsProvided(boolean success){
        if(!success){
            this.dialog.setMessage(Const.APPLICATION_NAME + " is unable to find update instructions");
            this.dialog.setProgressMessage("Update failed");
            this.dialog.setActionButtonText(DIALOG_STATE_CLOSE);
            this.dialog.setActionButtonDisabled(false);
            return;
        }
        this.dialog.setMessage(Const.APPLICATION_NAME + " must restart to finish installing updates."
                + " This may take several seconds.");

        this.dialog.setProgressMessage("Finished");
        this.dialog.setActionButtonText(DIALOG_STATE_RESTART);
        this.dialog.setActionButtonDisabled(false);
    }

}
