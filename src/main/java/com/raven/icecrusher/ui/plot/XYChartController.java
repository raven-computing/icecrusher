/* 
 * Copyright (C) 2020 Raven Computing
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

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.raven.common.struct.ByteColumn;
import com.raven.common.struct.Column;
import com.raven.common.struct.DataFrame;
import com.raven.common.struct.DataFrameException;
import com.raven.common.struct.DefaultDataFrame;
import com.raven.common.struct.DoubleColumn;
import com.raven.common.struct.FloatColumn;
import com.raven.common.struct.IntColumn;
import com.raven.common.struct.LongColumn;
import com.raven.common.struct.NullableByteColumn;
import com.raven.common.struct.NullableDoubleColumn;
import com.raven.common.struct.NullableFloatColumn;
import com.raven.common.struct.NullableIntColumn;
import com.raven.common.struct.NullableLongColumn;
import com.raven.common.struct.NullableShortColumn;
import com.raven.common.struct.ShortColumn;
import com.raven.icecrusher.application.Cache;
import com.raven.icecrusher.io.DataFrames;
import com.raven.icecrusher.ui.OneShotSnackbar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

/**
 * Base Controller class for Line- and Area Chart Activities. 
 * It provides functions for common features and controls in such activities.<br>
 * Both activities have the following features in common:<br>
 * 
 * <p>Two combo boxes to select the column for the x-axis and the column for the y-axis.
 * 
 * <p>Two text fields to change both axis labels as shown on the chart.
 * 
 * <p>A check box and a combo box to signal that the column set for the x-axis is a date 
 * column. The combo box will allow the user to specify the date format to be used.
 * 
 * <p>Check boxes to allow the user to change the chart content background and toggle 
 * the displaying of data points and grid lines.
 * 
 * <p>A button for adding the selected x-axis y-axis combination to the settings list.
 * 
 * <p>This class also provides a method to compute a <code>XYChart.Series</code> object
 * from two given columns. Both date-number as well as number-number combinations are 
 * supported. The date column must always be on the x-axis.
 *
 */
public abstract class XYChartController extends ChartController {

    /**
     * The maximum number of major ticks for an x-axis representing dates
     */
    protected static final int DATE_AXIS_THRESHOLD_TICKS = 50;

    protected static final String STYLE_PLOT_BACKGROUND_WHITE = "-fx-background-color: white";
    protected static final String STYLE_PLOTBACKGROUND_DARK = "-fx-background-color: #383838";
    protected static final String STYLE_GRID_LINES_WHITE = "-fx-stroke: derive(white, -10%)";
    protected static final String STYLE_GRID_LINES_DARK = "-fx-stroke: derive(white, -70%)";

    /**
     * The date format used internally to sort date-value mappings
     */
    protected static final DateTimeFormatter DATE_FORMATTER_ENCODED = 
            DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);

    /**
     * The date format of all ticks shown to the user inside the chart content
     */
    protected static final DateTimeFormatter DATE_FORMATTER_TICKS = 
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    /**
     * All options shown to the user in the date format combo box
     */
    protected static final ObservableList<String> OPTIONS_DATE_FORMAT = 
            FXCollections.observableArrayList(
                    "yyyy-MM-dd", "yyyyMMdd", "yyyy.MM.dd",
                    "dd-MM-yyyy", "ddMMyyyy", "dd.MM.yyyy");

    @FXML
    protected JFXComboBox<String> cbColumnX;

    @FXML
    protected JFXComboBox<String> cbColumnY;

    @FXML
    protected JFXTextField txtAxisXLabel;

    @FXML
    protected JFXTextField txtAxisYLabel;

    @FXML
    protected JFXCheckBox checkXIsDate;

    @FXML
    protected JFXCheckBox checkDataPoints;

    @FXML
    protected JFXCheckBox checkShowGrid;

    @FXML
    protected JFXCheckBox checkWhiteBackground;

    @FXML
    protected JFXComboBox<String> cbDateFormat;

    @FXML
    protected JFXButton btnYAdd;

    @FXML
    protected NumberAxis chartXAxis;

    @FXML
    protected NumberAxis chartYAxis;

    protected List<Series<Number, Number>> preparedSeries;
    protected List<XYChartData> usedDataList;

    protected int seriesNumber = 1;
    protected boolean xAxisIsDate;

    private XYChart<Number, Number> chart;

    public XYChartController(){
        super();
        //custom list behaviour
        this.usedDataList = new LinkedList<XYChartData>(){
            private static final long serialVersionUID = -3985848316534660143L;
            //override the contains() method to implement a custom behaviour.
            //This will call equals() on each list item instead on the argument
            @Override
            public boolean contains(Object o){
                final Iterator<XYChartData> iter = this.iterator();
                while(iter.hasNext()){
                    if(iter.next().equals(o)){
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Called when the user fires the "add" button
     * 
     * @param event The <code>ActionEvent</code> object of the event fired
     */
    protected abstract void onAdd(ActionEvent event);

    /**
     * Initialization method for the <code>XYChartController</code> class
     * 
     * @param chart The <code>XYChart</code> instance used by
     *              the concrete chart implementation
     */
    protected void initialize(final XYChart<Number, Number> chart){
        super.initialize(chart);
        this.chart = chart;
        this.settingsList.setAnimateResetAction(true);
        //ComboBoxes
        this.cbDateFormat.setItems(OPTIONS_DATE_FORMAT);
        this.cbDateFormat.setDisable(true);
        //CheckBoxes
        this.checkXIsDate.setSelected(false);
        this.checkXIsDate.selectedProperty().addListener(
                (ov, oldValue, newValue) -> {
                    
            xAxisIsDate = newValue;
            if(newValue){//is selected
                cbDateFormat.setDisable(false);
            }else{
                cbDateFormat.getSelectionModel().clearSelection();
                cbDateFormat.setDisable(true);
            }
        });
        this.checkDataPoints.setSelected(getConfiguration().booleanOf(PLOT, CONFIG_XYCHART_DATAPOINTS));
        this.checkDataPoints.selectedProperty().addListener(
                (ov, oldValue, newValue) -> {
                    
            getConfiguration().set(PLOT, CONFIG_XYCHART_DATAPOINTS, newValue);
        });
        this.checkShowGrid.selectedProperty().addListener(
                (ov, oldValue, newValue) -> {
                    
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
                nBack.setStyle(newValue
                        ? STYLE_PLOT_BACKGROUND_WHITE
                        : STYLE_PLOTBACKGROUND_DARK);
                
                nLinesVer.setStyle(newValue
                        ? STYLE_GRID_LINES_WHITE
                        : STYLE_GRID_LINES_DARK);
                
                nLinesHor.setStyle(newValue
                        ? STYLE_GRID_LINES_WHITE
                        : STYLE_GRID_LINES_DARK);
                
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
        this.cbColumnX.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {

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
        this.cbColumnY.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {
                    
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
        this.txtAxisXLabel.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    
            chartXAxis.setLabel(newValue);
            final String axis = cbColumnX.getValue();
            if(axis != null){
                Cache.session().set("ChartController.chart.xaxis.label." + axis, newValue);
            }
        });
        this.txtAxisYLabel.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    
            chartYAxis.setLabel(newValue);
            final String axis = cbColumnY.getValue();
            if(axis != null){
                Cache.session().set("ChartController.chart.yaxis.label." + axis, newValue);
            }
        });
    }

    /**
     * Gets a <code>DateTimeFormatter</code> based on the selected format of the combo box
     * 
     * @return A <code>DateTimeFormatter</code> for parsing dates according to
     *         the selected format
     */
    protected DateTimeFormatter getDateFormatterFromSelection(){
        return DateTimeFormatter.ofPattern(cbDateFormat
                .getSelectionModel().getSelectedItem(), Locale.ENGLISH);
    }

    /**
     * Gets the <code>SettingsView</code> that corresponds to the given series
     * 
     * @param series The series which maps to the returned SettingsView
     * @return The <code>SettingsView</code> for the specified series
     */
    protected SettingsView getSettingsViewForSeries(final Series<Number, Number> series){
        return ((SettingsView)settingsList.getChildren().get(chart.getData().indexOf(series)));
    }

    /**
     * This method should be called whenever any column selection changes
     */
    protected void columnSelectionChanged(){
        if((cbColumnX.getSelectionModel().getSelectedIndex() != -1) 
                && (cbColumnY.getSelectionModel().getSelectedIndex() != -1)){

            btnYAdd.setDisable(false);
        }
    }

    /**
     * Returns the column object that corresponds to the selected column
     * in the x-axis combo box
     * 
     * @return The <code>Column</code> selected by the user
     */
    protected Column selectedXColumn(){
        return df.getColumn(cbColumnX.getSelectionModel().getSelectedItem());
    }

    /**
     * Returns the column object that corresponds to the selected column
     * in the y-axis combo box
     * 
     * @return The <code>Column</code> selected by the user
     */
    protected Column selectedYColumn(){
        return df.getColumn(cbColumnY.getSelectionModel().getSelectedItem());
    }

    /**
     * Resets all settings to the default values and removes all chart series data
     */
    protected void reset(){
        chart.getData().clear();
        settingsList.resetSettingsList();
        usedDataList.clear();
        preparedSeries = null;
        chartXAxis.setTickLabelFormatter(null);
        chartXAxis.setAutoRanging(true);
        chartXAxis.setMinorTickVisible(true);
        plotIsShown = false;
        resetPlotButton();
        setDateControlsDisabled(false);
    }

    protected void setDateControlsDisabled(final boolean value){
        this.checkXIsDate.setDisable(value);
        this.cbDateFormat.setDisable(value);
    }

    protected void updateSymbolsColor(final ObservableList<Data<Number, Number>> data,
            final String color){
        
        for(final Data<Number, Number> dataPoint : data){
            final Node symbol = dataPoint.getNode();
            if(symbol != null){
                symbol.setStyle("-fx-background-color: " + color);
            }
        }
    }

    protected void updateAllSymbolsColor(final XYChart<Number, Number> chart){
        final ObservableList<Node> views = settingsList.getChildren();
        if(chart.getData().size() == views.size()){
            for(int i=0; i<chart.getData().size(); ++i){
                final String color = ((SettingsView)views.get(i)).getColor();
                for(final Data<Number, Number> dataPoint : chart.getData().get(i).getData()){
                    final Node symbol = dataPoint.getNode();
                    if(symbol != null){
                        symbol.setStyle("-fx-background-color: " + color);
                    }
                }
            }
        }
    }

    /**
     * Removes the series mapped by the given SettingsView from both the settings list
     * as well as the chart content and any internal data structures that may hold a
     * reference to that series
     * 
     * @param view The <code>SettingsView</code> of the series to remove
     */
    protected void removeSeries(final SettingsView view){
        final int i = view.getIndex();
        //remove the SettingsView item from the list
        //indices get updated by the list
        removeSettingsViewFromList(view);
        //remove it from the used data list
        usedDataList.remove(i);
        //offset between the index of the item in the settings list
        //and the number of series already plotted on the screen
        final int offset = (i-chart.getData().size());
        //remove it from the list of precomputed series
        //if it is not plotted
        if((preparedSeries != null) && (offset >= 0)){
            preparedSeries.remove(offset);
            if(preparedSeries.isEmpty()){//cleanup
                preparedSeries = null;
                if(chart.getData().isEmpty()){
                    resetPlotButton();
                    setDateControlsDisabled(false);
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
                setDateControlsDisabled(false);
                plotIsShown = false;
            }else{
                updateAllLegendColors();
            }
        }
    }

    /**
     * Precomputes a <code>XYChartData</code> object which holds a <code>XYChart.Series</code>. 
     * The x-axis values of the series will be taken from the column of the first argument while 
     * the y-axis values will be taken from the column of the second argument. This method will
     * distinguish between plain numerical data and time-based data. <br>
     * If any precondition is not met, then an info message is shown to the user as a Snackbar
     * 
     * @param colX The column to be used as the x-values
     * @param colY The column to be used as the y-values
     * @return A <code>XYChartData</code> object holding a <code>XYChart.Series</code> for the 
     *         given column data
     */
    @SuppressWarnings("unlikely-arg-type")
    protected XYChartData prepareData(final Column colX, final Column colY){
        //do various checks
        if(!colX.isNumeric() && !DataFrames.columnUsesStrings(colX)){
            OneShotSnackbar.showFor(getRootNode(),
                    "Only numbers and strings are allowed for the x-Axis");
            
            return null;
        }
        if(!colY.isNumeric()){
            OneShotSnackbar.showFor(getRootNode(),
                    "Only numbers are allowed for the y-Axis");
            
            return null;
        }
        if(usedDataList.contains(colY)){
            OneShotSnackbar.showFor(getRootNode(),
                    "This data series has already been added");
            
            return null;
        }
        XYChartData data = null;
        //branches for time and numeric data
        if(xAxisIsDate){//X-AXIS IS DATE FORMAT
            if(cbDateFormat.getSelectionModel().getSelectedIndex() == -1){
                OneShotSnackbar.showFor(getRootNode(),
                        "Please select a date format");
                
                return null;
            }
            try{
                data = prepareTimeData(colX, colY);
            }catch(DateFormatException ex){
                OneShotSnackbar.showFor(getRootNode(),
                        "Incorrectly formatted date at index "
                        + ex.getCauseIndex());

                return null;
            }catch(DataFrameException ex){
                OneShotSnackbar.showFor(getRootNode(),
                        "No valid data points for selected columns");
                
                return null;
            }
        }else{//X-AXIS IS NUMERIC
            if(DataFrames.columnUsesStrings(colX)){
                OneShotSnackbar.showFor(getRootNode(),
                        "Strings must represent dates");
                
                return null;
            }
            data = prepareNumericData(colX, colY);
        }
        data.setYcolumn(colY);
        this.usedDataList.add(data);
        return data;
    }

    private XYChartData prepareNumericData(final Column colX, final Column colY){
        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        series.setName("Series " + (seriesNumber++));
        final ObservableList<Data<Number, Number>> data = series.getData();
        for(int i=0; i<df.rows(); ++i){
            final Number x = (Number) colX.getValue(i);
            final Number y = (Number) colY.getValue(i);
            if((x != null) && (y != null)){
                data.add(new Data<Number, Number>(x, y));
                if(y.doubleValue() < min){ min = y.doubleValue(); }
                if(y.doubleValue() > max){ max = y.doubleValue(); }
            }
        }
        chartXAxis.setForceZeroInRange(false);
        return new XYChartData(series, min, max);
    }

    private XYChartData prepareTimeData(final Column colX, final Column colY) 
            throws DateFormatException, DataFrameException{

        final XYChart.Series<Number, Number> series = new XYChart.Series<>();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        series.setName("Series " + (seriesNumber++));
        final ObservableList<Data<Number, Number>> data = series.getData();

        final DataFrame dates = buildSortedXYMapping(colX, colY);
        if(dates.isEmpty()){
            throw new DataFrameException();
        }
        //add the origin t=0
        final Column y = dates.getColumn(1);
        final Number yZero = (Number) y.getValue(0);
        data.add(new Data<Number, Number>(0, yZero));
        final LocalDate tZero = LocalDate.parse(String.valueOf(dates.getInt(0, 0)),
                DATE_FORMATTER_ENCODED);
        
        min = yZero.doubleValue();
        max = yZero.doubleValue();

        for(int i=1; i<dates.rows(); ++i){
            final LocalDate d = LocalDate.parse(String.valueOf(dates.getInt(0, i)),
                    DATE_FORMATTER_ENCODED);
            
            final Period p = Period.between(tZero, d);
            final int x = ((p.getYears()*365)+(p.getMonths()*30)+p.getDays());
            final Number yValue = (Number)y.getValue(i);
            data.add(new Data<Number, Number>(x, yValue));
            if(yValue.doubleValue() < min){ min = yValue.doubleValue(); }
            if(yValue.doubleValue() > max){ max = yValue.doubleValue(); }
        }
        //format the x-axis
        chartXAxis.setTickLabelFormatter(new DateTickConverter(tZero));
        chartXAxis.setAutoRanging(false);
        chartXAxis.setLowerBound(0);//set to t=0
        //compute the period between the first and last date in the series
        final Period p = Period.between(tZero, 
                LocalDate.parse(String.valueOf(dates.getInt(0, dates.rows()-1)),
                        DATE_FORMATTER_ENCODED));

        //the total number of days
        final int timeframe = ((p.getYears()*365)+(p.getMonths()*30)+p.getDays());
        chartXAxis.setUpperBound(timeframe);
        if(timeframe > DATE_AXIS_THRESHOLD_TICKS){
            chartXAxis.setTickUnit(Math.ceil(timeframe/DATE_AXIS_THRESHOLD_TICKS));
        }else{
            //each major tick will be 1 day
            chartXAxis.setTickUnit(1);
        }
        chartXAxis.setMinorTickVisible(false);

        return new XYChartData(series, min, max);
    }

    private DataFrame buildSortedXYMapping(final Column colX, final Column colY)
            throws DateFormatException{
        
        return (df.isNullable()
                ? buildSortedXYMappingNullable(colX, colY)
                        : buildSortedXYMappingDefault(colX, colY));
    }

    private DataFrame buildSortedXYMappingDefault(final Column colX, final Column colY) 
            throws DateFormatException{

        final int[] datesRaw = new int[df.rows()];
        final DateTimeFormatter formatter = getDateFormatterFromSelection();
        for(int i=0; i<df.rows(); ++i){
            try{
                final String xRaw = String.valueOf(colX.getValue(i));
                final LocalDate date = LocalDate.parse(xRaw, formatter);
                datesRaw[i] = Integer.valueOf(date.format(DATE_FORMATTER_ENCODED));
            }catch(DateTimeParseException ex){
                throw new DateFormatException(i);
            }
        }
        final DataFrame dates = new DefaultDataFrame(new IntColumn(datesRaw), ((Column)colY.clone()));
        dates.sortBy(0);
        return dates;
    }

    private DataFrame buildSortedXYMappingNullable(final Column colX, final Column colY) 
            throws DateFormatException{

        final Integer[] datesRaw = new Integer[df.rows()];
        final DateTimeFormatter formatter = getDateFormatterFromSelection();
        int validDataPoints = 0;
        for(int i=0; i<df.rows(); ++i){
            final Object val = colX.getValue(i);
            if(val != null){
                try{
                    final String xRaw = String.valueOf(val);
                    final LocalDate date = LocalDate.parse(xRaw, formatter);
                    datesRaw[i] = Integer.valueOf(date.format(DATE_FORMATTER_ENCODED));
                }catch(DateTimeParseException ex){
                    throw new DateFormatException(i);
                }
                //increment if both x- and y-value is guaranteed to be non-null
                if(colY.getValue(i) != null){ ++validDataPoints; }
            }
        }
        //copy non-null entries
        final DataFrame dates = buildNonNullDataFrame(datesRaw, colY, validDataPoints);
        dates.sortBy(0);
        return dates;
    }

    private DataFrame buildNonNullDataFrame(final Integer[] rawX,
            final Column colY, final int validDataPoints){
        
        final int[] xValues = new int[validDataPoints];
        int i = 0;
        switch(colY.typeCode()){
        case NullableByteColumn.TYPE_CODE:{
            final NullableByteColumn y = (NullableByteColumn) colY;
            final byte[] yValues = new byte[validDataPoints];
            for(int j=0; j<df.rows(); ++j){
                if((rawX[j] != null) && (y.get(j) != null)){
                    xValues[i] = rawX[j];
                    yValues[i++] = y.get(j);
                }
            }
            return new DefaultDataFrame(new IntColumn(xValues), new ByteColumn(yValues));
        }
        case NullableShortColumn.TYPE_CODE:{
            final NullableShortColumn y = (NullableShortColumn) colY;
            final short[] yValues = new short[validDataPoints];
            for(int j=0; j<df.rows(); ++j){
                if((rawX[j] != null) && (y.get(j) != null)){
                    xValues[i] = rawX[j];
                    yValues[i++] = y.get(j);
                }
            }
            return new DefaultDataFrame(new IntColumn(xValues), new ShortColumn(yValues));
        }
        case NullableIntColumn.TYPE_CODE:{
            final NullableIntColumn y = (NullableIntColumn) colY;
            final int[] yValues = new int[validDataPoints];
            for(int j=0; j<df.rows(); ++j){
                if((rawX[j] != null) && (y.get(j) != null)){
                    xValues[i] = rawX[j];
                    yValues[i++] = y.get(j);
                }
            }
            return new DefaultDataFrame(new IntColumn(xValues), new IntColumn(yValues));
        }
        case NullableLongColumn.TYPE_CODE:{
            final NullableLongColumn y = (NullableLongColumn) colY;
            final long[] yValues = new long[validDataPoints];
            for(int j=0; j<df.rows(); ++j){
                if((rawX[j] != null) && (y.get(j) != null)){
                    xValues[i] = rawX[j];
                    yValues[i++] = y.get(j);
                }
            }
            return new DefaultDataFrame(new IntColumn(xValues), new LongColumn(yValues));
        }
        case NullableFloatColumn.TYPE_CODE:{
            final NullableFloatColumn y = (NullableFloatColumn) colY;
            final float[] yValues = new float[validDataPoints];
            for(int j=0; j<df.rows(); ++j){
                if((rawX[j] != null) && (y.get(j) != null)){
                    xValues[i] = rawX[j];
                    yValues[i++] = y.get(j);
                }
            }
            return new DefaultDataFrame(new IntColumn(xValues), new FloatColumn(yValues));
        }
        case NullableDoubleColumn.TYPE_CODE:{
            final NullableDoubleColumn y = (NullableDoubleColumn) colY;
            final double[] yValues = new double[validDataPoints];
            for(int j=0; j<df.rows(); ++j){
                if((rawX[j] != null) && (y.get(j) != null)){
                    xValues[i] = rawX[j];
                    yValues[i++] = y.get(j);
                }
            }
            return new DefaultDataFrame(new IntColumn(xValues), new DoubleColumn(yValues));
        }
        }
        return null;
    }
}
