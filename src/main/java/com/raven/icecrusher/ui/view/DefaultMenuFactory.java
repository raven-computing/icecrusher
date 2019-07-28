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

import java.util.ArrayList;
import java.util.List;

import com.raven.common.struct.BooleanColumn;
import com.raven.common.struct.ByteColumn;
import com.raven.common.struct.CharColumn;
import com.raven.common.struct.Column;
import com.raven.common.struct.DoubleColumn;
import com.raven.common.struct.FloatColumn;
import com.raven.common.struct.IntColumn;
import com.raven.common.struct.LongColumn;
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
import com.raven.icecrusher.ui.view.DataFrameColumnView.ColumnType;
import com.raven.icecrusher.ui.view.DataFrameView.ContextMenuEvent;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * Default implementation for the {@link MenuFactory} interface.<br>
 * This implementation provides standard context menus to all supported
 * DataFrameColumnViews.
 *
 */
public class DefaultMenuFactory implements MenuFactory {

    public static final String MENU_TITLE_RENAME        = "Rename";
    public static final String MENU_TITLE_SORT          = "Sort";
    public static final String MENU_TITLE_DELETE        = "Delete";
    public static final String MENU_TITLE_STATS         = "Show stats";
    public static final String MENU_TITLE_CONVERT       = "Convert to";

    public static final String MENU_ITEM_TITLE_BYTES    = "bytes";
    public static final String MENU_ITEM_TITLE_SHORTS   = "shorts";
    public static final String MENU_ITEM_TITLE_INTS     = "ints";
    public static final String MENU_ITEM_TITLE_LONGS    = "longs";
    public static final String MENU_ITEM_TITLE_STRINGS  = "strings";
    public static final String MENU_ITEM_TITLE_FLOATS   = "floats";
    public static final String MENU_ITEM_TITLE_DOUBLES  = "doubles";
    public static final String MENU_ITEM_TITLE_CHARS    = "chars";
    public static final String MENU_ITEM_TITLE_BOOLEANS = "booleans";

    public DefaultMenuFactory(){ }

    @Override
    public ContextMenu createContextMenu(final DataFrameColumnView columnView){
        final Column column = columnView.getColumn();
        final boolean showStats = !columnUsesNaNs(column);

        final MenuItem item1 = new MenuItem(MENU_TITLE_RENAME);
        item1.setOnAction((event) -> {
            columnView.getDataFrameView().action(ContextMenuEvent.Action.RENAME, 
                    columnView.getText(), null);
        });
        final MenuItem item2 = new MenuItem(MENU_TITLE_SORT);
        item2.setOnAction((event) -> {
            columnView.getDataFrameView().action(ContextMenuEvent.Action.SORT, 
                    columnView.getText(), null);
        });
        MenuItem item3 = null;
        if(showStats){
            item3 = new MenuItem(MENU_TITLE_STATS);
            item3.setOnAction((event) -> {
                columnView.getDataFrameView().action(ContextMenuEvent.Action.STATS, 
                        columnView.getText(), null);
            });
        }
        final MenuItem item4 = new MenuItem(MENU_TITLE_DELETE);
        item4.setOnAction((event) -> {
            columnView.getDataFrameView().action(ContextMenuEvent.Action.DELETE, 
                    columnView.getText(), null);
        });
        final Menu menuConvert = createConvertSubmenu(columnView, column);
        final ContextMenu menu = (showStats 
                ? new ContextMenu(item1, item2, item3, menuConvert, item4)
                        : new ContextMenu(item1, item2, menuConvert, item4));

        return menu;
    }

    private Menu createConvertSubmenu(final DataFrameColumnView columnView, final Column column){
        final Menu menu = new Menu(MENU_TITLE_CONVERT);
        //bisect lookup
        if(column.isNullable()){	
            switch(column.typeCode()){
            case NullableByteColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForBytes(columnView));
                break;
            case NullableShortColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForShorts(columnView));
                break;
            case NullableIntColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForInts(columnView));
                break;
            case NullableLongColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForLongs(columnView));
                break;
            case NullableStringColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForStrings(columnView));
                break;
            case NullableFloatColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForFloats(columnView));
                break;
            case NullableDoubleColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForDoubles(columnView));
                break;
            case NullableCharColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForChars(columnView));
                break;
            case NullableBooleanColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForBooleans(columnView));
                break;
            default:

            }
        }else{
            switch(column.typeCode()){
            case ByteColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForBytes(columnView));
                break;
            case ShortColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForShorts(columnView));
                break;
            case IntColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForInts(columnView));
                break;
            case LongColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForLongs(columnView));
                break;
            case StringColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForStrings(columnView));
                break;
            case FloatColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForFloats(columnView));
                break;
            case DoubleColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForDoubles(columnView));
                break;
            case CharColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForChars(columnView));
                break;
            case BooleanColumn.TYPE_CODE:
                menu.getItems().addAll(menuItemsForBooleans(columnView));
                break;
            default:

            }
        }
        return menu;
    }

    private List<MenuItem> menuItemsForBytes(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(8);
        list.add(createMenuItemForShorts(columnView));
        list.add(createMenuItemForInts(columnView));
        list.add(createMenuItemForLongs(columnView));
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForFloats(columnView));
        list.add(createMenuItemForDoubles(columnView));
        list.add(createMenuItemForChars(columnView));
        list.add(createMenuItemForBooleans(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForShorts(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(8);
        list.add(createMenuItemForBytes(columnView));
        list.add(createMenuItemForInts(columnView));
        list.add(createMenuItemForLongs(columnView));
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForFloats(columnView));
        list.add(createMenuItemForDoubles(columnView));
        list.add(createMenuItemForChars(columnView));
        list.add(createMenuItemForBooleans(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForInts(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(8);
        list.add(createMenuItemForBytes(columnView));
        list.add(createMenuItemForShorts(columnView));
        list.add(createMenuItemForLongs(columnView));
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForFloats(columnView));
        list.add(createMenuItemForDoubles(columnView));
        list.add(createMenuItemForChars(columnView));
        list.add(createMenuItemForBooleans(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForLongs(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(8);
        list.add(createMenuItemForBytes(columnView));
        list.add(createMenuItemForShorts(columnView));
        list.add(createMenuItemForInts(columnView));
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForFloats(columnView));
        list.add(createMenuItemForDoubles(columnView));
        list.add(createMenuItemForChars(columnView));
        list.add(createMenuItemForBooleans(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForStrings(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(8);
        list.add(createMenuItemForBytes(columnView));
        list.add(createMenuItemForShorts(columnView));
        list.add(createMenuItemForInts(columnView));
        list.add(createMenuItemForLongs(columnView));
        list.add(createMenuItemForFloats(columnView));
        list.add(createMenuItemForDoubles(columnView));
        list.add(createMenuItemForChars(columnView));
        list.add(createMenuItemForBooleans(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForFloats(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(2);
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForDoubles(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForDoubles(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(2);
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForFloats(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForChars(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(8);
        list.add(createMenuItemForBytes(columnView));
        list.add(createMenuItemForShorts(columnView));
        list.add(createMenuItemForInts(columnView));
        list.add(createMenuItemForLongs(columnView));
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForFloats(columnView));
        list.add(createMenuItemForDoubles(columnView));
        list.add(createMenuItemForBooleans(columnView));
        return list;
    }

    private List<MenuItem> menuItemsForBooleans(final DataFrameColumnView columnView){
        final List<MenuItem> list = new ArrayList<>(8);
        list.add(createMenuItemForBytes(columnView));
        list.add(createMenuItemForShorts(columnView));
        list.add(createMenuItemForInts(columnView));
        list.add(createMenuItemForLongs(columnView));
        list.add(createMenuItemForStrings(columnView));
        list.add(createMenuItemForFloats(columnView));
        list.add(createMenuItemForDoubles(columnView));
        list.add(createMenuItemForChars(columnView));
        return list;
    }

    private MenuItem createMenuItemForBytes(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_BYTES);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.BYTE));

        return item;
    }

    private MenuItem createMenuItemForShorts(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_SHORTS);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.SHORT));

        return item;
    }

    private MenuItem createMenuItemForInts(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_INTS);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.INT));

        return item;
    }

    private MenuItem createMenuItemForLongs(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_LONGS);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.LONG));

        return item;
    }

    private MenuItem createMenuItemForStrings(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_STRINGS);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.STRING));

        return item;
    }

    private MenuItem createMenuItemForFloats(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_FLOATS);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.FLOAT));

        return item;
    }

    private MenuItem createMenuItemForDoubles(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_DOUBLES);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.DOUBLE));

        return item;
    }

    private MenuItem createMenuItemForChars(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_CHARS);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.CHAR));

        return item;
    }

    private MenuItem createMenuItemForBooleans(final DataFrameColumnView columnView){
        final MenuItem item = new MenuItem(MENU_ITEM_TITLE_BOOLEANS);
        item.setOnAction((e) -> columnView.getDataFrameView()
                .action(ContextMenuEvent.Action.CONVERT, columnView.getText(), ColumnType.BOOLEAN));

        return item;
    }

    private boolean columnUsesNaNs(final Column col){
        final byte typeCode = col.typeCode();
        return (typeCode == StringColumn.TYPE_CODE
                || typeCode == CharColumn.TYPE_CODE
                || typeCode == BooleanColumn.TYPE_CODE
                || typeCode == NullableStringColumn.TYPE_CODE
                || typeCode == NullableCharColumn.TYPE_CODE
                || typeCode == NullableBooleanColumn.TYPE_CODE);

    }

}
