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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;

/**
 * Implements a read-only cell to be used for BinaryColumns in DataFrameViews.
 *
 */
public class BinaryCellView extends DataFrameViewCell<Integer, Object> {
    
    private TextField textField;
    private TextFormatter<Object> textFormatter;

    /**
     * Constructs a new <code>BinaryCellView</code> using the filter and
     * string converter of the specified ConversionPack and adding the specified tooltip.<br>
     * The filter of the ConversionPack is used to filter editing input passed to this cell.
     * The converter is used to convert the string input to an object and vice versa
     * 
     * @param conversion The <code>ConversionPack</code> holding a filter and converter for
     *                   this table cell. Must not be null
     * @param tooltip The tooltip to add to the cell. May be null
     */
    public BinaryCellView(final ConversionPack conversion, final Tooltip tooltip){
        super();
        textField = new TextField();
        textFormatter = new TextFormatter<Object>(
                conversion.getConverter(),
                "DEFAULT",
                conversion.getFilter());
        
        textField.setTextFormatter(textFormatter);
        
        textProperty().bind(Bindings
                .when(emptyProperty())
                .then((String)null)
                .otherwise(asString(itemProperty())));

        setGraphic(textField);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        if(tooltip != null){
            setTooltip(tooltip);
        }
    }

    @Override
    protected void updateItem(Object value, boolean empty){
        super.updateItem(value, empty);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
    
    private static StringBinding asString(final ObservableValue<?> ov){
        return new StringBinding(){
            {
                super.bind(ov);
            }

            @Override
            public void dispose(){
                super.unbind(ov);
            }

            @Override
            protected String computeValue(){
                final Object obj = ov.getValue();
                return (obj != null) ? Converters.binaryTruncatingConverter().toString(obj) : "null";
            }
        };
    }
}
