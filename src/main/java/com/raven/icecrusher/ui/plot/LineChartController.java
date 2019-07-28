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

package com.raven.icecrusher.ui.plot;

import java.util.LinkedList;
import com.raven.common.struct.Column;
import com.raven.icecrusher.ui.plot.SettingsView.ViewListener;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Series;

/**
 * Controller class for the Line Chart activity.
 *
 */
public class LineChartController extends XYChartController {

    @FXML
    private LineChart<Number, Number> chart;

    public LineChartController(){
        super();
    }

    @FXML
    @Override
    public void initialize(){
        super.initialize(this.chart);
    }

    @Override
    public void onStart(ArgumentBundle bundle){
        super.onStart(bundle);
        //CheckBoxes
        checkDataPoints.selectedProperty().addListener(
                (ov, oldValue, newValue) -> {
                    
            chart.setCreateSymbols(newValue);
            if(newValue){
                updateAllSymbolsColor(chart);
            }
        });
        chart.setCreateSymbols(checkDataPoints.isSelected());
        chartYAxis.setForceZeroInRange(false);
    }

    @FXML
    @Override
    public void onPlot(ActionEvent event){
        if(!plotIsShown || (preparedSeries != null)){
            this.chart.getData().addAll(preparedSeries);
            this.plotIsShown = true;
            this.btnPlotExport.setText("Export as PNG");
            for(final Series<Number, Number> series : preparedSeries){
                final String color = getSettingsViewForSeries(series).getColor();
                updateLineColor(series.getNode(), color);
                updateSymbolsColor(series.getData(), color);
            }
            updateAllLegendColors();
            this.preparedSeries = null;
        }else{
            exportSnapshot();
        }
    }

    @FXML
    @Override
    protected void onAdd(ActionEvent event){
        prepareChart();
    }

    @FXML
    protected void onClose(ActionEvent event){
        super.onClose(event);
    }

    private void prepareChart(){
        final Column colX = selectedXColumn();
        final Column colY = selectedYColumn();

        final XYChartData data = prepareData(colX, colY);
        if(data == null){
            return;
        }
        final Series<Number, Number> series = data.getSeries();
        setDateControlsDisabled(true);

        final int index = this.settingsList.getChildren().size();
        final SettingsView lsv = new LineSettingsView(index, series.getName());
        lsv.setViewListener(new ViewListener(){
            @Override
            public void onRelabel(SettingsView view, String newLabel){
                series.setName(newLabel);
                updateAllLegendColors();
            }
            @Override
            public void onColorChanged(SettingsView view, String newColor){
                final Node node = series.getNode();
                if(node != null){//only set color if plot is rendered on screen
                    updateLineColor(node, newColor);
                    updateLegendColor(view, newColor);
                    updateSymbolsColor(series.getData(), newColor);
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
        addSettingsViewToList(lsv);

        if(preparedSeries == null){
            this.preparedSeries = new LinkedList<>();
        }
        this.preparedSeries.add(series);

        this.btnPlotExport.setDisable(false);
        this.btnPlotExport.setText("Plot");
    }

    private void updateLineColor(final Node node, final String color){
        if(node != null){
            node.setStyle("-fx-stroke: " + color);
        }
    }

}
