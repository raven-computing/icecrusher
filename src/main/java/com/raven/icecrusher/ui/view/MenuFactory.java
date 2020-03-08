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

import javafx.scene.control.ContextMenu;

/**
 * Factory interface for producing <code>ContextMenu</code> objects for particular
 * <code>DataFrameColumnViews</code>. Concrete classes must provide an implementation
 * for the <code>createContextMenu()</code> method. 
 * 
 * @see DefaultMenuFactory
 *
 */
public interface MenuFactory {

    /**
     * Factory method which is being called by a <code>DataFrameView</code> during 
     * initialization and reloading to add a specific <code>ContextMenu</code> to
     * each <code>DataFrameColumnView</code>. Implementations have the responsibility 
     * to construct and return an appropriate ContextMenu, which then will be added
     * to the DataFrameColumnView to be accessible to user interactions.
     * 
     * <p>Do not set the ContextMenu to the provided column view directly as this will
     * be handled by the underlying DataFrameView
     * 
     * @param columnView The <code>DataFrameColumnView</code> to create a 
     *                   <code>ContextMenu</code> for
     * @return A fully created and initialized <code>ContextMenu</code> object to be 
     *         used by the provided DataFrameColumnView
     */
    public ContextMenu createContextMenu(DataFrameColumnView columnView);

}
