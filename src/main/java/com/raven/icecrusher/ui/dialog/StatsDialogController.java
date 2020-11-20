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

package com.raven.icecrusher.ui.dialog;

import static java.lang.Double.isNaN;

import java.text.DecimalFormat;

import com.raven.icecrusher.util.ColumnStats;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller class for the {@link StatsDialog}.
 *
 */
public class StatsDialogController {

    /**
     * Listener interface for the <code>StatsDialog</code>.
     *
     */
    public interface DialogListener {

        /**
         * Called when the user presses the close button
         * 
         */
        void onClose();
    }

    @FXML
    private Label mainLabel;

    @FXML
    private Label labelMin;

    @FXML
    private Label labelMax;

    @FXML
    private Label labelAvg;

    @FXML
    private Label labelSum;

    private DialogListener delegate;

    public void setCloseListener(DialogListener delegate){
        this.delegate = delegate;
    }

    public void setColumnStats(final ColumnStats stats){
        mainLabel.setText("Stats for " + stats.getColumnName());
        final boolean b = stats.usesDecimals();
        final DecimalFormat form = new DecimalFormat("##.####");
        if(b){
            final double min = stats.getMinimum();
            labelMin.setText(isNaN(min) ? "NaN" : form.format(min));
            final double max = stats.getMaximum();
            labelMax.setText(isNaN(max) ? "NaN" : form.format(max));
            final double sum = stats.getSum();
            labelSum.setText(isNaN(sum) ? "NaN" : form.format(sum));
        }else{
            final double min = stats.getMinimum();
            labelMin.setText(isNaN(min) ? "NaN" : String.valueOf(
                    stats.getMinimumNoDecimals()));

            final double max = stats.getMaximum();
            labelMax.setText(isNaN(max) ? "NaN" : String.valueOf(
                    stats.getMaximumNoDecimals()));

            final double sum = stats.getSum();
            labelSum.setText(isNaN(sum) ? "NaN" : String.valueOf(
                    stats.getSumNoDecimals()));

        }
        final double avg = stats.getAverage();
        labelAvg.setText(isNaN(avg) ? "NaN" : form.format(avg));
    }

    @FXML
    private void initialize(){ }

    @FXML
    private void onClose(ActionEvent event){
        if(delegate != null){
            delegate.onClose();
        }
    }
}
