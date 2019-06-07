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

package com.raven.icecrusher.ui.view;

import com.raven.icecrusher.ui.view.Converters.Converter;

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.cell.ComboBoxTableCell;

/**
 * Base class for all narrow writable <code>DataFrameView</code> Cells.<br>
 * The options provided by such cells are defined during their construction and
 * are visually displayed to the user by a combo box.
 *
 */
public class ComboBoxCellView extends ComboBoxTableCell<Integer, Object> {
	
	/** Negates the pseudo class 'selected' **/
	private static final PseudoClass PSEUDO_CLASS_UNSELECTED =
            PseudoClass.getPseudoClass("unselected");
	
	public ComboBoxCellView(final Converter converter, final ObservableList<Object> items){
		super(converter, items);
		//initial state is always unselected
		pseudoClassStateChanged(PSEUDO_CLASS_UNSELECTED, true);
		selectedProperty().addListener((ov, oldVal, isSelected) -> {
			pseudoClassStateChanged(PSEUDO_CLASS_UNSELECTED, !isSelected);
		});
	}

}
