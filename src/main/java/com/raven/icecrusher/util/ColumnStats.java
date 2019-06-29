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

package com.raven.icecrusher.util;

import java.math.BigDecimal;

import com.raven.common.struct.Column;
import com.raven.common.struct.NullableByteColumn;
import com.raven.common.struct.NullableShortColumn;
import com.raven.common.struct.NullableIntColumn;
import com.raven.common.struct.NullableLongColumn;
import com.raven.common.struct.NullableFloatColumn;
import com.raven.common.struct.NullableDoubleColumn;
import com.raven.common.struct.ByteColumn;
import com.raven.common.struct.ShortColumn;
import com.raven.common.struct.IntColumn;
import com.raven.common.struct.LongColumn;
import com.raven.common.struct.FloatColumn;
import com.raven.common.struct.DoubleColumn;

/**
 * Holds statistics about a numerical Column and provides a method to compute 
 * the sum of all entries with overflow conscience code since that functionality 
 * is missing in the DataFrame API.
 *
 */
public class ColumnStats {

	private String columnName;
	private double min;
	private double max;
	private double avg;
	private double sum;
	private boolean usesDecimals;
	private boolean sumOverflow;

	public ColumnStats(){ }
	
	/**
	 * Computes the sum of all (non-null) entries in the given Column and 
	 * saves it in the internal member field
	 * 
	 * @param col The <code>Column</code> to compute the sum for
	 * @return The computed sum of all entries in the specified column, exluding null values
	 */
	public double computeSumFor(final Column col){
		if(col.isNullable()){
			switch(col.typeCode()){
			case NullableByteColumn.TYPE_CODE:{
				long sum = 0;
				final Byte[] array = ((NullableByteColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					if(array[i] != null){
						final long tmp = sum;
						sum += array[i];
						if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
							this.sumOverflow = true;
						}
					}
				}
				this.sum = (double)sum;
			}
			break;
			case NullableShortColumn.TYPE_CODE:{
				long sum = 0;
				final Short[] array = ((NullableShortColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					if(array[i] != null){
						final long tmp = sum;
						sum += array[i];
						if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
							this.sumOverflow = true;
						}
					}
				}
				this.sum = (double)sum;
			}
			break;
			case NullableIntColumn.TYPE_CODE:{
				long sum = 0;
				final Integer[] array = ((NullableIntColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					if(array[i] != null){
						final long tmp = sum;
						sum += array[i];
						if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
							this.sumOverflow = true;
						}
					}
				}
				this.sum = (double)sum;
			}
			break;
			case NullableLongColumn.TYPE_CODE:{
				long sum = 0;
				final Long[] array = ((NullableLongColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					if(array[i] != null){
						final long tmp = sum;
						sum += array[i];
						if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
							this.sumOverflow = true;
						}
					}
				}
				this.sum = (double)sum;
			}
			break;
			case NullableFloatColumn.TYPE_CODE:{
				BigDecimal sum = BigDecimal.ZERO;
				final Float[] array = ((NullableFloatColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					if(array[i] != null){
						sum = sum.add(new BigDecimal(Float.toString(array[i])));
					}
				}
				this.sum = sum.doubleValue();
			}
			break;
			case NullableDoubleColumn.TYPE_CODE:{
				BigDecimal sum = BigDecimal.ZERO;
				final Double[] array = ((NullableDoubleColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					if(array[i] != null){
						sum = sum.add(new BigDecimal(Double.toString(array[i])));
					}
				}
				this.sum = sum.doubleValue();
			}
			break;
			}
		}else{
			switch(col.typeCode()){
			case ByteColumn.TYPE_CODE:{
				long sum = 0;
				final byte[] array = ((ByteColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					final long tmp = sum;
					sum += array[i];
					if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
						this.sumOverflow = true;
					}
				}
				this.sum = (double)sum;
			}
			break;
			case ShortColumn.TYPE_CODE:{
				long sum = 0;
				final short[] array = ((ShortColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					final long tmp = sum;
					sum += array[i];
					if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
						this.sumOverflow = true;
					}
				}
				this.sum = (double)sum;
			}
			break;
			case IntColumn.TYPE_CODE:{
				long sum = 0;
				final int[] array = ((IntColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					final long tmp = sum;
					sum += array[i];
					if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
						this.sumOverflow = true;
					}
				}
				this.sum = (double)sum;
			}
			break;
			case LongColumn.TYPE_CODE:{
				long sum = 0;
				final long[] array = ((LongColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					final long tmp = sum;
					sum += array[i];
					if(((tmp ^ sum) & (array[i] ^ sum)) < 0){
						this.sumOverflow = true;
					}
				}
				this.sum = (double)sum;
			}
			break;
			case FloatColumn.TYPE_CODE:{
				BigDecimal sum = BigDecimal.ZERO;
				final float[] array = ((FloatColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					sum = sum.add(new BigDecimal(Float.toString(array[i])));
				}
				this.sum = sum.doubleValue();
			}
			break;
			case DoubleColumn.TYPE_CODE:{
				BigDecimal sum = BigDecimal.ZERO;
				final double[] array = ((DoubleColumn)col).asArray();
				for(int i=0; i<array.length; ++i){
					sum = sum.add(new BigDecimal(Double.toString(array[i])));
				}
				this.sum = sum.doubleValue();
			}
			break;
			}
		}
		return this.sum;
	}
	
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

	public boolean isSumOverflow(){
		return this.sumOverflow;
	}

	public void setSumOverflow(final boolean sumOverflow){
		this.sumOverflow = sumOverflow;
	}

}
