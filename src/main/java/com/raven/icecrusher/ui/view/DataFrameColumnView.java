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

package com.raven.icecrusher.ui.view;

import com.raven.common.struct.Column;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;

/**
 * The DataFrameColumnView control is used to visually display the content of
 * a column in a DataFrameView. 
 *
 */
public class DataFrameColumnView extends TableColumn<Integer, Object> {

    /**
     * Defines all supported column types by their internally used types.
     *
     */
    public enum ColumnType {
        BYTE,
        SHORT,
        INT,
        LONG,
        STRING,
        FLOAT,
        DOUBLE,
        CHAR,
        BOOLEAN,
        BINARY;
    }

    private Column column;

    /**
     * Constructs a new <code>DataFrameColumnView</code> for the specified 
     * column with the specified name
     * 
     * @param column The <code>Column</code> to use
     * @param columnName The name of the view. This will be used as the title 
     *                   of the view
     */
    public DataFrameColumnView(final Column column, final String columnName){
        super(columnName);
        this.column = column;
        //make sure the internal column width is not fractional.
        //Fractional width properties seem to be causing minor visual problems
        //on startup and when a column is resized
        ensureCeiledWidth();
    }

    public Column getColumn(){
        return this.column;
    }

    public DataFrameView getDataFrameView(){
        return ((DataFrameView)super.getTableView());
    }

    private void ensureCeiledWidth(){
        //ensures this table column has an integer width property
        //this only needs to be set once. Subsequent resizing does not
        //appear to further change the decimal place  
        final ChangeListener<Number> listener = new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, 
                    Number oldValue, Number newValue){

                //always round up to the nearest integer number
                setPrefWidth(Math.ceil(getWidth()));
                //remove listener declared above. There is no need to further
                //ceil subsequent column resizing
                widthProperty().removeListener(this);
            }
        };
        //adds listener at the beginning
        widthProperty().addListener(listener);
    }

}
