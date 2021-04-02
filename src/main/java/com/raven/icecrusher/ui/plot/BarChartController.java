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

import java.text.DecimalFormat;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.raven.common.struct.Column;
import com.raven.common.struct.Item;
import com.raven.common.struct.WritableItem;
import com.raven.icecrusher.application.Cache;
import com.raven.icecrusher.ui.OneShotSnackbar;
import com.raven.icecrusher.ui.plot.SettingsView.ViewListener;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static com.raven.icecrusher.util.EditorConfiguration.CONFIG_BACKGROUND_WHITE;
import static com.raven.icecrusher.util.EditorConfiguration.CONFIG_GRID_VISIBLE;
import static com.raven.icecrusher.util.EditorConfiguration.CONFIG_BARCHART_MODE;
import static com.raven.icecrusher.util.EditorConfiguration.getConfiguration;
import static com.raven.icecrusher.util.EditorConfiguration.Section.PLOT;

/**
 * Controller class for the Bar Chart activity.
 *
 */
public class BarChartController extends ChartController {

    protected static final String STYLE_PLOT_BACKGROUND_WHITE = "-fx-background-color: white";
    protected static final String STYLE_PLOTBACKGROUND_DARK = "-fx-background-color: #383838";
    protected static final String STYLE_GRID_LINES_WHITE = "-fx-stroke: derive(white, -10%)";
    protected static final String STYLE_GRID_LINES_DARK = "-fx-stroke: derive(white, -70%)";

    /**
     * Maximum amount of items on the x-axis
     */
    protected static final int THRESHOLD_MAX_X_VALUES = 50;

    //computation is index based. Do not change order of items
    private static final ObservableList<String> OPTIONS_Y_MODE = FXCollections
            .observableArrayList(
            "Sum", "Minimum", "Maximum", "Average", "Single");

    @FXML
    protected JFXComboBox<String> cbColumnX;

    @FXML
    protected JFXComboBox<String> cbColumnY;

    @FXML
    protected JFXTextField txtAxisXLabel;

    @FXML
    protected JFXTextField txtAxisYLabel;

    @FXML
    protected JFXComboBox<String> cbYMode;

    @FXML
    protected JFXCheckBox checkShowGrid;

    @FXML
    protected JFXCheckBox checkWhiteBackground;

    @FXML
    protected JFXButton btnYAdd;

    @FXML
    protected CategoryAxis chartXAxis;

    @FXML
    protected NumberAxis chartYAxis;

    @FXML
    private BarChart<String, Number> chart;

    private List<Series<String, Number>> preparedSeries;
    private List<BarDescriptor> usedBars;
    private List<Item<String>> xAxisLabels;
    private DecimalFormat barFormat;

    private int seriesNumber = 1;

    public BarChartController(){
        super();
        this.usedBars = new ArrayList<>();
        this.preparedSeries = new ArrayList<>();
        this.xAxisLabels =  new ArrayList<>();
        this.barFormat = new DecimalFormat("##.##");
    }

    @FXML
    @Override
    public void initialize(){
        super.initialize(this.chart);
        this.settingsList.setAnimateResetAction(true);
        //ComboBoxes
        this.cbYMode.setItems(OPTIONS_Y_MODE);
        this.cbYMode.getSelectionModel().selectFirst();//default is Sum
        selectOptionFromConfig(cbYMode, CONFIG_BARCHART_MODE, OPTIONS_Y_MODE);
        this.cbYMode.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {
                    
                    getConfiguration().set(PLOT, CONFIG_BARCHART_MODE, newValue);
                });
        //CheckBoxes
        this.checkShowGrid.selectedProperty().addListener((ov, oldValue, newValue) -> {
            chart.setHorizontalGridLinesVisible(newValue);
            chart.setVerticalGridLinesVisible(newValue);
            getConfiguration().set(PLOT, CONFIG_GRID_VISIBLE, newValue);
        });
        this.checkShowGrid.setSelected(getConfiguration().booleanOf(PLOT, CONFIG_GRID_VISIBLE));
        this.checkWhiteBackground.selectedProperty().addListener(
                (ov, oldValue, newValue) -> {
                    
            final Node nBack = chart.lookup(".chart-plot-background");
            final Node nLinesVer = chart.lookup(".chart-vertical-grid-lines");
            final Node nLinesHor = chart.lookup(".chart-horizontal-grid-lines");
            if((nBack != null) && (nLinesVer != null) && (nLinesHor != null)){
                nBack.setStyle(newValue ? STYLE_PLOT_BACKGROUND_WHITE : STYLE_PLOTBACKGROUND_DARK);
                nLinesVer.setStyle(newValue ? STYLE_GRID_LINES_WHITE : STYLE_GRID_LINES_DARK);
                nLinesHor.setStyle(newValue ? STYLE_GRID_LINES_WHITE : STYLE_GRID_LINES_DARK);
            }
            getConfiguration().set(PLOT, CONFIG_BACKGROUND_WHITE, newValue);
        });
        this.checkWhiteBackground.setSelected(getConfiguration().booleanOf(PLOT, CONFIG_BACKGROUND_WHITE));
    }

    @Override
    public void onStart(ArgumentBundle bundle){
        super.onStart(bundle);
        //flush the DataFrame to make sure that counting
        //and cloning does not result in deviating sizes
        this.df.flush();
        this.cbColumnX.getItems().addAll(df.getColumnNames());
        this.cbColumnX.getSelectionModel().selectedItemProperty()
                   .addListener((ov, oldValue, newValue) -> {
                       
            columnSelectionChanged();
            reset();
            if(newValue != null){
                final String text = txtAxisXLabel.getText();
                if((text == null) || text.isEmpty()){
                    txtAxisXLabel.setText(Cache.session()
                            .get("ChartController.chart.xaxis.label." + newValue));
                    
                }else{
                    Cache.session().set("ChartController.chart.xaxis.label." + newValue, text);
                }
            }
        });
        this.cbColumnY.getItems().addAll(df.getColumnNames());
        this.cbColumnY.getSelectionModel().selectedItemProperty()
                   .addListener((ov, oldValue, newValue) -> {
                       
            columnSelectionChanged();
            if(newValue != null){
                final String text = txtAxisYLabel.getText();
                if((text == null) || text.isEmpty()){
                    txtAxisYLabel.setText(Cache.session()
                            .get("ChartController.chart.yaxis.label." + newValue));
                    
                }else{
                    Cache.session().set("ChartController.chart.yaxis.label." + newValue, text);
                }
            }
        });
        this.txtAxisXLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            chartXAxis.setLabel(newValue);
            final String axis = cbColumnX.getValue();
            if(axis != null){
                Cache.session().set("ChartController.chart.xaxis.label." + axis, newValue);
            }
        });
        this.txtAxisYLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            chartYAxis.setLabel(newValue);
            final String axis = cbColumnY.getValue();
            if(axis != null){
                Cache.session().set("ChartController.chart.yaxis.label." + axis, newValue);
            }
        });
        this.chart.needsLayoutProperty().addListener((ov, oldValue, newValue) -> {
            if(plotIsShown){
                addMouseEventsForXAxis();
            }
        });
    }

    @FXML
    @Override
    public void onPlot(ActionEvent event){
        if(!plotIsShown || (!preparedSeries.isEmpty())){
            this.chartXAxis.setAnimated(false);
            this.chart.getData().addAll(preparedSeries);
            this.plotIsShown = true;
            this.btnPlotExport.setText("Export as PNG");
            for(final Series<String, Number> series : preparedSeries){
                final String color = getSettingsViewForSeries(series).getColor();
                updateBarColor(series, color);
            }
            updateAllLegendColors();
            //add hover actions for each added bar
            for(final Series<String, Number> series : preparedSeries){
                for(final XYChart.Data<String, Number> data : series.getData()){
                    addHoverListenerFor(data);
                }
            }
            this.preparedSeries.clear();
            addMouseEventsForXAxis();

        }else{
            exportSnapshot();
        }
    }

    @FXML
    public void onAdd(ActionEvent event){
        prepareChart();
    }

    @FXML
    protected void onClose(ActionEvent event){
        super.onClose(event);
    }

    /**
     * Updates the color of all bars in a particular series
     * 
     * @param series The <code>XYChart.Series</code> of the bars to change
     * @param color The new color of all bars within the provided series
     */
    private void updateBarColor(final XYChart.Series<String, Number> series,
            final String color){
        
        for(final XYChart.Data<String, Number> data : series.getData()){
            if(data.getNode() == null){
                break;
            }
            data.getNode().setStyle("-fx-bar-fill: " + color);
        }
    }

    /**
     * This method should be called whenever any column selection changes
     */
    private void columnSelectionChanged(){
        if((cbColumnX.getSelectionModel().getSelectedIndex() != -1) 
                && (cbColumnY.getSelectionModel().getSelectedIndex() != -1)){

            btnYAdd.setDisable(false);
        }
    }

    /**
     * Returns the column object that corresponds to the selected
     * column in the x-axis combo box
     * 
     * @return The <code>Column</code> selected by the user
     */
    private Column selectedXColumn(){
        return df.getColumn(cbColumnX.getSelectionModel().getSelectedItem());
    }

    /**
     * Returns the column object that corresponds to the selected
     * column in the y-axis combo box
     * 
     * @return The <code>Column</code> selected by the user
     */
    private Column selectedYColumn(){
        return df.getColumn(cbColumnY.getSelectionModel().getSelectedItem());
    }

    /**
     * Gets the <code>SettingsView</code> that corresponds to the given series
     * 
     * @param series The series which maps to the returned SettingsView
     * @return The <code>SettingsView</code> for the specified series
     */
    private SettingsView getSettingsViewForSeries(final Series<String, Number> series){
        return ((SettingsView)settingsList.getChildren().get(chart.getData().indexOf(series)));
    }

    /**
     * Resets all settings to the default values and removes all chart series data
     */
    private void reset(){
        this.chart.getData().clear();
        this.settingsList.resetSettingsList();
        this.usedBars.clear();
        this.preparedSeries.clear();
        this.xAxisLabels.clear();
        this.plotIsShown = false;
        resetPlotButton();
    }

    /**
     * Removes the series mapped by the given SettingsView from both the settings list
     * as well as the chart content and any internal data structures that may hold a
     * reference to that series. Performs cleanup operations
     * 
     * @param view The <code>SettingsView</code> of the series to remove
     */
    private void removeSeries(final SettingsView view){
        final int i = view.getIndex();
        //remove the SettingsView item from the list
        //indices get updated by the list
        removeSettingsViewFromList(view);
        //remove it from the used bars list
        usedBars.remove(i);
        //offset between the index of the item in the settings list
        //and the number of series already plotted on the screen
        final int offset = (i-chart.getData().size());
        //remove it from the list of precomputed series
        //if it is not plotted
        if(!preparedSeries.isEmpty() && (offset >= 0)){
            preparedSeries.remove(offset);
            if(preparedSeries.isEmpty()){//cleanup
                preparedSeries.clear();
                if(chart.getData().isEmpty()){
                    resetPlotButton();
                    plotIsShown = false;
                }else{
                    btnPlotExport.setText("Export as PNG");
                }
            }
        }
        //series is already plotted on the screen 
        //remove it from the chart 
        if(offset < 0){
            chart.getData().remove(i);
            if(chart.getData().isEmpty()){//cleanup
                resetPlotButton();
                plotIsShown = false;
            }else{
                updateAllLegendColors();
            }
        }
    }

    private void prepareChart(){
        final Column colX = selectedXColumn();
        final Column colY = selectedYColumn();

        if((chart.getData().size() + preparedSeries.size()+1) > 8){
            OneShotSnackbar.showFor(getRootNode(),
                    "The maximum number of series has been reached");
            
            return ;
        }
        final Series<String, Number> series = prepareData(colX, colY);
        if(series == null){
            return;
        }
        final int index = this.settingsList.getChildren().size();
        final SettingsView sv = new BarSettingsView(
                index,
                colY.getName(),
                cbYMode.getSelectionModel().getSelectedItem());
        
        series.setName(sv.getEditText());
        sv.setViewListener(new ViewListener(){
            @Override
            public void onRelabel(SettingsView view, String newLabel){
                series.setName(newLabel);
                updateAllLegendColors();
            }
            @Override
            public void onColorChanged(SettingsView view, String newColor){
                if(plotIsShown){
                    updateBarColor(series, newColor);
                    updateLegendColor(view, newColor);
                }
            }
            @Override
            public void onRemove(SettingsView view){
                //an animation may still be in process
                if(!settingsList.isRemoving()){
                    removeSeries(view);
                }
            }
        });
        addSettingsViewToList(sv);
        this.preparedSeries.add(series);
        this.btnPlotExport.setDisable(false);
        this.btnPlotExport.setText("Plot");
    }

    private Series<String, Number> prepareData(final Column colX, final Column colY){
        final String yColName = df.getColumnName(
                cbColumnY.getSelectionModel().getSelectedIndex());
        
        final int yColMode = cbYMode.getSelectionModel().getSelectedIndex();
        final BarDescriptor bar = BarDescriptor.from(yColName, yColMode);
        if(usedBars.contains(bar)){
            OneShotSnackbar.showFor(getRootNode(),
                    "This data series has already been added");
            
            return null;
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Series " + (seriesNumber++));

        Map<Object, Double> map = null;
        try{
            map = createMapping(colX, colY,
                    cbYMode.getSelectionModel().getSelectedIndex());
            
        }catch(RuntimeException ex){
            if(ex instanceof NumberFormatException){
                OneShotSnackbar.showFor(getRootNode(), "Only numbers are allowed for the y-Axis");
            }else if(ex instanceof ClassCastException){
                OneShotSnackbar.showFor(getRootNode(), "A column contains unsupported types");
            }else{
                OneShotSnackbar.showFor(getRootNode(), ex.getMessage());
            }
            return null;
        }
        if(map.isEmpty()){
            OneShotSnackbar.showFor(getRootNode(),
                    "No valid data points for selected columns");
            
            return null;
        }
        final ObservableList<Data<String, Number>> data = series.getData();

        final boolean isEmpty = xAxisLabels.isEmpty(); 
        for(final Map.Entry<Object, Double> e : map.entrySet()){
            data.add(new XYChart.Data<>(e.getKey().toString(), e.getValue()));
            if(isEmpty){
                final String key = e.getKey().toString();
                xAxisLabels.add(new WritableItem<String>(key,
                        Cache.session().get("BarChart.xaxislabel.text." + key, key)));
            }
        }
        usedBars.add(bar);

        return series;
    }

    private Map<Object, Double> createMapping(final Column colX, final Column colY,
            final int opMode) throws RuntimeException{

        BarOperator operator = null;
        switch(opMode){
        case 0://SUM
            operator = BarOperator.sumMode(colX, colY);
            break;
        case 1://MINIMUM
            operator = BarOperator.minimumMode(colX, colY);
            break;
        case 2://MAXIMUM
            operator = BarOperator.maximumMode(colX, colY);
            break;
        case 3://AVERAGE
            operator = BarOperator.averageMode(colX, colY);
            break;
        case 4://SINGLE
            operator = BarOperator.singleMode(colX, colY);
            break;
        default:
            throw new IllegalArgumentException("Unknown operation mode: " + opMode);
        }
        for(int i=0; i<df.rows(); ++i){
            operator.operate(i);
        }
        return operator.finish();
    }

    private void addHoverListenerFor(final XYChart.Data<String, Number> data){
        final Text text = new Text(barFormat.format(data.getYValue()));
        data.getNode().hoverProperty().addListener((ov, oldValue, newValue) -> {
            if(!plotIsShown){
                return;
            }
            Platform.runLater(() -> {
                final Node node = data.getNode();
                text.setFill((checkWhiteBackground.isSelected()
                        ? Color.BLACK
                        : Color.WHITE));
                
                final Group group = ((Group)node.getParent());
                if(group != null){
                    if(newValue){
                        final Bounds bounds = node.getBoundsInParent();
                        text.setLayoutX(Math.round(bounds.getMinX() 
                                + bounds.getWidth()/2 
                                - text.prefWidth(-1)/2));

                        text.setLayoutY(Math.round(bounds.getMinY() 
                                - text.prefHeight(-1)*0.5));

                        group.getChildren().add(text);
                    }else{
                        group.getChildren().remove(text);
                    }
                }
            });
        });
    }

    private void addMouseEventsForXAxis(){
        int i = 0;
        for(final Node node : chartXAxis.getChildrenUnmodifiable()){
            if(node instanceof Text){
                //set the text fom memory
                ((Text)node).setText(xAxisLabels.get(i).getValue());
                final TextField text =  new TextField(((Text)node).getText());
                text.setId("bar-chart-edit-text");//CSS ID
                text.setPrefWidth(173.0);
                text.setMaxWidth(200.0);//prevents unusable scene for rogue user input
                text.setPrefHeight(28.0);
                text.setAlignment(Pos.CENTER);
                final int indexChange = i;
                node.setOnMouseEntered((e) -> {
                    //ignore if it is already part of the scene graph
                    if(!chartPane.getChildren().contains(text)){
                        final Bounds bounds = chartPane.sceneToLocal(
                                node.localToScene(node.getBoundsInLocal()));

                        text.setPrefWidth(bounds.getWidth()+50);
                        //compute the center point of the Text control showing the label
                        final double centerOfTextX = (bounds.getMinX() + (bounds.getWidth()/2));
                        final double centerOfTextY = (bounds.getMinY() + (bounds.getHeight()/2));
                        //locate the TextField in a way that the center point is equal to the center point
                        //of the overlayed Text control while keeping the max width in mind
                        text.relocate(
                                (centerOfTextX - (text.getPrefWidth()/2)), 
                                (centerOfTextY - (text.getPrefHeight()/2)));

                        text.setOnMouseExited((event) -> {
                            chartPane.getChildren().remove(text);
                        });
                        text.setOnAction((event) -> {
                            if(!text.getText().isEmpty()){
                                text.setOnMouseExited(null);
                                ((Text)node).setText(text.getText());
                                //update internal memory
                                xAxisLabels.get(indexChange).setValue(text.getText());
                                Cache.session().set(
                                        "BarChart.xaxislabel.text."
                                           + xAxisLabels.get(indexChange).getKey(),
                                        text.getText());
                                
                                chartPane.getChildren().remove(text);
                                ((Pane)chart.getChildrenUnmodifiable().get(1)).requestLayout();
                            }else{
                                OneShotSnackbar.showFor(getRootNode(), "Bar label cannot be empty");
                            }
                        });
                        chartPane.getChildren().add(text);
                    }
                });
                ++i;
            }
        }
    }

}
