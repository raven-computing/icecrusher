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

package com.raven.icecrusher.io;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import com.raven.common.struct.NullableColumn;
import com.raven.common.struct.NullableDoubleColumn;
import com.raven.common.struct.NullableFloatColumn;
import com.raven.common.struct.NullableIntColumn;
import com.raven.common.struct.NullableLongColumn;
import com.raven.common.struct.NullableShortColumn;
import com.raven.common.struct.NullableStringColumn;
import com.raven.common.struct.ShortColumn;
import com.raven.common.struct.StringColumn;
import com.raven.icecrusher.ui.view.DataFrameColumnView.ColumnType;

/**
 * Utility class which provides methods to manipulate DataFrames
 *
 */
public class DataFrames {

    /**
     * The set of allowed strings representing valid (true) booleans during column conversions
     */
    private static final Set<String> SET_VALUES_TRUE = new HashSet<>(
            Arrays.asList(new String[]{"true", "TRUE", "t", "T", "1", "yes", "YES", "y", "Y"}));

    /**
     * The set of allowed strings representing valid (false) booleans during column conversions
     */
    private static final Set<String> SET_VALUES_FALSE = new HashSet<>(
            Arrays.asList(new String[]{"false","FALSE", "f", "F", "0", "no", "NO", "n", "N"}));

    private DataFrames() { }

    /**
     * Sets all column names of the provided <code>DataFrame</code> to their
     * default value, which is the index of that column as a string
     * 
     * @param df The DataFrame to change
     */
    public static void setDefaultColumnNames(final DataFrame df){
        final String[] names = new String[df.columns()];
        for(int i=0; i<names.length; ++i){
            names[i] = String.valueOf(i);
        }
        df.setColumnNames(names);
    }

    /**
     * Changes all entries in all columns of the provided <code>DataFrame</code> to
     * a default value, if and only if, that entry cannot be handled by this implementation
     * of this application or <code>DataFrameView</code>.<br><br>
     * <b><u>IMPLEMENTATION NOTE:</u></b><br>
     * Currently, all DataFrames must have column names set. If a deserialized DataFrame has
     * not set any column names, default values will be added to it.<br>
     * NULL characters in any CharColumn instance cannot be handled and must be replaced.
     * As of this implementation, a NULL character in CharColumns violates the official DataFrame
     * specification but may still be encountered in DataFrame objects originating from older
     * formats. Any NULL character will be replaced by the default placeholder char value
     * 
     * @param df The DataFrame to modify
     * @return The sanitized DataFrame instance
     */
    public static DataFrame sanitize(final DataFrame df){
        if((!df.hasColumnNames()) && (df.columns() != 0)){
            setDefaultColumnNames(df);
        }
        for(final Column col : df){
            if(col.typeCode() == CharColumn.TYPE_CODE){
                final CharColumn chars = (CharColumn)col;
                for(int i=0; i<df.rows(); ++i){
                    if(chars.get(i) == '\u0000'){
                        chars.set(i, CharColumn.DEFAULT_VALUE);
                    }
                }
            }else if(col.typeCode() == NullableCharColumn.TYPE_CODE){
                final NullableCharColumn chars = (NullableCharColumn)col;
                for(int i=0; i<df.rows(); ++i){
                    final Character c = chars.get(i);
                    if((c != null) && (c == '\u0000')){
                        chars.set(i, CharColumn.DEFAULT_VALUE);
                    }
                }
            }
        }
        return df;
    }

    /**
     * Indicates whether the given column uses Strings as its internal data
     * 
     * @param col The column to check for String usage
     * @return True if the specified column uses Strings, false otherwise
     */
    public static boolean columnUsesStrings(final Column col){
        return (col.typeCode() == StringColumn.TYPE_CODE
                || col.typeCode() == NullableStringColumn.TYPE_CODE);
    }

    /**
     * Indicates whether the given column uses booleans as its internal data
     * 
     * @param col The column to check for Boolean usage
     * @return True if the specified column uses booleans, false otherwise
     */
    public static boolean columnUsesBooleans(final Column col){
        return (col.typeCode() == BooleanColumn.TYPE_CODE
                || col.typeCode() == NullableBooleanColumn.TYPE_CODE);
    }

    /**
     * Indicates whether the given column uses binary data as its internal data
     * 
     * @param col The column to check for binary usage
     * @return True if the specified column uses binary data, false otherwise
     */
    public static boolean columnUsesBinary(final Column col){
        return (col.typeCode() == BinaryColumn.TYPE_CODE
                || col.typeCode() == NullableBinaryColumn.TYPE_CODE);
    }

    /**
     * Converts a given <code>Column</code> to the specified type. All elements of 
     * the source column will also be converted to the appropriate type of the target column.<br>
     * If not all elements can be converted successfully, then a <code>ConversionException</code>
     * is thrown to indicate failure to do so
     * 
     * @param source The source <code>Column</code> to convert
     * @param size The size of the source DataFrame column, in other words, the number of rows 
     *             the underlying DataFrame has at the time of conversion
     * @param targetType The type of the new converted column
     * @return A converted <code>Column</code> which has all its internal data 
     *         converted to the target type
     * @throws ConversionException If the provided column or any of its elements cannot be 
     *                             converted to the specified target type
     */
    public static Column convertColumn(final Column source, final int size, 
            final ColumnType targetType) throws ConversionException{

        Column target = null;
        final boolean isNullable = (source instanceof NullableColumn);
        final boolean sourceIsBoolean = ((source instanceof NullableBooleanColumn) 
                || (source instanceof BooleanColumn));

        switch(targetType){
        case BYTE:
            target = (isNullable
                    ? new NullableByteColumn(size)
                    : new ByteColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(sourceIsBoolean){
                    target.setValue(i, ((value != null) ? (((Boolean)value) ? (byte)1 : (byte)0) : null));
                }else{
                    try{
                        target.setValue(i, ((value != null) ? Byte.valueOf(value.toString()) : null));
                    }catch(NumberFormatException ex){
                        throw ConversionException.with("Invalid byte", value, i);
                    }
                }
            }
            break;
        case SHORT:
            target = (isNullable
                    ? new NullableShortColumn(size)
                    : new ShortColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(sourceIsBoolean){
                    target.setValue(i, ((value != null) ? (((Boolean)value) ? (short)1 : (short)0) : null));
                }else{
                    try{
                        target.setValue(i, ((value != null) ? Short.valueOf(value.toString()) : null));
                    }catch(NumberFormatException ex){
                        throw ConversionException.with("Invalid short", value, i);
                    }
                }
            }
            break;
        case INT:
            target = (isNullable
                    ? new NullableIntColumn(size)
                    : new IntColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(sourceIsBoolean){
                    target.setValue(i, ((value != null) ? (((Boolean)value) ? 1 : 0) : null));
                }else{
                    try{
                        target.setValue(i, ((value != null) ? Integer.valueOf(value.toString()) : null));
                    }catch(NumberFormatException ex){
                        throw ConversionException.with("Invalid integer", value, i);
                    }
                }
            }
            break;
        case LONG:
            target = (isNullable
                    ? new NullableLongColumn(size)
                    : new LongColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(sourceIsBoolean){
                    target.setValue(i, ((value != null) ? (((Boolean)value) ? 1l : 0l) : null));
                }else{
                    try{
                        target.setValue(i, ((value != null) ? Long.valueOf(value.toString()) : null));
                    }catch(NumberFormatException ex){
                        throw ConversionException.with("Invalid long", value, i);
                    }
                }
            }
            break;
        case STRING:
            target = (isNullable
                    ? new NullableStringColumn(size)
                    : new StringColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                target.setValue(i, ((value != null) ? value.toString() : null));
            }
            break;
        case FLOAT:
            target = (isNullable
                    ? new NullableFloatColumn(size)
                    : new FloatColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(sourceIsBoolean){
                    target.setValue(i, ((value != null) ? (((Boolean)value) ? 1.0f : 0.0f) : null));
                }else{
                    try{
                        target.setValue(i, ((value != null) ? Float.valueOf(value.toString()) : null));
                    }catch(NumberFormatException ex){
                        throw ConversionException.with("Invalid float", value, i);
                    }
                }
            }
            break;
        case DOUBLE:
            target = (isNullable
                    ? new NullableDoubleColumn(size)
                    : new DoubleColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(sourceIsBoolean){
                    target.setValue(i, ((value != null) ? (((Boolean)value) ? 1.0 : 0.0) : null));
                }else{
                    try{
                        target.setValue(i, ((value != null) ? Double.valueOf(value.toString()) : null));
                    }catch(NumberFormatException ex){
                        throw ConversionException.with("Invalid double", value, i);
                    }
                }
            }
            break;
        case CHAR:
            target = (isNullable 
                    ? new NullableCharColumn(size)
                    : new CharColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(value == null){
                    target.setValue(i, null);
                }else{
                    final String s = value.toString();
                    if(sourceIsBoolean){
                        target.setValue(i, ((value != null) ? (((Boolean)value) ? 'T' : 'F') : null));
                    }else{
                        if(s.length() > 1){
                            throw ConversionException.with("Invalid character", s, i);
                        }
                        target.setValue(i, s.charAt(0));
                    }
                }
            }
            break;
        case BOOLEAN:
            target = (isNullable
                    ? new NullableBooleanColumn(size) 
                    : new BooleanColumn(size));
            
            for(int i=0; i<size; ++i){
                final Object value = source.getValue(i);
                if(value == null){
                    target.setValue(i, null);
                }else{
                    final String s = value.toString();
                    final boolean isTrue = SET_VALUES_TRUE.contains(s);
                    final boolean isFalse = SET_VALUES_FALSE.contains(s);
                    if(!isTrue && !isFalse){
                        throw ConversionException.with("Invalid boolean", s, i);
                    }
                    target.setValue(i, isTrue);
                }
            }
            break;
        default:
            break;
        }
        return target;
    }
}
