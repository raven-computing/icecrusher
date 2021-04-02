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

import com.raven.common.struct.BinaryColumn;
import com.raven.common.struct.BooleanColumn;
import com.raven.common.struct.ByteColumn;
import com.raven.common.struct.CharColumn;
import com.raven.common.struct.Column;
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

import javafx.scene.control.Tooltip;

/**
 * Utility class providing tooltips for DataFrameView table cells. <br>
 * This class cannot be instantiated. Use one of the <code>*Tooltip()</code> methods
 * instead.
 *
 */
public class Tooltips {

    private static Tooltip byteTooltip      = new Tooltip("byte");
    private static Tooltip shortTooltip     = new Tooltip("short");
    private static Tooltip intTooltip       = new Tooltip("int");
    private static Tooltip longTooltip      = new Tooltip("long");
    private static Tooltip stringTooltip    = new Tooltip("string");
    private static Tooltip floatTooltip     = new Tooltip("float");
    private static Tooltip doubleTooltip    = new Tooltip("double");
    private static Tooltip charTooltip      = new Tooltip("char");
    private static Tooltip booleanTooltip   = new Tooltip("boolean");
    private static Tooltip binaryTooltip    = new Tooltip("binary");

    private Tooltips(){ }

    /**
     * Returns a reference to a Tooltip responsible for byte columns
     * 
     * @return A Tooltip
     */
    public static Tooltip byteTooltip(){
        return byteTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for short columns
     * 
     * @return A Tooltip
     */
    public static Tooltip shortTooltip(){
        return shortTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for int columns
     * 
     * @return A Tooltip
     */
    public static Tooltip intTooltip(){
        return intTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for long columns
     * 
     * @return A Tooltip
     */
    public static Tooltip longTooltip(){
        return longTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for string columns
     * 
     * @return A Tooltip
     */
    public static Tooltip stringTooltip(){
        return stringTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for float columns
     * 
     * @return A Tooltip
     */
    public static Tooltip floatTooltip(){
        return floatTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for double columns
     * 
     * @return A Tooltip
     */
    public static Tooltip doubleTooltip(){
        return doubleTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for character columns
     * 
     * @return A Tooltip
     */
    public static Tooltip charTooltip(){
        return charTooltip;
    }

    /**
     * Returns a reference to a Tooltip responsible for boolean columns
     * 
     * @return A Tooltip
     */
    public static Tooltip booleanTooltip(){
        return booleanTooltip;
    }
    
    /**
     * Returns a reference to a Tooltip responsible for binary columns
     * 
     * @return A Tooltip
     */
    public static Tooltip binaryTooltip(){
        return binaryTooltip;
    }
    
    /**
     * Returns the appropriate <code>Tooltip</code> object for the specified column
     * 
     * @param col The <code>Column</code> object to get a Tooltip for
     * @return The <code>Tooltip</code> to be used with the specified column
     */
    public static Tooltip columnTooltip(final Column col){
        if(col.isNullable()){
            switch(col.typeCode()){
            case NullableStringColumn.TYPE_CODE:
                return stringTooltip();
            case NullableByteColumn.TYPE_CODE:
                return byteTooltip();
            case NullableShortColumn.TYPE_CODE:
                return shortTooltip();
            case NullableIntColumn.TYPE_CODE:
                return intTooltip();
            case NullableLongColumn.TYPE_CODE:
                return longTooltip();
            case NullableFloatColumn.TYPE_CODE:
                return floatTooltip();
            case NullableDoubleColumn.TYPE_CODE:
                return doubleTooltip();
            case NullableBooleanColumn.TYPE_CODE:
                return booleanTooltip();
            case NullableCharColumn.TYPE_CODE:
                return charTooltip();
            case NullableBinaryColumn.TYPE_CODE:
                return binaryTooltip();
            }
        }else{
            switch(col.typeCode()){
            case StringColumn.TYPE_CODE:
                return stringTooltip();
            case ByteColumn.TYPE_CODE:
                return byteTooltip();
            case ShortColumn.TYPE_CODE:
                return shortTooltip();
            case IntColumn.TYPE_CODE:
                return intTooltip();
            case LongColumn.TYPE_CODE:
                return longTooltip();
            case FloatColumn.TYPE_CODE:
                return floatTooltip();
            case DoubleColumn.TYPE_CODE:
                return doubleTooltip();
            case BooleanColumn.TYPE_CODE:
                return booleanTooltip();
            case CharColumn.TYPE_CODE:
                return charTooltip();
            case BinaryColumn.TYPE_CODE:
                return binaryTooltip();
            }
        }
        return null;
    }
}
