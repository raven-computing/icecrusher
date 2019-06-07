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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.raven.common.struct.Column;
import com.raven.icecrusher.ui.OneShotSnackbar;
import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;

/**
 * Controller class for the Pie Chart activity.
 *
 */
@SuppressWarnings("restriction")
public class PieChartController extends ChartController {
	
	/**
	 * The maximum number of slices the pie chart is allowed to create.
	 * Prevents performance issues on weak machines. Having more slices results
	 * in an unclear plot anyway and is considered an unrealistic case
	 */
	private static final int MAX_NUMBER_SLICES_ALLOWED = 25;
	
	@FXML
	private JFXComboBox<String> cbColumn;
	
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
	
	private int totalNulls;

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
			reloadSliceLabels();
			updateAllLegendColors();
		});
		this.checkShowPerc.selectedProperty().addListener((ov, oldValue, newValue) -> {
			this.showPerc = newValue;
			reloadSliceLabels();
			updateAllLegendColors();
		});
		this.showAbs = true;
		this.showPerc = true;
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
		//ComboBoxes
		this.cbColumn.getItems().addAll(df.getColumnNames());
		this.cbColumn.getSelectionModel().selectedItemProperty().addListener(
				(ov, oldValue, newValue) -> {
					
			this.btnPlotExport.setDisable(false);
			settingsList.resetSettingsList();
			if(plotIsShown){
				this.btnPlotExport.setText("Plot");
				this.plotIsShown = false;
			}
			prepareChart(df.getColumn(newValue));
		});
	}
	
	@FXML
	@Override
	public void onPlot(ActionEvent event){
		if(plotIsShown){
			exportSnapshot();
		}else{
			this.chart.setData(data);
			//apply all selected colors
			for(final Map.Entry<SettingsView, PieChart.Data> e : viewDataMap.entrySet()){
				e.getValue().getNode().setStyle("-fx-pie-color: " + e.getKey().getColor());
			}
			this.plotIsShown = true;
			this.btnPlotExport.setText("Export as PNG");
			updateAllLegendColors();
		}
	}

	@FXML
	protected void onClose(ActionEvent event){
		super.onClose(event);
	}
	
	private void prepareChart(final Column col){
		this.viewDataMap = new HashMap<>();
		Map<String, Integer> map = prepareData(col);
		//perform various checks
		if(totalNulls == df.rows()){
			showInfo("This column only contains null values");
			return;
		}
		if(map.size() > MAX_NUMBER_SLICES_ALLOWED){
			showInfo("This column produces too many slices");
			return;
		}
		//set up members
		final List<PieChart.Data> list = new ArrayList<>();
		final List<SettingsView> views = new ArrayList<>();
		int i = 0;
		for(final Map.Entry<String, Integer> e : map.entrySet()){
			final PieChart.Data slice = new PieChart.Data(labelForSlice(e.getKey(), e.getValue()),
					e.getValue());
			
			list.add(slice);
			final SettingsView ssv = new SliceSettingsView(i++, e.getKey());
			ssv.setViewListener(new ViewListenerAdapter(){
				@Override
				public void onRelabel(SettingsView view, String newLabel){
					viewDataMap.get(view).setName(labelForSlice(newLabel, e.getValue()));
					updateAllLegendColors();
				}
				@Override
				public void onColorChanged(SettingsView view, String newColor){
					final Node node = viewDataMap.get(view).getNode();
					if(node != null){//only set color if plot is rendered on screen
						node.setStyle("-fx-pie-color: " + newColor);
						updateLegendColor(view, newColor);
					}
				}
			});
			viewDataMap.put(ssv, slice);
			views.add(ssv);
		}
		this.data = FXCollections.observableArrayList(list);
		addAllSettingsViewsToList(views);
	}

	private Map<String, Integer> prepareData(final Column col){
		this.totalNulls = 0;
		final Map<String, Integer> map = new HashMap<>();
		for(int i=0; i<df.rows(); ++i){
			final Object value = col.getValueAt(i);
			if(value != null){
				final String s = String.valueOf(value);
				final Integer count = map.get(s);
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
	
	private String labelForSlice(final String label, final int value){
		final StringBuilder sb = new StringBuilder();
		sb.append(label);
		if(showAbs){
			sb.append(" ");
			sb.append(value);
		}
		if(showPerc){
			final DecimalFormat form = new DecimalFormat("##.##");
			final double perc = ((double)value / (double)(df.rows()-totalNulls));
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
				slice.setName(labelForSlice(e.getKey().getEditText(), (int) slice.getPieValue()));
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
		this.btnPlotExport.setDisable(true);
	}
	
}
