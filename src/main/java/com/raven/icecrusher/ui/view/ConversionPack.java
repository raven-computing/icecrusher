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
import com.raven.icecrusher.ui.view.Converters.Converter;
import com.raven.icecrusher.ui.view.Filters.Filter;

/**
 * Wrapper for holding filters and converters to be used in DataFrameView table cells.
 *
 */
public class ConversionPack {

    private Filter filter;
    private Converter converter;

    public ConversionPack(Filter filter, Converter converter){
        this.filter = filter;
        this.converter = converter;
    }

    /**
     * Gets the filter of this pack
     * 
     * @return A reference to a Filter
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Gets the converter of this pack
     * 
     * @return A reference to a StringConverter
     */
    public Converter getConverter() {
        return this.converter;
    }

    /**
     * Creates a <code>ConversionPack</code> holding the appropriate
     * filters and converters for a cell used in DataFrameViews. The
     * choice which filters and converters to pick is made according
     * to the column's type
     * 
     * @param col The <code>Column</code> for which to get a ConversionPack
     * @return The <code>ConversionPack</code> to be used for this Column
     */
    public static ConversionPack columnConversion(final Column col){
        if(col.isNullable()){
            switch(col.typeCode()){
            case NullableStringColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.stringFilter(true), Converters.stringConverter());

            case NullableByteColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.byteFilter(true), Converters.byteConverter());

            case NullableShortColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.shortFilter(true), Converters.shortConverter());

            case NullableIntColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.intFilter(true), Converters.intConverter());

            case NullableLongColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.longFilter(true), Converters.longConverter());

            case NullableFloatColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.floatFilter(true), Converters.floatConverter());

            case NullableDoubleColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.doubleFilter(true), Converters.doubleConverter());

            case NullableBooleanColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.booleanFilter(true), Converters.booleanConverter());

            case NullableCharColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.charFilter(true), Converters.charConverter());

            case NullableBinaryColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.binaryFilter(true), Converters.binaryTruncatingConverter());

            }
        }else{
            switch(col.typeCode()){
            case StringColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.stringFilter(false), Converters.stringConverter());

            case ByteColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.byteFilter(false), Converters.byteConverter());

            case ShortColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.shortFilter(false), Converters.shortConverter());

            case IntColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.intFilter(false), Converters.intConverter());

            case LongColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.longFilter(false), Converters.longConverter());

            case FloatColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.floatFilter(false), Converters.floatConverter());

            case DoubleColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.doubleFilter(false), Converters.doubleConverter());

            case BooleanColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.booleanFilter(false), Converters.booleanConverter());

            case CharColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.charFilter(false), Converters.charConverter());

            case BinaryColumn.TYPE_CODE:
                return new ConversionPack(
                        Filters.binaryFilter(false), Converters.binaryTruncatingConverter());

            }
        }
        return null;
    }
}
