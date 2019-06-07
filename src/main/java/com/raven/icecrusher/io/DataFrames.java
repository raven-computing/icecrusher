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

package com.raven.icecrusher.io;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.raven.common.struct.BooleanColumn;
import com.raven.common.struct.ByteColumn;
import com.raven.common.struct.CharColumn;
import com.raven.common.struct.Column;
import com.raven.common.struct.DataFrame;
import com.raven.common.struct.DoubleColumn;
import com.raven.common.struct.FloatColumn;
import com.raven.common.struct.IntColumn;
import com.raven.common.struct.LongColumn;
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
	 * As of this implementation, a NULL character will be replaced by a dash
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
						chars.set(i, '-');
					}
				}
			}else if(col.typeCode() == NullableCharColumn.TYPE_CODE){
				final NullableCharColumn chars = (NullableCharColumn)col;
				for(int i=0; i<df.rows(); ++i){
					if(chars.get(i) == '\u0000'){
						chars.set(i, '-');
					}
				}
			}
		}
		return df;
	}

	/**
	 * Indicates whether the given column uses NaNs as its internal data
	 * 
	 * @param col The column to check for NaN usage
	 * @return True if the specified column uses NaNs, false otherwise
	 */
	public static boolean columnUsesNaNs(final Column col){
		final byte typeCode = col.typeCode();
		return (typeCode == StringColumn.TYPE_CODE
				|| typeCode == CharColumn.TYPE_CODE
				|| typeCode == BooleanColumn.TYPE_CODE
				|| typeCode == NullableStringColumn.TYPE_CODE
				|| typeCode == NullableCharColumn.TYPE_CODE
				|| typeCode == NullableBooleanColumn.TYPE_CODE);
	}

	/**
	 * Indicates whether the given column uses Strings as its internal data
	 * 
	 * @param col The column to check for String usage
	 * @return True if the specified column uses Strings, false otherwise
	 */
	public static boolean columnUsesStrings(final Column col){
		return (col.typeCode() == StringColumn.TYPE_CODE || col.typeCode() == NullableStringColumn.TYPE_CODE);
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
			target = (isNullable ? new NullableByteColumn(new Byte[size]) : new ByteColumn(new byte[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(sourceIsBoolean){
					target.setValueAt(i, ((value != null) ? (((Boolean)value) ? (byte)1 : (byte)0) : null));
				}else{
					try{
						target.setValueAt(i, ((value != null) ? Byte.valueOf(value.toString()) : null));
					}catch(NumberFormatException ex){
						throw ConversionException.with("Invalid byte", value, i);
					}
				}
			}
			break;
		case SHORT:
			target = (isNullable ? new NullableShortColumn(new Short[size]) : new ShortColumn(new short[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(sourceIsBoolean){
					target.setValueAt(i, ((value != null) ? (((Boolean)value) ? (short)1 : (short)0) : null));
				}else{
					try{
						target.setValueAt(i, ((value != null) ? Short.valueOf(value.toString()) : null));
					}catch(NumberFormatException ex){
						throw ConversionException.with("Invalid short", value, i);
					}
				}
			}
			break;
		case INT:
			target = (isNullable ? new NullableIntColumn(new Integer[size]) : new IntColumn(new int[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(sourceIsBoolean){
					target.setValueAt(i, ((value != null) ? (((Boolean)value) ? 1 : 0) : null));
				}else{
					try{
						target.setValueAt(i, ((value != null) ? Integer.valueOf(value.toString()) : null));
					}catch(NumberFormatException ex){
						throw ConversionException.with("Invalid integer", value, i);
					}
				}
			}
			break;
		case LONG:
			target = (isNullable ? new NullableLongColumn(new Long[size]) : new LongColumn(new long[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(sourceIsBoolean){
					target.setValueAt(i, ((value != null) ? (((Boolean)value) ? 1l : 0l) : null));
				}else{
					try{
						target.setValueAt(i, ((value != null) ? Long.valueOf(value.toString()) : null));
					}catch(NumberFormatException ex){
						throw ConversionException.with("Invalid long", value, i);
					}
				}
			}
			break;
		case STRING:
			target = (isNullable ? new NullableStringColumn(new String[size]) : new StringColumn(new String[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				target.setValueAt(i, ((value != null) ? value.toString() : null));
			}
			break;
		case FLOAT:
			target = (isNullable ? new NullableFloatColumn(new Float[size]) : new FloatColumn(new float[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(sourceIsBoolean){
					target.setValueAt(i, ((value != null) ? (((Boolean)value) ? 1.0f : 0.0f) : null));
				}else{
					try{
						target.setValueAt(i, ((value != null) ? Float.valueOf(value.toString()) : null));
					}catch(NumberFormatException ex){
						throw ConversionException.with("Invalid float", value, i);
					}
				}
			}
			break;
		case DOUBLE:
			target = (isNullable ? new NullableDoubleColumn(new Double[size]) : new DoubleColumn(new double[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(sourceIsBoolean){
					target.setValueAt(i, ((value != null) ? (((Boolean)value) ? 1.0 : 0.0) : null));
				}else{
					try{
						target.setValueAt(i, ((value != null) ? Double.valueOf(value.toString()) : null));
					}catch(NumberFormatException ex){
						throw ConversionException.with("Invalid double", value, i);
					}
				}
			}
			break;
		case CHAR:
			target = (isNullable ? new NullableCharColumn(new Character[size]) : new CharColumn(new char[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(value == null){
					target.setValueAt(i, null);
				}else{
					final String s = value.toString();
					if(sourceIsBoolean){
						target.setValueAt(i, ((value != null) ? (((Boolean)value) ? 'T' : 'F') : null));
					}else{
						if(s.length() > 1){
							throw ConversionException.with("Invalid character", s, i);
						}
						target.setValueAt(i, s.charAt(0));
					}
				}
			}
			break;
		case BOOLEAN:
			target = (isNullable ? new NullableBooleanColumn(new Boolean[size]) : new BooleanColumn(new boolean[size]));
			for(int i=0; i<size; ++i){
				final Object value = source.getValueAt(i);
				if(value == null){
					target.setValueAt(i, null);
				}else{
					final String s = value.toString();
					final boolean isTrue = SET_VALUES_TRUE.contains(s);
					final boolean isFalse = SET_VALUES_FALSE.contains(s);
					if(!isTrue && !isFalse){
						throw ConversionException.with("Invalid boolean", s, i);
					}
					target.setValueAt(i, isTrue);
				}
			}
			break;
		}
		return target;
	}

}
