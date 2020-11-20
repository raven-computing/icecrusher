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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.raven.common.struct.Column;
import com.raven.icecrusher.io.DataFrames;
import com.raven.icecrusher.ui.OneShotSnackbar;
import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;

import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

/**
 * Controller class for the Pie Chart activity.
 *
 */
public class PieChartController extends ChartController {
    
    /**
     * The maximum number of slices the pie chart is allowed to create.
     * Prevents performance issues on weak machines. Having more slices results
     * in an unclear plot anyway and is considered an unrealistic case
     */
    private static final int MAX_NUMBER_SLICES_ALLOWED = 25;

    @FXML
    private JFXComboBox<String> cbColumnKeys;

    @FXML
    private JFXComboBox<String> cbColumnValues;

    @FXML
    private JFXCheckBox checkShowAbs;

    @FXML
    private JFXCheckBox checkShowPerc;

    @FXML
    private PieChart chart;

    private ObservableList<PieChart.Data> data;
    private Map<SettingsView, PieChart.Data> viewDataMap;

    private boolean showAbs;
    private boolean showPerc;
    private boolean multiColumn;
    private boolean plotDataChanged;
    private boolean keyChanged;

    private int totalNulls;
    private double totalSum;

    public PieChartController(){
        super();
    }

    @FXML
    @Override
    public void initialize(){
        super.initialize(this.chart);
        //CheckBoxes
        this.checkShowAbs.selectedProperty().addListener((ov, oldValue, newValue) -> {
            this.showAbs = newValue;
            getConfiguration().set(PLOT, CONFIG_PIECHART_SHOW_ABS, newValue);
            if(!plotDataChanged){
                reloadSliceLabels();
                updateAllLegendColors();
            }
        });
        this.checkShowPerc.selectedProperty().addListener((ov, oldValue, newValue) -> {
            this.showPerc = newValue;
            getConfiguration().set(PLOT, CONFIG_PIECHART_SHOW_PERC, newValue);
            if(!plotDataChanged){
                reloadSliceLabels();
                updateAllLegendColors();
            }
        });
        this.showAbs = getConfiguration().booleanOf(PLOT, CONFIG_PIECHART_SHOW_ABS);
        this.showPerc = getConfiguration().booleanOf(PLOT, CONFIG_PIECHART_SHOW_PERC);
        this.checkShowAbs.setSelected(showAbs);
        this.checkShowPerc.setSelected(showPerc);
        //add a listener to get notified when the selection changes
        //and update the entire legend. Otherwise the legend will display
        //the old items when its visibility changes after the user has plotted a new chart
        this.cbLegendPosition.getSelectionModel().selectedIndexProperty().addListener(
                (ov, oldValue, newValue) -> {

                    if(((int)oldValue) == 4){//oldValue equals 'Disabled'
                        updateLegend();
                    }
                });
    }
    
    @Override
    public void onStart(ArgumentBundle bundle){
        super.onStart(bundle);
        final String[] names = df.getColumnNames();
        //ComboBoxes
        this.cbColumnKeys.getItems().addAll(names);
        this.cbColumnValues.getItems().addAll(names);
        this.cbColumnKeys.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {

                    keySelectionChanged(newValue);
                });
        this.cbColumnValues.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldValue, newValue) -> {

                    valueSelectionChanged(newValue);
                });
    }

    @FXML
    @Override
    public void onPlot(ActionEvent event){
        if(plotIsShown && !plotDataChanged){
            exportSnapshot();
        }else{
            this.chart.setData(data);
            //apply all selected colors
            for(final Map.Entry<SettingsView, PieChart.Data> e : viewDataMap.entrySet()){
                e.getValue().getNode().setStyle("-fx-pie-color: " + e.getKey().getColor());
            }
            this.plotIsShown = true;
            this.plotDataChanged = false;
            this.btnPlotExport.setText("Export as PNG");
            reloadSliceLabels();
            updateAllLegendColors();
        }
    }

    @FXML
    protected void onClose(ActionEvent event){
        super.onClose(event);
    }

    private void keySelectionChanged(final String newKey){
        this.btnPlotExport.setDisable(false);
        settingsList.resetSettingsList();
        this.keyChanged = true;
        final String value = this.cbColumnValues.getSelectionModel().getSelectedItem();
        if((value != null) && (!value.isEmpty())){
            prepareChart(df.getColumn(newKey), df.getColumn(value));
        }else{
            prepareChart(df.getColumn(newKey), null);
        }
    }

    private void valueSelectionChanged(final String newValue){
        final String key = this.cbColumnKeys.getSelectionModel().getSelectedItem();
        if((key != null) && (!key.isEmpty())){
            this.btnPlotExport.setDisable(false);
            prepareChart(df.getColumn(key), df.getColumn(newValue));
        }
    }

    private void prepareChart(final Column keys, final Column values){
        if(DataFrames.columnUsesBinary(keys)){
            showInfo("Cannot use binary data");
            return;
        }
        final boolean hasValues = (values != null);
        if((values != null) && !values.isNumeric()){//compiler NPE warning for 'hasValues'
            showInfo("Values must be numeric");
            return;
        }
        Map<String, Number> map = null;
        try{
            map = (hasValues ? prepareData(keys, values) : prepareData(keys));
        }catch(RuntimeException ex){
            showInfo(ex.getMessage());
            return;
        }
        // perform various checks
        if(totalNulls == df.rows()){
            showInfo("This column only contains null values");
            return;
        }
        if(map.size() > MAX_NUMBER_SLICES_ALLOWED){
            showInfo("This column produces too many slices");
            return;
        }
        if(map.size() != settingsList.getChildren().size()){
            settingsList.resetSettingsList();
            this.keyChanged = true;
        }
        //set up members
        this.viewDataMap = new HashMap<>();
        this.multiColumn = hasValues;
        final List<PieChart.Data> list = new ArrayList<>();
        final List<SettingsView> views = new ArrayList<>();
        int index = 0;
        for(final Map.Entry<String, Number> e : map.entrySet()){
            final PieChart.Data slice = new PieChart.Data(labelForSlice(e.getKey(),
                    e.getValue().doubleValue()),
                    e.getValue().doubleValue());

            list.add(slice);
            final SettingsView ssv = initSettingsView(index, e, !keyChanged);
            viewDataMap.put(ssv, slice);
            views.add(ssv);
            ++index;
        }
        this.data = FXCollections.observableArrayList(list);
        //only add and animate the SettingsListView the first time that key is used
        if(keyChanged){
            addAllSettingsViewsToList(views);
        }
        this.keyChanged = false;
        this.btnPlotExport.setText("Plot");
        this.plotDataChanged = true;
    }

    private SettingsView initSettingsView(final int index,
            final Map.Entry<String, Number> entry, final boolean recycle){

        if(recycle){
            //reuse the current set SettingsView
            return this.settingsList.getSettingsViewAt(index);
        }
        final SettingsView ssv = new SliceSettingsView(index, entry.getKey());
        ssv.setViewListener(new ViewListenerAdapter(){
            @Override
            public void onRelabel(SettingsView view, String newLabel){
                if(!plotDataChanged){
                    final PieChart.Data slice = viewDataMap.get(view);
                    viewDataMap.get(view).setName(labelForSlice(newLabel, slice.getPieValue()));
                    updateAllLegendColors();
                }
            }

            @Override
            public void onColorChanged(SettingsView view, String newColor){
                if(!plotDataChanged){
                    final Node node = viewDataMap.get(view).getNode();
                    if(node != null){// only set color if plot is rendered on screen
                        node.setStyle("-fx-pie-color: " + newColor);
                        updateLegendColor(view, newColor);
                    }
                }
            }
        });
        return ssv;
    }

    private Map<String, Number> prepareData(final Column keys){
        this.totalNulls = 0;
        final Map<String, Number> map = new HashMap<>();
        for(int i=0; i<df.rows(); ++i){
            final Object value = keys.getValue(i);
            if(value != null){
                final String s = value.toString();
                final Integer count = (Integer)map.get(s);
                if(count != null){
                    map.put(s, count+1);
                }else{
                    map.put(s, 1);
                }
            }else{//track null values
                ++totalNulls;
            }
        }
        return map;
    }

    private Map<String, Number> prepareData(final Column keys, final Column values){
        BigDecimal bdSum = BigDecimal.ZERO;
        final Map<String, Number> map = new HashMap<>();
        for(int i=0; i<df.rows(); ++i){
            final Object key = keys.getValue(i);
            final Number value = (Number)values.getValue(i);
            if((key != null) && (value != null)){
                final double dValue = value.doubleValue();
                if(dValue < 0){
                    throw new RuntimeException("Values contain negative numbers "
                            + "(At index " + String.valueOf(i) + ")");

                }
                final String s = key.toString();
                final Number sum = (Number)map.get(s);
                if(sum != null){
                    map.put(s, new BigDecimal(Double.toString(sum.doubleValue()))
                            .add(new BigDecimal(Double.toString(dValue))));

                }else{
                    map.put(s, value);
                }
                bdSum = bdSum.add(new BigDecimal(Double.toString(value.doubleValue())));
            }
        }
        this.totalSum = bdSum.doubleValue();
        return map;
    }

    private String labelForSlice(final String label, final double value){
        final StringBuilder sb = new StringBuilder();
        final DecimalFormat form = new DecimalFormat("##.##");
        sb.append(label);
        if(showAbs){
            sb.append(" ");
            sb.append(form.format(value));
        }
        if(showPerc){
            final double perc = (multiColumn
                    ? (value / totalSum)
                            : (value / (double)(df.rows()-totalNulls)));

            sb.append(showAbs ? " (" : " ");
            sb.append(form.format(perc*100));
            sb.append(showAbs ? "%)" : "%");
        }
        return sb.toString();
    }

    private void reloadSliceLabels(){
        if(viewDataMap != null){
            for(final Map.Entry<SettingsView, PieChart.Data> e : viewDataMap.entrySet()){
                final PieChart.Data slice = e.getValue();
                slice.setName(labelForSlice(e.getKey().getEditText(),
                        slice.getPieValue()));

            }
        }
    }

    private void updateLegend(){
        final Legend legend = getChartLegend();
        legend.getItems().clear();
        if(chart.getData() != null){
            for(final PieChart.Data slice : chart.getData()) {
                final LegendItem item = new LegendItem(slice.getName());
                item.getSymbol().getStyleClass().addAll(slice.getNode().getStyleClass());
                item.getSymbol().getStyleClass().add("pie-legend-symbol");
                legend.getItems().add(item);
            }
            updateAllLegendColors();
        }
    }

    private void showInfo(final String message){
        OneShotSnackbar.showFor(getRootNode(), message);
        if(!plotIsShown || plotDataChanged){
            this.btnPlotExport.setDisable(true);
        }
    }
}
