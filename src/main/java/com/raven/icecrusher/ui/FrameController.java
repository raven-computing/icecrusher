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

import java.util.LinkedList;
import java.util.List;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTabPane;
import com.raven.common.struct.Column;
import com.raven.common.struct.DataFrame;
import com.raven.common.struct.DefaultDataFrame;
import com.raven.common.struct.DoubleColumn;
import com.raven.common.struct.FloatColumn;
import com.raven.common.struct.NullableDataFrame;
import com.raven.common.struct.NullableDoubleColumn;
import com.raven.common.struct.NullableFloatColumn;
import com.raven.common.util.Chronometer;
import com.raven.icecrusher.Editor;
import com.raven.icecrusher.application.Controller;
import com.raven.icecrusher.application.Exposed;
import com.raven.icecrusher.application.Resources;
import com.raven.icecrusher.base.Activity;
import com.raven.icecrusher.io.ConversionException;
import com.raven.icecrusher.io.DataFrames;
import com.raven.icecrusher.io.Files;
import com.raven.icecrusher.io.update.Updater;
import com.raven.icecrusher.io.update.Version;
import com.raven.icecrusher.ui.dialog.AddColumnDialog;
import com.raven.icecrusher.ui.dialog.ConfirmationDialog;
import com.raven.icecrusher.ui.dialog.CreateDialog;
import com.raven.icecrusher.ui.dialog.Dialogs;
import com.raven.icecrusher.ui.dialog.ExportDialog;
import com.raven.icecrusher.ui.dialog.FilterDialog;
import com.raven.icecrusher.ui.dialog.ImportDialog;
import com.raven.icecrusher.ui.dialog.RenameColumnDialog;
import com.raven.icecrusher.ui.dialog.SaveDialog;
import com.raven.icecrusher.ui.dialog.SortDialog;
import com.raven.icecrusher.ui.dialog.StatsDialog;
import com.raven.icecrusher.ui.view.DataFrameView;
import com.raven.icecrusher.ui.view.DataFrameView.ContextMenuEvent;
import com.raven.icecrusher.ui.view.DataFrameView.EditEvent;
import com.raven.icecrusher.ui.view.DataFrameView.ViewListener;
import com.raven.icecrusher.util.ColumnStats;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.EditorConfiguration;
import com.raven.icecrusher.util.EditorFile;
import com.raven.icecrusher.util.ExceptionHandler;
import com.raven.icecrusher.util.History;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.raven.common.io.DataFrameSerializer.DF_FILE_EXTENSION;
import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

/**
 * Controller class for the main activity.
 *
 */
public class FrameController extends Controller implements ViewListener {

    /**
     * Invocation key which can be used by other Controllers to save all open tabs
     */
    public static final String INVOCATION_KEY_SAVE_TABS = "Invoke-1";

    /**
     * Invocation key which can be used by other Controllers to see if the user has 
     * any modified (i.e. unsaved) tabs open
     */
    public static final String INVOCATION_KEY_HAS_MODIFIED_TABS = "Invoke-2";

    /**
     * Invocation key which can be used by other Controllers to setup internal history
     * configuration according to the current state of all open tabs
     */
    public static final String INVOCATION_KEY_SETUP_TABS_HISTORY = "Invoke-3";

    @FXML
    protected StackPane rootPane;

    @FXML
    protected AnchorPane editScene;

    @FXML
    protected MenuItem menuSave;

    @FXML
    protected MenuItem menuSaveAs;

    @FXML
    protected MenuItem menuExport;

    @FXML
    protected MenuItem menuAddRow;

    @FXML
    protected MenuItem menuAddCol;

    @FXML
    protected MenuItem menuSort;

    @FXML
    protected MenuItem menuFilter;

    @FXML
    protected MenuItem menuConvert;

    @FXML
    protected MenuItem menuPieChart;

    @FXML
    protected MenuItem menuLineChart;

    @FXML
    protected MenuItem menuAreaChart;

    @FXML
    protected MenuItem menuBarChart;

    @FXML
    protected Label labelType;

    @FXML
    protected Label labelRows;

    @FXML
    protected Label labelCols;

    @FXML
    protected Label labelHint;

    @FXML
    protected Label editSceneFile;

    @FXML
    protected AnchorPane editSceneAnchor;

    @FXML
    protected BorderPane mainBorderPane;

    @FXML
    protected SplitPane splitPane;

    @FXML
    protected AnchorPane mainTabsPane;

    @FXML 
    protected JFXTabPane mainTabs;

    @FXML
    protected JFXButton btnSave;

    @FXML
    protected JFXSpinner sp;

    protected EditorConfiguration config;

    private RowAdder adder;

    //keep reference to avoid garbage collection
    //when updating
    private Updater updater;

    //only to be used in saveTabsAndExit()
    private volatile int exitLatch;
    private boolean sliderIsUp;
    private boolean showingConfirmation;

    public FrameController(){
        this.config = getConfiguration();
    }

    @FXML
    public void initialize(){
        sp.setVisible(false);
        mainTabs.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        setSaveButtonsDisabled(true);
        setEditMenuItemsDisabled(true);
        setStatsMenuItemsDisabled(true);

        editScene.setManaged(false);
        editScene.setVisible(false);
        splitPane.setDividerPositions(1.0);
        //tab pane behaviour
        mainTabs.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            tabSwitchBehaviour(newTab);
        });

        //later executed on FX application thread
        Platform.runLater(() -> {
            splitPane.lookupAll(".split-pane-divider")
            .stream().forEach(node ->  node.setMouseTransparent(true));

            getStage().heightProperty()
            .addListener((observable, oldValue, newValue) -> splitPane.setDividerPositions(1.0));

            enableDarkTheme(config.booleanOf(GLOBAL, CONFIG_THEME_VIEW_DARK));

            if(config.booleanOf(UPDATER, CONFIG_AUTO_UPDATE_CHECK)){
                updateBehaviour();
            }
        });
    }

    @Override
    public void onStart(ArgumentBundle bundle){
        if(config.booleanOf(GLOBAL, CONFIG_RECALL_TABS)){
            recoverHistory();
        }
        if(Editor.wasUpdated()){
            final Chronometer chrono = new Chronometer();
            chrono.setTimer(java.time.Duration.ofSeconds(2), (elapsed) -> {
                chrono.stop();
                Platform.runLater(() -> {
                    OneShotSnackbar.showFor(rootPane,
                            Const.APPLICATION_NAME 
                            + " has been updated to version " 
                            + Const.APPLICATION_VERSION,
                            "What's new?",
                            10000, (e) -> {// show for 10 seconds
                                OneShotSnackbar.closeIfVisible();
                                Updater.showReleaseNotes();
                            });
                });
            }).start();
        }
    }

    @Override
    public void onResume(ArgumentBundle bundle){
        if(bundle != null){
            final Boolean reload = (Boolean) bundle.getArgument(Const.BUNDLE_KEY_RELOAD_REQUIRED);
            if((reload != null) && reload){
                for(final Tab tab : mainTabs.getTabs()){
                    ((FileTab)tab).getView()
                    .showIndexColumn(config.booleanOf(GLOBAL, CONFIG_SHOW_INDEX_COL));
                }
            }
            final Boolean theme = (Boolean) bundle.getArgument(Const.BUNDLE_KEY_THEME_CHANGED);
            if((theme != null) && theme){
                enableDarkTheme(config.booleanOf(GLOBAL, CONFIG_THEME_VIEW_DARK));
            }
            final Boolean exit = (Boolean) bundle.getArgument(Const.BUNDLE_KEY_EXIT_REQUESTED);
            if((exit != null) && exit){
                if(exit()){
                    //call Platform.exit() directly
                    Platform.exit();
                }
            }
        }
    }

    @Override
    public boolean onExitRequested(){
        //ignore exit requests when an update is in progress
        if(Updater.isExecuting()){
            return false;
        }
        return exit();
    }

    @Override
    public void onEdit(EditEvent event){
        setSaveButtonsDisabled(false);
        currentlySelectedTab().setSaved(false);
        if(event.isRowDeletion()){
            final DataFrameView view = event.getView();
            if(config.booleanOf(GLOBAL, CONFIG_CONFIRM_ROW_DELETION)){//ask for confirmation
                if(!showingConfirmation){//ignore if dialog is already showing
                    showingConfirmation = true;
                    final ConfirmationDialog dialog = new ConfirmationDialog(rootPane, 
                            "Delete?", "Are you sure that you want to delete this row?",
                            "Delete");
                    dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
                    dialog.setOnConfirm(() -> {
                        dialog.close();
                        view.getDataFrame().removeRow(event.getRow());
                        view.reload();
                        labelRows.setText(String.format("%,d", view.getDataFrame().rows()));
                        setStatsMenuItemsDisabled(view.getDataFrame());
                    });
                    dialog.setOnDialogClosed((e) -> showingConfirmation = false);
                    dialog.show();
                }
            }else{//skip confirmation
                view.getDataFrame().removeRow(event.getRow());
                view.reload();
                setStatsMenuItemsDisabled(view.getDataFrame());
            }
        }
        labelRows.setText(String.format("%,d", event.getView().getDataFrame().rows()));
        labelCols.setText(String.format("%,d", event.getView().getDataFrame().columns()));
    }

    @Override
    public void onMenuAction(ContextMenuEvent event){
        if(sliderIsUp){
            slide();
        }
        switch(event.getAction()){
        case RENAME:
            renameColumn(event);
            break;
        case SORT:
            sortColumn(event.getView(), event.getColumnName());
            break;
        case STATS:
            showColumnStats(event);
            break;
        case CONVERT:
            convertColumn(event);
            break;
        case DELETE:
            deleteColumn(event);
            break;
        default:
            //ignore
        }
    }

    @Exposed(INVOCATION_KEY_SAVE_TABS)
    public boolean saveTabs(final EventHandler<ActionEvent> handler){
        return this.saveAllTabs(false, handler);
    }

    @Exposed(INVOCATION_KEY_HAS_MODIFIED_TABS)
    public boolean hasModifiedTabs(){
        return this.tabsModified();
    }

    @Exposed(INVOCATION_KEY_SETUP_TABS_HISTORY)
    public void setupTabsHistory(){
        this.setupHistory();
    }

    private void tabSwitchBehaviour(final Tab newTab){
        if(sliderIsUp){
            slide();
        }
        if(newTab != null){
            final FileTab tab = (FileTab) newTab;
            final DataFrame df = tab.getDataFrame();
            setSaveButtonsDisabled(tab.isSaved());
            if(df.columns() > 0){
                setEditMenuItemsDisabled(false);
                setStatsMenuItemsDisabled(df);
            }else{//handle uninitialized df
                setEditMenuItemsDisabled(true);
                setStatsMenuItemsDisabled(true);
                menuAddCol.setDisable(false);
            }
            labelType.setText(df.getClass().getSimpleName());
            labelRows.setText(String.format("%,d", df.rows()));
            labelCols.setText(String.format("%,d", df.columns()));
        }else{//all tabs got closed
            setEditMenuItemsDisabled(true);
            setStatsMenuItemsDisabled(true);
            setSaveButtonsDisabled(true);
            labelHint.setVisible(true);
            labelType.setText(" ");
            labelRows.setText(" ");
            labelCols.setText(" ");
        }
    }

    private boolean fileIsDuplicate(final EditorFile file){
        for(final Tab tab : mainTabs.getTabs()){
            if(file.equals(((FileTab)tab).getFile())){
                mainTabs.getSelectionModel().select(tab);
                return true;
            }
        }
        return false;
    }

    private boolean tabsModified(){
        for(final Tab t : mainTabs.getTabs()){
            final FileTab tab = (FileTab) t;
            if(!tab.isSaved()){
                return true;
            }
        }
        return false;
    }

    private void updateBehaviour(){
        this.updater = new Updater();
        if(config.booleanOf(UPDATER, CONFIG_UPDATE_AVAILABLE) || updater.dataIsStale()){
            updater.checkForUpdates((version) -> {
                if(version != null){
                    final int i = Version.current().compareTo(version);
                    if(i < 0){
                        config.set(UPDATER, CONFIG_UPDATE_AVAILABLE, true);
                        final boolean isNativ = Editor.isNative();
                        OneShotSnackbar.showFor(getRootNode(), 
                                "Version "+version+" is now available", 
                                (isNativ ? "Update" : "Show"),
                                Const.TIME_SHOW_UPDATE_NOTIFICATION, (e) -> {

                                    OneShotSnackbar.closeIfVisible();
                                    if(isNativ){
                                        updater.performUpdate(this);
                                    }else{
                                        Updater.showInBrowser();
                                        updater = null;//discard reference
                                    }
                                });
                    }else{
                        config.set(UPDATER, CONFIG_UPDATE_AVAILABLE, false);
                    }
                }
            });
        }
    }

    private void setupHistory(){
        if(config.booleanOf(GLOBAL, CONFIG_RECALL_TABS)){
            final History history = new History();
            history.setFocusIndex(mainTabs.getSelectionModel().getSelectedIndex());
            final List<EditorFile> list = new LinkedList<>();
            for(final Tab tab : mainTabs.getTabs()){
                list.add(((FileTab)tab).getFile());
            }
            history.setHistoryList(list);
            config.setHistory(history);
        }else{
            config.deleteRecallFile();
        }
    }

    private void recoverHistory(){
        final History history = config.getHistory();
        if(history != null){
            final List<EditorFile> historyList = history.getHistoryList();
            if(!historyList.isEmpty()){
                setLoadingIndication(true);
                labelHint.setVisible(false);
                Files.readAllFiles(historyList, (tabs) -> {
                    for(int i=0; i<tabs.size(); ++i){
                        final FileTab tab = tabs.get(i);
                        final EditorFile file = historyList.get(i);
                        final DataFrame df = tab.getDataFrame();
                        if(df != null){
                            tab.setText(file.getName());
                            tab.getView().addEditListener(this);
                            tab.setOnCloseRequest((e) -> setTabCloseBehaviour(e, tab));
                            mainTabs.getTabs().add(tab);
                            if(df.columns() > 0){
                                setEditMenuItemsDisabled(false);
                                setStatsMenuItemsDisabled(df);
                            }else{//disable for uninitialized df, except for adding columns
                                setEditMenuItemsDisabled(true);
                                setStatsMenuItemsDisabled(true);
                                menuAddCol.setDisable(false);
                            }
                        }
                    }
                    setLoadingIndication(false);
                    if(tabs.isEmpty()){//no file could be recovered
                        labelHint.setVisible(true);
                    }
                    final int i = history.getFocusIndex();
                    mainTabs.getSelectionModel().select(
                            ((i>=0 && i<mainTabs.getTabs().size()) ? i : 0));
                    
                });
            }
        }
    }

    private void setTabCloseBehaviour(final Event event, final FileTab tab){
        if(!tab.isSaved()){
            event.consume();
            mainTabs.getSelectionModel().select(tab);
            final SaveDialog dialog = new SaveDialog(rootPane);
            dialog.setMessage("Do you want to save this tab before closing?");
            dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
            dialog.setOnConfirm((save) -> {
                dialog.close();
                if(save){ 
                    saveTab(tab, true); 
                }else{
                    tab.getView().removeEditListener(this);
                    mainTabs.getTabs().remove(tab);
                }
            });
            dialog.show();
        }
    }

    private void slide(){
        //compute the target length of the key value.
        //the row adder pane has a constant height of 134
        double target = (sliderIsUp ? 1.0 : (1.0 - (134.0 / splitPane.getHeight())));
        KeyValue keyValue = new KeyValue(splitPane.getDividers().get(0).positionProperty(),
                target, Interpolator.EASE_BOTH);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(600), keyValue));
        if(sliderIsUp){
            editScene.setManaged(false);
        }else{
            //always expand manually, plus offset, when sliding up 
            //because editScene is unmanaged at this point
            editScene.resize((double) splitPane.getWidth()-2.0, editScene.getHeight()+40.0);
            editScene.setVisible(true);
        }
        timeline.setOnFinished((e) -> {
            if(sliderIsUp){
                editScene.setVisible(false);
                editSceneAnchor.getChildren().clear();
            }else{
                editScene.setManaged(true);
            }
            sliderIsUp = !sliderIsUp;
        });
        timeline.play();
    }

    private void showSnackbar(final String msg){
        OneShotSnackbar.showFor(rootPane, msg);
    }

    private void setSaveButtonsDisabled(final boolean value){
        btnSave.setDisable(value);
        menuSave.setDisable(value);
    }
    private void setEditMenuItemsDisabled(final boolean value){
        menuAddRow.setDisable(value);
        menuAddCol.setDisable(value);
        menuSort.setDisable(value);
        menuFilter.setDisable(value);
        menuConvert.setDisable(value);
        //also handle saveAs and export menu item here
        menuSaveAs.setDisable(value);
        menuExport.setDisable(value);
    }

    private void setStatsMenuItemsDisabled(final boolean value){
        menuPieChart.setDisable(value);
        menuLineChart.setDisable(value);
        menuAreaChart.setDisable(value);
        menuBarChart.setDisable(value);
    }

    private void setStatsMenuItemsDisabled(final DataFrame df){
        //charts need at least one column
        final boolean colsOk = (df.columns() >= 1);
        //line and area charts need at least two columns
        final boolean atLeastTwoCols = (df.columns() >= 2);
        //charts need at least one row of data
        final boolean rowsOk = (df.rows() >= 1);

        menuPieChart.setDisable(!(rowsOk && colsOk));
        menuLineChart.setDisable(!(rowsOk && colsOk && atLeastTwoCols));
        menuAreaChart.setDisable(!(rowsOk && colsOk && atLeastTwoCols));
        menuBarChart.setDisable(!(rowsOk && colsOk && atLeastTwoCols));
    }

    private void setLoadingIndication(final boolean value){
        sp.setVisible(value);
        mainBorderPane.setDisable(value);
    }

    private FileTab currentlySelectedTab(){
        return (FileTab)mainTabs.getSelectionModel().getSelectedItem();
    }

    private DataFrame convert(final DataFrame df){
        return DataFrames.sanitize(DataFrame.convert(df, (df.isNullable() 
                ? DefaultDataFrame.class 
                        : NullableDataFrame.class)));
    }

    private void renameColumn(final ContextMenuEvent event){
        final FileTab tab = currentlySelectedTab();
        final EditorFile file = tab.getFile();
        if((file != null) && (file.isImported()) && (!file.hasCSVHeader())){
            showSnackbar("This CSV file has not header");
            return;
        }
        final DataFrameView view = event.getView();
        final RenameColumnDialog dialog = new RenameColumnDialog(rootPane);
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setCurrent(view.getDataFrame(), event.getColumnName());
        dialog.setOnRename((newName) -> {
            final int index = view.getDataFrame().getColumnIndex(event.getColumnName());
            view.getDataFrame().setColumnName(index, newName);
            final int OFFSET = (config.booleanOf(GLOBAL, CONFIG_SHOW_INDEX_COL) ? 1 : 0);
            view.getColumns().get(index+OFFSET).setText(newName);
            tab.setSaved(false);
            setSaveButtonsDisabled(false);
            dialog.close();
        });
        dialog.show();
    }

    private void sortColumn(final DataFrameView view, final String colummn){
        final DataFrame df = view.getDataFrame();
        if(df.rows() > 1){
            setSaveButtonsDisabled(false);
            currentlySelectedTab().setSaved(false);
            if(df.rows() >= Const.DF_PARALLELISM_THRESHOLD){
                parallelSortColumn(view, colummn);
            }else{
                //sort directly on FX application thread
                view.getDataFrame().sortBy(colummn);
                view.reload();
            }
        }
    }

    private void parallelSortColumn(final DataFrameView view, final String colummn){
        setLoadingIndication(true);
        new Thread(new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                view.getDataFrame().sortBy(colummn);
                Platform.runLater(() -> {
                    view.reload();
                    setLoadingIndication(false);
                });
                return null;
            }
        }).start();
    }

    private void deleteColumn(final ContextMenuEvent event){
        final DataFrameView view = event.getView();
        if(view.getDataFrame().columns() <= 1){
            showSnackbar("Your DataFrame must have at least one column");
            return;
        }
        final ConfirmationDialog dialog = new ConfirmationDialog(rootPane,
                "Delete Column?", "Are you sure that you want to delete the "
                 + event.getColumnName()+" column?", "Delete");
        
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setOnConfirm(() -> {
            dialog.close();
            setSaveButtonsDisabled(false);
            currentlySelectedTab().setSaved(false);
            view.getDataFrame().removeColumn(event.getColumnName());
            view.reload();
            setStatsMenuItemsDisabled(view.getDataFrame());
            labelCols.setText(String.format("%,d", view.getDataFrame().columns()));
        });
        dialog.show();
    }

    private void showColumnStats(final ContextMenuEvent event){
        final DataFrame df = event.getView().getDataFrame();
        final String name = event.getColumnName();
        final ColumnStats stats = new ColumnStats();
        final Column col = df.getColumn(name);
        final byte type = col.typeCode();
        stats.setUsesDecimals(
                (type == FloatColumn.TYPE_CODE
                || type == DoubleColumn.TYPE_CODE
                || type == NullableFloatColumn.TYPE_CODE
                || type == NullableDoubleColumn.TYPE_CODE));

        stats.setColumnName(name);
        stats.setMinimum(df.minimum(name));
        stats.setMaximum(df.maximum(name));
        final double sum = stats.computeSumFor(col);
        stats.setAverage(sum/df.rows());

        final StatsDialog dialog = new StatsDialog(rootPane, stats);
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.show();
    }

    private void convertColumn(final ContextMenuEvent event){
        final FileTab tab  = currentlySelectedTab();
        final DataFrame df = tab.getDataFrame();
        df.flush();
        try{
            final Column col = DataFrames.convertColumn(df.getColumn(event.getColumnName()),
                    df.rows(), event.getConversionTarget());

            df.setColumnAt(df.getColumnIndex(event.getColumnName()), col);
        }catch(ConversionException ex){
            showSnackbar(ex.getMessage() + " at index " + ex.getRowIndex() 
            + " (" + ex.getFormattedOffendingValue() + ")");

            return;
        }
        tab.getView().reload();
        tab.setSaved(false);
        setSaveButtonsDisabled(false);
    }

    private void saveTab(final FileTab tab){
        saveTab(tab, false);
    }

    private void saveTab(final FileTab tab, final boolean removeWhenSaved){
        EditorFile file = tab.getFile();
        if(file == null){
            file = Dialogs.showFileSaveDialog(getStage());
            if(file == null){
                return;
            }
            if(!file.getName().endsWith(DF_FILE_EXTENSION)){
                //add file extension manually
                file = Files.addExtensionToFile(file);
            }
        }
        setLoadingIndication(true);
        Files.persistFile(file, tab.getDataFrame(), (f) -> {
            setLoadingIndication(false);
            if(removeWhenSaved){
                tab.getView().removeEditListener(this);
                mainTabs.getTabs().remove(tab);
            }
        });
        setSaveButtonsDisabled(true);
        tab.setFile(file);
        tab.setSaved(true);
    }

    private boolean saveAllTabs(final boolean exit){
        return saveAllTabs(exit, null);
    }

    private boolean saveAllTabs(final boolean exit, final EventHandler<ActionEvent> handler){
        final List<Tab> tabs = mainTabs.getTabs();
        final List<FileTab> unsaved = new LinkedList<>();
        for(final Tab t : tabs){
            final FileTab tab = (FileTab) t;
            if(!tab.isSaved()){
                unsaved.add(tab);
            }
        }
        //set exit latch so that the last thread terminates this application
        exitLatch = unsaved.size();
        setLoadingIndication(true);
        for(final FileTab tab : unsaved){
            if(tab.getFile() == null){
                mainTabs.getSelectionModel().select(tab);
            }
            EditorFile file = tab.getFile();
            if(file == null){
                file = Dialogs.showFileSaveDialog(getStage());
                if(file == null){
                    setLoadingIndication(false);
                    return false;
                }
                if(!file.getName().endsWith(DF_FILE_EXTENSION)){
                    //add file extension manually
                    file = Files.addExtensionToFile(file);
                }
                tab.setFile(file);
            }
            Files.persistFile(file, tab.getDataFrame(), (f) -> {
                tab.setSaved(true);
                exitCount();
                if(exitLatch == 0){
                    if(handler != null){
                        Platform.runLater(() -> {
                            handler.handle(new ActionEvent());
                        });
                    }
                    if(exit){
                        //save history first
                        setupHistory();
                        //call Platform.exit() directly. No need to go through lifecycle again
                        Platform.exit();
                    }else{
                        Platform.runLater(() -> {
                            setLoadingIndication(false);
                        });
                    }
                }
            });
        }
        return true;
    }

    private synchronized void exitCount(){
        --exitLatch;
    }

    private boolean exit(){
        if(!showingConfirmation){
            if(tabsModified()){
                this.showingConfirmation = true;
                final SaveDialog dialog = new SaveDialog(rootPane);
                dialog.setMessage("Do you want to save all modified tabs before closing?");
                dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
                dialog.setOnConfirm((save) -> {
                    dialog.close();
                    if(save){
                        saveAllTabs(true);
                    }else{//user wants to discard all changes
                        setupHistory();
                        //call Platform.exit() directly
                        Platform.exit();
                    }
                });
                dialog.setOnDialogClosed((e) -> showingConfirmation = false);
                dialog.show();
                return false;
            }else{
                setupHistory();
                return true;
            }
        }
        //some dialog is awaiting confirmation at this point
        //discontinue exit process
        return false;
    }

    private void enableDarkTheme(final boolean value){
        try{
            final ImageView img = (ImageView) this.labelHint.getGraphic();
            if(value && (mainTabsPane.getStyleClass().add("tabs-pane-dark"))){
                img.setImage(Resources.image(Resources.IC_FOLDER_WHITE));
            }else{
                if(mainTabsPane.getStyleClass().remove("tabs-pane-dark")){
                    img.setImage(Resources.image(Resources.IC_FOLDER_BLACK));
                }
            }
        }catch(Exception ex){
            ExceptionHandler.handle(ex);
        }
    }

    @FXML
    private void onFileNew(ActionEvent event){
        final CreateDialog dialog = new CreateDialog(rootPane);
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setOnCreate((df) -> {
            FileTab tab = new FileTab(null, df);
            tab.setText(Files.DEFAULT_NEW_FILENAME);
            tab.getView().addEditListener(this);
            tab.setOnCloseRequest((e) -> setTabCloseBehaviour(e, tab));
            tab.setSaved(false);
            mainTabs.getTabs().add(tab);
            dialog.close();
            mainTabs.getSelectionModel().select(tab);
            setEditMenuItemsDisabled(false);
            setStatsMenuItemsDisabled(true);
        });
        dialog.show();
    }

    @FXML
    private void onFileOpen(ActionEvent event){
        final Stage stage = getStage();
        final EditorFile file = Dialogs.showFileOpenDialog(stage);
        if(file != null && file.exists()){
            if(fileIsDuplicate(file)){
                return;
            }
            setLoadingIndication(true);
            labelHint.setVisible(false);
            Files.readFile(file, (tab) -> {
                if(tab != null){
                    final DataFrame df = tab.getDataFrame();
                    tab.setText(file.getName());
                    tab.getView().addEditListener(this);
                    tab.setOnCloseRequest((e) -> setTabCloseBehaviour(e, tab));
                    mainTabs.getTabs().add(tab);
                    mainTabs.getSelectionModel().select(tab);
                    if(df.columns() > 0){
                        setEditMenuItemsDisabled(false);
                        setStatsMenuItemsDisabled(df);
                    }else{//disable for uninitialized df, except for adding columns
                        setEditMenuItemsDisabled(true);
                        setStatsMenuItemsDisabled(true);
                        menuAddCol.setDisable(false);
                    }
                }
                setLoadingIndication(false);
            });
        }
    }

    @FXML
    private void onFileSave(ActionEvent event){
        saveTab(currentlySelectedTab());
    }

    @FXML
    private void onFileSaveAs(ActionEvent event){
        final Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        final FileTab tab = currentlySelectedTab();
        EditorFile file = Dialogs.showFileSaveDialog(stage);
        if(file == null){
            return;
        }
        if(!file.getName().endsWith(DF_FILE_EXTENSION)){
            //add file extension manually
            file = Files.addExtensionToFile(file);
        }
        setLoadingIndication(true);
        Files.persistFile(file, tab.getDataFrame(), (f) -> setLoadingIndication(false));
        tab.setFile(file);
        tab.setSaved(true);
        setSaveButtonsDisabled(true);
    }

    @FXML
    private void onFileImport(ActionEvent event){
        try{
            final EditorFile file = Dialogs.showFileImportDialog(getStage());
            if(file == null){
                return;
            }
            if(fileIsDuplicate(file)){
                return;
            }
            file.setImported(true);
            final ImportDialog dialog = new ImportDialog(rootPane);
            dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
            dialog.setOnImport((hasHeader, separator) -> {
                file.setCSVSeparator(separator);
                file.hasCSVHeader(hasHeader);
                setLoadingIndication(true);
                dialog.close();
                Files.readFile(file, (tab) -> {
                    if(tab == null){
                        showSnackbar("Improperly formatted CSV file");
                    }else{
                        tab.setText(file.getName());
                        tab.getView().addEditListener(this);
                        tab.setOnCloseRequest((e) -> setTabCloseBehaviour(e, tab));
                        mainTabs.getTabs().add(tab);
                        mainTabs.getSelectionModel().select(tab);
                        setStatsMenuItemsDisabled(tab.getDataFrame());
                        setEditMenuItemsDisabled(false);
                    }
                    setLoadingIndication(false);
                });
            });
            dialog.show();
        }catch(Exception ex){
            ExceptionHandler.showDialog(ex);
        }
    }

    @FXML
    private void onFileExport(ActionEvent event){
        final FileTab tab = currentlySelectedTab();
        final EditorFile file = Dialogs.showFileExportDialog(getStage());
        if(file == null){
            return;
        }
        file.setImported(true);
        final ExportDialog dialog = new ExportDialog(rootPane);
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setOnExport((separator) -> {
            dialog.close();
            file.setCSVSeparator(separator);
            setLoadingIndication(true);
            Files.persistFile(file, tab.getDataFrame(), (f) -> setLoadingIndication(false));
        });
        dialog.show();
    }

    @FXML
    private void onFileQuit(ActionEvent event){
        Editor.exit();
    }

    @FXML
    private void onEditAddRow(ActionEvent event){
        if(!sliderIsUp){
            this.adder = new RowAdder(this);
            this.adder.prepareRowAddition();		
        }
        slide();
    }

    @FXML
    private void onRowAdd(ActionEvent event){
        if(adder != null){
            if(adder.addRow()){
                final FileTab tab = currentlySelectedTab();
                final int rows = tab.getDataFrame().rows();
                labelRows.setText(String.format("%,d", rows));
                tab.setSaved(false);
                setSaveButtonsDisabled(false);
                setStatsMenuItemsDisabled(tab.getDataFrame());
            }
        }
    }

    @FXML
    private void onRowClose(ActionEvent event){
        if(sliderIsUp){
            slide();
        }
    }

    @FXML
    private void onEditAddColumn(ActionEvent event){
        if(sliderIsUp){
            slide();
        }
        final FileTab tab = currentlySelectedTab();
        final DataFrameView view = tab.getView();
        final DataFrame df = view.getDataFrame();
        final AddColumnDialog dialog = new AddColumnDialog(rootPane, df);
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setAddListener((name, col) -> {
            df.addColumn(name, col);
            view.reload();
            dialog.close();
            labelCols.setText(String.format("%,d", df.columns()));
            setSaveButtonsDisabled(false);
            if(df.columns() > 0){
                //enable when adding first col to uninitialized df
                setEditMenuItemsDisabled(false);
                setStatsMenuItemsDisabled(df);
            }
            tab.setSaved(false);
        });
        dialog.show();
    }

    @FXML
    private void onEditSort(ActionEvent event){
        if(sliderIsUp){
            slide();
        }
        final FileTab tab = currentlySelectedTab();
        final SortDialog dialog = new SortDialog(rootPane);
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setColumns(tab.getDataFrame().getColumnNames());
        dialog.setOnSort((column) -> {
            sortColumn(tab.getView(), column);
            dialog.close();
            setSaveButtonsDisabled(false);
            tab.setSaved(false);
        });
        dialog.show();
    }

    @FXML
    private void onEditFilter(ActionEvent event){
        if(sliderIsUp){
            slide();
        }
        final FileTab tab = currentlySelectedTab();
        final FilterDialog dialog = new FilterDialog(rootPane);
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setColumns(tab.getDataFrame().getColumnNames());
        dialog.setOnFilter((String column, String regex, boolean openTab) -> {
            final DataFrame filtered = tab.getDataFrame().filter(column, regex);
            if(filtered.isEmpty()){
                showSnackbar("Nothing found that matches your filter term");
            }else{
                if(openTab){
                    final FileTab newTab = new FileTab(null, filtered);
                    newTab.setText(Files.DEFAULT_NEW_FILENAME);
                    newTab.getView().addEditListener(this);
                    newTab.setOnCloseRequest((e) -> setTabCloseBehaviour(e, newTab));
                    newTab.setSaved(false);
                    mainTabs.getTabs().add(newTab);
                    dialog.close();
                    mainTabs.getSelectionModel().select(newTab);
                }else{
                    tab.replaceWith(filtered);
                    tab.setSaved(false);
                    setSaveButtonsDisabled(false);
                    labelRows.setText(String.format("%,d", filtered.rows()));
                    dialog.close();
                }
            }
        });
        dialog.show();
    }

    @FXML
    private void onEditConvert(ActionEvent event){
        if(sliderIsUp){
            slide();
        }
        final FileTab tab = currentlySelectedTab();
        DataFrame df = tab.getDataFrame();
        final String targetType = (df.isNullable() ? "DefaultDataFrame" : "NullableDataFrame");
        final ConfirmationDialog dialog = new ConfirmationDialog(rootPane, 
                "Convert?", "Are you sure that you want to convert this DataFrame to a "
                + targetType + "?", "Convert");
        
        dialog.setBackgroundEffect(mainBorderPane, Dialogs.getBackgroundBlur());
        dialog.setOnConfirm(() -> {
            dialog.close();
            final DataFrame converted = convert(df);
            tab.replaceWith(converted);
            tab.setSaved(false);
            setSaveButtonsDisabled(false);
            labelType.setText(converted.getClass().getSimpleName());
        });
        dialog.show();
    }

    @FXML
    private void onPreferences(ActionEvent event){
        startActivity(Activity.PREFERENCES);
    }

    @FXML
    private void onPlotPieChart(ActionEvent event){
        final FileTab tab = currentlySelectedTab();
        final ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_EDITORFILE, tab.getFile());
        bundle.addArgument(Const.BUNDLE_KEY_DATAFRAME, tab.getDataFrame());
        startActivity(Activity.PIE_CHART, bundle);
    }

    @FXML
    private void onPlotLineChart(ActionEvent event){
        final FileTab tab = currentlySelectedTab();
        final ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_EDITORFILE, tab.getFile());
        bundle.addArgument(Const.BUNDLE_KEY_DATAFRAME, tab.getDataFrame());
        startActivity(Activity.LINE_CHART, bundle);
    }

    @FXML
    private void onPlotAreaChart(ActionEvent event){
        final FileTab tab = currentlySelectedTab();
        final ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_EDITORFILE, tab.getFile());
        bundle.addArgument(Const.BUNDLE_KEY_DATAFRAME, tab.getDataFrame());
        startActivity(Activity.AREA_CHART, bundle);
    }

    @FXML
    private void onPlotBarChart(ActionEvent event){
        final FileTab tab = currentlySelectedTab();
        final ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_EDITORFILE, tab.getFile());
        bundle.addArgument(Const.BUNDLE_KEY_DATAFRAME, tab.getDataFrame());
        startActivity(Activity.BAR_CHART, bundle);
    }

    @FXML
    private void onFeedback(ActionEvent event){
        startActivity(Activity.FEEDBACK);
    }

    @FXML
    private void onAbout(ActionEvent event){
        startActivity(Activity.ABOUT);
    }

}
