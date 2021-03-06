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

package com.raven.icecrusher.ui.dialog;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.raven.common.struct.BinaryColumn;
import com.raven.common.struct.BooleanColumn;
import com.raven.common.struct.ByteColumn;
import com.raven.common.struct.CharColumn;
import com.raven.common.struct.Column;
import com.raven.common.struct.DataFrame;
import com.raven.common.struct.DoubleColumn;
import com.raven.common.struct.FloatColumn;
import com.raven.common.struct.IntColumn;
import com.raven.common.struct.LongColumn;
import com.raven.common.struct.NullableBinaryColumn;
import com.raven.common.struct.NullableBooleanColumn;
import com.raven.common.struct.NullableByteColumn;
import com.raven.common.struct.NullableCharColumn;
import com.raven.common.struct.NullableDoubleColumn;
import com.raven.common.struct.NullableFloatColumn;
import com.raven.common.struct.NullableIntColumn;
import com.raven.common.struct.NullableLongColumn;
import com.raven.common.struct.NullableShortColumn;
import com.raven.common.struct.NullableStringColumn;
import com.raven.common.struct.ShortColumn;
import com.raven.common.struct.StringColumn;
import com.raven.icecrusher.ui.OneShotSnackbar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * Controller class for the {@link AddColumnDialog}.
 *
 */
public class AddColumnDialogController {

    /**
     * Listener interface for the <code>AddColumnDialog</code>.
     *
     */
    public interface DialogListener {

        /**
         * Called when the user confirms a column creation
         * 
         * @param name The name of the <code>Column</code> to add
         * @param col The <code>Column</code> to add to the DataFrame
         */
        void onCreate(String name, Column col);
    }

    public static String[] options = {"Byte", "Short", "Int", "Long",
            "String", "Float", "Double", "Char", "Boolean", "Binary"};

    @FXML
    private JFXComboBox<String> cBoxColType;

    @FXML
    private JFXTextField txtColName;

    private Pane rootPane;
    private DialogListener delegate;
    private DataFrame df;
    private boolean useNullable;
    private int size;

    @FXML
    public void initialize(){
        cBoxColType.getItems().removeAll(cBoxColType.getItems());
        cBoxColType.getItems().addAll(options);
    }

    public void setRootContainer(Pane pane){
        this.rootPane = pane;
    }

    public void useDataFrame(final DataFrame df){
        this.df = df;
        this.useNullable = df.isNullable();
        this.size = df.rows();
    }

    public void setAddListener(DialogListener listener){
        this.delegate = listener;
    }

    @FXML
    private void onAdd(ActionEvent event){
        final Column col = constructColumn(cBoxColType.getValue());
        if(col == null){
            showWarnMsg("Please specify the column type");
            return;
        }
        final String name = txtColName.getText();
        if(name == null || name.isEmpty()){
            showWarnMsg("Please specify a valid column name");
            return;
        }
        if(nameIsDuplicate(name)){
            showWarnMsg("Duplicate column name");
            return;
        }
        if(delegate != null){
            delegate.onCreate(name, col);
        }
    }

    private Column constructColumn(final String type){
        if(type != null){
            switch(type){
            case "Byte":
                return (useNullable ? new NullableByteColumn(size) : new ByteColumn(size));
            case "Short":
                return (useNullable ? new NullableShortColumn(size) : new ShortColumn(size));
            case "Int":
                return (useNullable ? new NullableIntColumn(size) : new IntColumn(size));
            case "Long":
                return (useNullable ? new NullableLongColumn(size) : new LongColumn(size));
            case "String":
                return (useNullable ? new NullableStringColumn(size) : new StringColumn(size));
            case "Float":
                return (useNullable ? new NullableFloatColumn(size) : new FloatColumn(size));
            case "Double":
                return (useNullable ? new NullableDoubleColumn(size) : new DoubleColumn(size));
            case "Char":
                return (useNullable ? new NullableCharColumn(size) : new CharColumn(size));
            case "Boolean":
                return (useNullable ? new NullableBooleanColumn(size) : new BooleanColumn(size));
            case "Binary":
                return (useNullable ? new NullableBinaryColumn(size) : new BinaryColumn(size));
            }
        }
        return null;
    }

    private void showWarnMsg(final String msg){
        OneShotSnackbar.showFor(rootPane, msg);
    }

    private boolean nameIsDuplicate(final String name){
        final String[] names = this.df.getColumnNames();
        if(names != null){
            for(int i=0; i<names.length; ++i){
                if(names[i].equals(name)){ return true; }
            }
        }
        return false;
    }
}
