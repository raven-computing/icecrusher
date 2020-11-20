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

package com.raven.icecrusher.util;

/**
 * Holds statistics about a numerical Column.
 *
 */
public class ColumnStats {

    private String columnName;
    private double min;
    private double max;
    private double avg;
    private double sum;
    private boolean usesDecimals;

    public ColumnStats(){ }

    public double getMinimum(){
        return this.min;
    }

    public long getMinimumNoDecimals(){
        return (long)min;
    }

    public void setMinimum(final double min){
        this.min = min;
    }

    public double getMaximum(){
        return this.max;
    }

    public long getMaximumNoDecimals(){
        return (long)max;
    }

    public void setMaximum(final double max){
        this.max = max;
    }

    public double getAverage(){
        return this.avg;
    }

    public void setAverage(final double avg){
        this.avg = avg;
    }

    public double getSum(){
        return this.sum;
    }

    public long getSumNoDecimals(){
        return (long)sum;
    }

    public void setSum(final double sum){
        this.sum = sum;
    }

    public String getColumnName(){
        return this.columnName;
    }

    public void setColumnName(final String columnName){
        this.columnName = columnName;
    }

    public boolean usesDecimals(){
        return this.usesDecimals;
    }

    public void setUsesDecimals(final boolean usesDecimals){
        this.usesDecimals = usesDecimals;
    }
}
