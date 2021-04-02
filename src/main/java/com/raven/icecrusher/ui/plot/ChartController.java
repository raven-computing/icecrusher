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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.raven.common.struct.DataFrame;
import com.raven.icecrusher.application.Cache;
import com.raven.icecrusher.application.Controller;
import com.raven.icecrusher.io.Files;
import com.raven.icecrusher.ui.dialog.Dialogs;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.EditorFile;
import com.raven.icecrusher.util.ExceptionHandler;
import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.control.ComboBox;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;

import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

/**
 * Base Controller class for all Chart Activities. It provides functions for common
 * features and controls in all such activities. This includes:<br>
 * 
 * <p>The standard chart pane that holds the concrete chart implementation. 
 * Every chart has a title, a legend and a chart content. Both the title and the 
 * legend can change their position around the chart content or be disabled.
 * This class takes care of the internal logic for the combo boxes that let the 
 * user adjust the title and legend position.<br>
 * 
 * <p>A text field for changing the chart title. Changes are displayed in real-time.<br>
 * 
 * <p>A button for showing the prepared plot.
 * 
 * <p>A list of settings views for every chart item (like series or slice) to allow the 
 * user to change specific optics of each item. This is implemented as a SettingsListView
 * which is a custom VBox.
 * 
 *
 */
public abstract class ChartController extends Controller {

    protected static final double CHART_PANE_HEIGHT_OFFSET = 250.0;

    //computation is index based. Do not change order of items
    protected static ObservableList<String> optionsTitle = FXCollections.observableArrayList(
            "Top", "Bottom", "Left", "Right", "Disabled");

    //computation is index based. Do not change order of items
    protected static ObservableList<String> optionsLegend = FXCollections.observableArrayList(
            "Top", "Bottom", "Left", "Right", "Disabled");

    @FXML
    protected JFXTextField txtChartTitle;

    @FXML
    protected JFXComboBox<String> cbTitlePosition;

    @FXML
    protected JFXComboBox<String> cbLegendPosition;

    @FXML
    protected JFXButton btnPlotExport;

    @FXML
    protected SettingsListView settingsList;

    @FXML
    protected AnchorPane chartPane;

    protected DataFrame df;

    protected boolean plotIsShown;
    protected boolean titleDisabled;

    private Chart chart;

    public ChartController(){
        super();
    }

    /**
     * Called when the user fires the "plot" button. Implementations of this method
     * should do whatever it takes to actually show the plot to the user
     * 
     * @param event The <code>ActionEvent</code> object of the event fired
     */
    public abstract void onPlot(ActionEvent event);

    /**
     * Concrete implementations have to implement this method and call the super-method
     * and pass the concrete <code>Chart</code> instance to it
     */
    public abstract void initialize();

    /**
     * Initialization method for the <code>ChartController</code> class
     * 
     * @param chart The <code>Chart</code> instance used by the concrete chart implementation
     */
    public void initialize(final Chart chart){
        this.chart = chart;
        //ComboBoxes
        this.cbTitlePosition.setItems(optionsTitle);
        this.cbLegendPosition.setItems(optionsLegend);
        //set listeners
        this.cbTitlePosition.getSelectionModel().selectedIndexProperty().addListener(
                (ov, oldValue, newValue) -> {

                    changeTitlePosition(newValue);
                });

        this.cbLegendPosition.getSelectionModel().selectedIndexProperty().addListener(
                (ov, oldValue, newValue) -> {

                    changeLegendPosition(newValue);
                });
        //restore previous state
        selectOptionFromConfig(cbTitlePosition, CONFIG_TITLE_POSITION, optionsTitle);
        selectOptionFromConfig(cbLegendPosition, CONFIG_LEGEND_POSITION, optionsLegend);
    }

    @Override
    public void onStart(ArgumentBundle bundle){
        this.df = (DataFrame) bundle.getArgument(Const.BUNDLE_KEY_DATAFRAME);
        final EditorFile file = (EditorFile)bundle.getArgument(Const.BUNDLE_KEY_EDITORFILE);
        final String fileName = (file != null ? file.getName() : Files.DEFAULT_NEW_FILENAME);
        final String chartTitle = Cache.session()
                .get("ChartController.chart.title." + fileName, fileName);
        
        this.txtChartTitle.setText(chartTitle);
        
        this.txtChartTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            Cache.session().set("ChartController.chart.title." + fileName, newValue);
            if(!titleDisabled){
                chart.setTitle(newValue);
            }
        });
        if(!titleDisabled){
            this.chart.setTitle(chartTitle);
        }
        chartPane.setPrefHeight(getStage().getHeight()-CHART_PANE_HEIGHT_OFFSET);
    }

    @Override
    public boolean onExitRequested(){
        final ArgumentBundle bundle = new ArgumentBundle();
        bundle.addArgument(Const.BUNDLE_KEY_EXIT_REQUESTED, true);
        finishActivity(bundle);
        return false;
    }

    @Override
    public void onWindowResized(double width, double height){
        chartPane.setPrefHeight(height-CHART_PANE_HEIGHT_OFFSET);
    }

    /**
     * Called when the user wishes to close the activity. This implementation
     * simply finishes the current activity. Concrete classes may override this method
     * to add custom close behaviour
     * 
     * @param event The <code>ActionEvent</code> object of the event fired
     */
    protected void onClose(ActionEvent event){
        super.finishActivity();
    }

    /**
     * Opens a standard file dialog to prompt the user to export and save a snapshot of the
     * current chart to a PNG-file. The writing of the file will be performed on a 
     * background thread
     */
    protected void exportSnapshot(){
        File file = Dialogs.showPlotExportDialog(getStage());
        if(file != null){
            //use default parameters
            final WritableImage img = chart.snapshot(null, null);
            if(!file.getName().toLowerCase().endsWith(".png")){
                file = addPNGFileExtension(file);
            }
            final File f = file;
            new Thread(() -> {
                try{
                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", f);
                }catch(IOException ex){
                    Platform.runLater(() -> {
                        ExceptionHandler.showDialog(ex);
                    });
                }
            }).start();
        }
    }

    /**
     * Changes the position of the chart legend to the position mapped to the given index
     * 
     * @param index The index of the selected item in the combo box
     */
    protected void changeLegendPosition(final Number index){
        final int i = (int) index;
        switch(i){
        case 0://TOP
            this.chart.setLegendSide(Side.TOP);
            this.chart.setLegendVisible(true);
            break;
        case 1://BOTTOM
            this.chart.setLegendSide(Side.BOTTOM);
            this.chart.setLegendVisible(true);
            break;
        case 2://LEFT
            this.chart.setLegendSide(Side.LEFT);
            this.chart.setLegendVisible(true);
            break;
        case 3://RIGHT
            this.chart.setLegendSide(Side.RIGHT);
            this.chart.setLegendVisible(true);
            break;
        case 4://NO LEGEND
            this.chart.setLegendVisible(false);
            break;
        }
        getConfiguration().set(PLOT, CONFIG_LEGEND_POSITION, optionsLegend.get(i));
    }

    /**
     * Changes the position of the chart title to the position mapped to the given index
     * 
     * @param index The index of the selected item in the combo box
     */
    protected void changeTitlePosition(final Number index){
        final int i = (int) index;
        switch(i){
        case 0://TOP
            this.chart.setTitleSide(Side.TOP);
            this.chart.setTitle(txtChartTitle.getText());
            this.titleDisabled = false;
            break;
        case 1://BOTTOM
            this.chart.setTitleSide(Side.BOTTOM);
            this.chart.setTitle(txtChartTitle.getText());
            this.titleDisabled = false;
            break;
        case 2://LEFT
            this.chart.setTitleSide(Side.LEFT);
            this.chart.setTitle(txtChartTitle.getText());
            this.titleDisabled = false;
            break;
        case 3://RIGHT
            this.chart.setTitleSide(Side.RIGHT);
            this.chart.setTitle(txtChartTitle.getText());
            this.titleDisabled = false;
            break;
        case 4://NO TITLE
            this.chart.setTitle(null);
            this.titleDisabled = true;
            break;
        }
        getConfiguration().set(PLOT, CONFIG_TITLE_POSITION, optionsTitle.get(i));
    }
    
    /**
     * Selects an option in a ComboBox from the saved configuration state.
     * This method checks that the specified key is set correctly and that it is
     * contained in the list of valid options of the specified ComboBox. If the option
     * cannot be set, then this method selects the first item
     * in the provided ComboBox as a fallback
     * 
     * @param comboBox The <code>ComboBox</code> to use for selection
     * @param configKey The configuration key to use for querying the config
     *                  state form the PLOT section
     * @param options The list of valid options of the specified ComboBox
     */
    protected void selectOptionFromConfig(final ComboBox<String> comboBox,
            final String configKey, final List<String> options){
        
        final String option = getConfiguration().valueOf(PLOT, configKey);
        if((option != null) && !option.isEmpty() && options.contains(option)){
            comboBox.getSelectionModel().select(option);
        }else{//fallback
            comboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Updates the symbol color of the legend that corresponds to the given SettingsView instance
     * 
     * @param view The <code>SettingsView</code> instance that maps to the legend to adjust
     * @param color The color to apply to the chart legend symbol
     */
    protected void updateLegendColor(final SettingsView view, final String color){
        final ObservableList<LegendItem> items = getChartLegend().getItems();
        if(view.getIndex() < items.size()){
            items.get(view.getIndex()).getSymbol().setStyle("-fx-background-color: " + color);
        }
    }

    /**
     * Updates the color of all chart legend symbols according to the current color set in 
     * the corresponding SettingsView object
     */
    protected void updateAllLegendColors(){
        if(!plotIsShown){
            return;
        }
        final ObservableList<LegendItem> items = getChartLegend().getItems();
        final ObservableList<Node> views = settingsList.getChildren();
        if(items.size() == views.size()){
            for(int i=0; i<items.size(); ++i){
                items.get(i).getSymbol().setStyle("-fx-background-color: " 
                        + ((SettingsView)views.get(i)).getColor());
            }
        }
    }

    /**
     * Gets the chart legend of this chart
     * 
     * @return The <code>Legend</code> instance of this chart
     */
    protected Legend getChartLegend(){
        for(final Node node : chart.getChildrenUnmodifiable()){
            if(node instanceof Legend){
                return (Legend)node;
            }
        }
        return null;
    }

    /**
     * Adds the specified SettingsView to the <code>SettingsListView</code> control
     * 
     * @param view The <code>SettingsView</code> to add
     */
    protected void addSettingsViewToList(final SettingsView view){
        this.settingsList.addSettingsView(view);
    }

    /**
     * Adds all specified SettingsViews to the <code>SettingsListView</code> control
     * 
     * @param views The List of <code>SettingsViews</code> to add
     */
    protected void addAllSettingsViewsToList(final List<SettingsView> views){
        this.settingsList.addAllSettingsViews(views);
    }

    /**
     * Sets the <code>SettingsListView</code> control to all specified SettingsViews
     * 
     * @param views The List of <code>SettingsViews</code> to set
     */
    protected void setAllSettingsViewsInList(final List<SettingsView> views){
        this.settingsList.setAllSettingsViews(views);
    }

    /**
     * Removes the specified SettingsView from the <code>SettingsListView</code> control
     * 
     * @param view The <code>SettingsView</code> to remove
     */
    protected void removeSettingsViewFromList(final SettingsView view){
        this.settingsList.removeSettingsView(view);
    }

    /**
     * Resets the button for plot and export functionalities back to its default state
     */
    protected void resetPlotButton(){
        this.btnPlotExport.setText("Plot");
        this.btnPlotExport.setDisable(true);
    }

    private File addPNGFileExtension(final File file){
        return new File(file.getAbsolutePath() + ".png");
    }
}
