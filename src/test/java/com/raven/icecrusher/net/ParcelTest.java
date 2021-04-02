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

package com.raven.icecrusher.net;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParcelTest {

    @BeforeClass
    public static void setUpBeforeClass(){ }

    @AfterClass
    public static void tearDownAfterClass(){ }

    @Before
    public void setUp(){ }

    @After
    public void tearDown(){ }

    @Test
    public void testInitializeParcelWithRawBytes(){
        byte[] data = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07};
        Parcel parcel = new Parcel(data);
        assertArrayEquals(data, parcel.getBytes());
    }

    @Test
    public void testInitializeParcelWithText(){
        String data = "This is a text message";
        Parcel parcel = new Parcel(data);
        assertEquals(data, parcel.getString());
    }

    @Test
    public void testParcelHasHeader(){
        Parcel parcel = new Parcel();
        parcel.addHeader("mykey1", "myvalue1");
        parcel.addHeader("mykey2", "myvalue2");
        assertEquals("myvalue1", parcel.getHeader().getField("mykey1").getValue());
        assertEquals("myvalue2", parcel.getHeader().getField("mykey2").getValue());
    }

    @Test
    public void testParcelHasEmptyPayload(){
        Parcel parcel = new Parcel();
        assertTrue(parcel.payloadIsEmpty());
    }
}
