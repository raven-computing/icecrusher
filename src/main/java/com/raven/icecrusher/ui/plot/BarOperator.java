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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.raven.common.struct.Column;

import static com.raven.icecrusher.ui.plot.BarChartController.THRESHOLD_MAX_X_VALUES;

/**
 * Class for creating various mappings for operations on bars.
 *
 */
public abstract class BarOperator {

    Column colX;
    Column colY;
    Map<Object, Double> map;

    BarOperator(final Column colX, final Column colY){
        this.colX = colX;
        this.colY = colY;
        //use a TreeMap to impose a natural ordering on the x-axis values
        this.map = new TreeMap<>();
    }

    /**
     * Performs an iteration of the concrete bar operation
     * 
     * @param index The index of the iteration to perform
     * @throws RuntimeException If any internal constraints are violated
     */
    public abstract void operate(int index) throws RuntimeException;

    /**
     * Finishes all operations on the given bars and returns the computed result
     * as a Map, holding the x-axis values as the keys and the corresponding y-axis
     * values, defined by the underlying operation mode, as the map-values 
     * 
     * @return A Map holding the result of the operation
     */
    public abstract Map<Object, Double> finish();


    /**
     * Constructs a <code>BarOperator</code> for computing the bar mapping in single mode
     * 
     * @param colX The <code>Column</code> holding the values for the x-axis
     * @param colY The <code>Column</code> holding the values for the y-axis
     * @return A <code>BarOperator</code> set in single mode
     */
    public static BarOperator singleMode(final Column colX, final Column colY){
        return new SingleBarOperator(colX, colY);
    }

    /**
     * Constructs a <code>BarOperator</code> for computing the bar mapping in minimum mode
     * 
     * @param colX The <code>Column</code> holding the values for the x-axis
     * @param colY The <code>Column</code> holding the values for the y-axis
     * @return A <code>BarOperator</code> set in minimum mode
     */
    public static BarOperator minimumMode(final Column colX, final Column colY){
        return new MinimumBarOperator(colX, colY);
    }

    /**
     * Constructs a <code>BarOperator</code> for computing the bar mapping in maximum mode
     * 
     * @param colX The <code>Column</code> holding the values for the x-axis
     * @param colY The <code>Column</code> holding the values for the y-axis
     * @return A <code>BarOperator</code> set in maximum mode
     */
    public static BarOperator maximumMode(final Column colX, final Column colY){
        return new MaximumBarOperator(colX, colY);
    }

    /**
     * Constructs a <code>BarOperator</code> for computing the bar mapping in average mode
     * 
     * @param colX The <code>Column</code> holding the values for the x-axis
     * @param colY The <code>Column</code> holding the values for the y-axis
     * @return A <code>BarOperator</code> set in average mode
     */
    public static BarOperator averageMode(final Column colX, final Column colY){
        return new AverageBarOperator(colX, colY);
    }

    /**
     * Constructs a <code>BarOperator</code> for computing the bar mapping in sum mode
     * 
     * @param colX The <code>Column</code> holding the values for the x-axis
     * @param colY The <code>Column</code> holding the values for the y-axis
     * @return A <code>BarOperator</code> set in sum mode
     */
    public static BarOperator sumMode(final Column colX, final Column colY){
        return new SumBarOperator(colX, colY);
    }

}

/**
 * A BarOperator implementing operations in SINGLE MODE.
 *
 */
class SingleBarOperator extends BarOperator {

    SingleBarOperator(final Column colX, final Column colY){
        super(colX, colY);
    }

    @Override
    public void operate(int index) throws RuntimeException{
        Object value = colX.getValue(index);
        if(value != null){
            final Object xValue = value;
            value = colY.getValue(index);
            final Double yValue = ((value != null)
                    ? Double.valueOf(String.valueOf(value))
                    : null);
            
            if(yValue != null){
                map.put(xValue, yValue);
            }else{
                map.put(xValue, 0.0);
            }
        }
        if(map.size() > THRESHOLD_MAX_X_VALUES){
            throw new RuntimeException("The x-Axis has too many unique values");
        }
    }

    @Override
    public Map<Object, Double> finish(){
        return map;
    }
}

/**
 * A BarOperator implementing operations in MINIMUM MODE.
 *
 */
class MinimumBarOperator extends BarOperator {

    MinimumBarOperator(final Column colX, final Column colY){
        super(colX, colY);
    }

    @Override
    public void operate(int index) throws RuntimeException{
        Object value = colX.getValue(index);
        if(value != null){
            final Object xValue = value;
            value = colY.getValue(index);
            final Double yValue = ((value != null)
                    ? Double.valueOf(String.valueOf(value))
                    : null);
            
            final Double current = map.get(xValue);
            if(current != null){
                if((yValue != null) && (yValue < current)){
                    map.put(xValue, yValue);
                }
            }else{
                if(yValue != null){
                    map.put(xValue, yValue);
                }else{
                    map.put(xValue, Double.MAX_VALUE);
                }
            }
        }
        if(map.size() > THRESHOLD_MAX_X_VALUES){
            throw new RuntimeException("The x-Axis has too many unique values");
        }
    }

    @Override
    public Map<Object, Double> finish(){
        for(final Map.Entry<Object, Double> e : map.entrySet()){
            if(e.getValue() == Double.MAX_VALUE){
                e.setValue(0.0);
            }
        }
        return map;
    }
}

/**
 * A BarOperator implementing operations in MAXIMUM MODE.
 *
 */
class MaximumBarOperator extends BarOperator {

    MaximumBarOperator(final Column colX, final Column colY){
        super(colX, colY);
    }

    @Override
    public void operate(int index) throws RuntimeException{
        Object value = colX.getValue(index);
        if(value != null){
            final Object xValue = value;
            value = colY.getValue(index);
            final Double yValue = ((value != null)
                    ? Double.valueOf(String.valueOf(value))
                    : null);
            
            final Double current = map.get(xValue);
            if(current != null){
                if((yValue != null) && (yValue > current)){
                    map.put(xValue, yValue);
                }
            }else{
                if(yValue != null){
                    map.put(xValue, yValue);
                }else{
                    map.put(xValue, Double.MIN_VALUE);
                }
            }
        }
        if(map.size() > THRESHOLD_MAX_X_VALUES){
            throw new RuntimeException("The x-Axis has too many unique values");
        }
    }

    @Override
    public Map<Object, Double> finish(){
        for(final Map.Entry<Object, Double> e : map.entrySet()){
            if(e.getValue() == Double.MIN_VALUE){
                e.setValue(0.0);
            }
        }
        return map;
    }
}

/**
 * A BarOperator implementing operations in AVERAGE MODE.
 *
 */
class AverageBarOperator extends BarOperator {

    private Map<String, Integer> mapTotal;

    AverageBarOperator(final Column colX, final Column colY){
        super(colX, colY);
        this.mapTotal = new HashMap<>();
    }

    @Override
    public void operate(int index) throws RuntimeException{
        Object value = colX.getValue(index);
        if(value != null){
            final Object xValue = value;
            value = colY.getValue(index);
            final Double yValue = ((value != null)
                    ? Double.valueOf(String.valueOf(value))
                    : null);
            
            final Double current = map.get(xValue);
            if(current != null){
                if(yValue != null){
                    map.put(xValue, yValue + current);
                    final String key = xValue.toString();
                    mapTotal.put(key, mapTotal.get(key)+1);
                }
            }else{
                if(yValue != null){
                    map.put(xValue, yValue);
                    final Integer amount = mapTotal.get(xValue);
                    mapTotal.put(xValue.toString(),
                            ((amount != null) ? amount+1 : 1));
                    
                }else{
                    map.put(xValue, 0.0);
                }
            }
        }
        if(map.size() > THRESHOLD_MAX_X_VALUES){
            throw new RuntimeException("The x-Axis has too many unique values");
        }
    }

    @Override
    public Map<Object, Double> finish(){
        for(final Map.Entry<Object, Double> e : map.entrySet()){
            e.setValue(e.getValue() / mapTotal.get(e.getKey().toString()));
        }
        return map;
    }
}

/**
 * A BarOperator implementing operations in SUM MODE.
 *
 */
class SumBarOperator extends BarOperator {

    SumBarOperator(final Column colX, final Column colY){
        super(colX, colY);
    }

    @Override
    public void operate(int index) throws RuntimeException{
        Object value = colX.getValue(index);
        if(value != null){
            final Object xValue = value;
            value = colY.getValue(index);
            final Double yValue = ((value != null)
                    ? Double.valueOf(String.valueOf(value))
                    : null);
            
            final Double current = map.get(xValue);
            if(current != null){
                if(yValue != null){
                    map.put(xValue, yValue + current);
                }
            }else{
                if(yValue != null){
                    map.put(xValue, yValue);
                }else{
                    map.put(xValue, 0.0);
                }
            }
        }
        if(map.size() > THRESHOLD_MAX_X_VALUES){
            throw new RuntimeException("The x-Axis has too many unique values");
        }
    }

    @Override
    public Map<Object, Double> finish(){
        return map;
    }
}
