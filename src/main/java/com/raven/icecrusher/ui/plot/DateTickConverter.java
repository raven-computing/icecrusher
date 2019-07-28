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

import java.time.LocalDate;

import javafx.util.StringConverter;

/**
 * Converter to convert x-axis numerical data to the corresponding formatted date string.
 *
 */
public class DateTickConverter extends StringConverter<Number> {
	
	private LocalDate tZero;
	
	public DateTickConverter(final LocalDate tZero){
		this.tZero = tZero;
	}

	@Override
	public String toString(Number object){
	    return "  "+tZero.plusDays(((int)((double)object))).format(XYChartController.DATE_FORMATTER_TICKS)+"  ";
	}

	@Override
	public Number fromString(String string){
		return null;
	}

}
