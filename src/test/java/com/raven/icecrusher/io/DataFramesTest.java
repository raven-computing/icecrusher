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

package com.raven.icecrusher.io;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.raven.common.struct.BinaryColumn;
import com.raven.common.struct.BooleanColumn;
import com.raven.common.struct.Column;
import com.raven.common.struct.DataFrame;
import com.raven.common.struct.DefaultDataFrame;
import com.raven.common.struct.IntColumn;
import com.raven.common.struct.NullableBinaryColumn;
import com.raven.common.struct.NullableBooleanColumn;
import com.raven.common.struct.NullableStringColumn;
import com.raven.common.struct.StringColumn;

public class DataFramesTest {

    @BeforeClass
    public static void setUpBeforeClass(){ }

    @AfterClass
    public static void tearDownAfterClass(){ }

    @Before
    public void setUp(){ }

    @After
    public void tearDown(){ }

    @Test
    public void testSetDefaultColumnNames(){
        DataFrame df = new DefaultDataFrame( 
                Column.create("columnA", 10,20,30,40,50),
                Column.create("columnB", 11,21,31,41,51),
                Column.create("columnC", 12,22,32,42,52),
                Column.create("columnD", 13l,23l,33l,43l,53l),
                Column.create("columnE", "10","20","30","40","50"),
                Column.create("columnF", 'a','b','c','d','e'),
                Column.create("columnG", 10.1f,20.2f,30.3f,40.4f,50.5f),
                Column.create("columnH", 11.1,21.2,31.3,41.4,51.5),
                Column.create("columnI", true,false,true,false,true));
        
        DataFrames.setDefaultColumnNames(df);
        for(int i=0; i<df.columns(); ++i){
            assertTrue(df.getColumn(i).getName().equals(String.valueOf(i)));
        }
        String[] truth = {"0","1","2","3","4","5","6","7","8"};
        assertArrayEquals("Names do not match", truth, df.getColumnNames());
    }

    @Test
    public void testColumnUsesStrings(){
        assertTrue("Function should return true",
                   DataFrames.columnUsesStrings(
                           new StringColumn("colname", new String[]{"AA", "BB", "CC"})));
        
        assertTrue("Function should return true",
                DataFrames.columnUsesStrings(
                        new NullableStringColumn("colname", new String[]{"AA", null, "CC"})));
        
        
        assertFalse("Function should return false",
                DataFrames.columnUsesStrings(
                        new IntColumn("colname", new int[]{11, 22, 33})));
    }

    @Test
    public void testColumnUsesBooleans(){
        assertTrue("Function should return true",
                   DataFrames.columnUsesBooleans(
                           new BooleanColumn("colname", new boolean[]{true, false, true})));
        
        assertTrue("Function should return true",
                DataFrames.columnUsesBooleans(
                        new NullableBooleanColumn("colname", new Boolean[]{true, null, false})));
        
        
        assertFalse("Function should return false",
                DataFrames.columnUsesBooleans(
                        new IntColumn("colname", new int[]{11, 22, 33})));
    }

    @Test
    public void testColumnUsesBinary(){
        assertTrue("Function should return true",
                   DataFrames.columnUsesBinary(
                           new BinaryColumn("colname", new byte[][]{{0x12}, {0x13}, {0x14}})));
        
        assertTrue("Function should return true",
                DataFrames.columnUsesBinary(
                        new NullableBinaryColumn("colname", new byte[][]{{0x12}, null, {0x14}})));
        
        
        assertFalse("Function should return false",
                DataFrames.columnUsesBinary(
                        new IntColumn("colname", new int[]{11, 22, 33})));
    }

}
