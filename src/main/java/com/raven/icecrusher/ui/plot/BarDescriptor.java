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

package com.raven.icecrusher.ui.plot;

/**
 * Wrapper class for holding data about used columns in Bar Charts.<br>
 * A used bar is defined by the name of the y-axis column and the 
 * applied operation mode.
 *
 */
public class BarDescriptor {

	private String column;
	private int mode;

	private BarDescriptor(final String column, final int mode){
		this.column = column;
		this.mode = mode;
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof BarDescriptor)){
			return false;
		}
		final BarDescriptor bar = (BarDescriptor)obj;
		return ((this.column.equals(bar.getColumn())) 
				&& (this.mode == bar.getMode()));
	}
	
	public String getColumn(){
		return column;
	}

	public void setColumn(final String column){
		this.column = column;
	}

	public int getMode(){
		return mode;
	}

	public void setMode(final int mode){
		this.mode = mode;
	}
	
	public static BarDescriptor from(final String column, final int mode){
		return new BarDescriptor(column, mode);
	}
	
}
