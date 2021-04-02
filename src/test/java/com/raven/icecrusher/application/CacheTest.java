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

package com.raven.icecrusher.application;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the Cache class.
 *
 */
public class CacheTest {
        
    static Cache cache1;
    Cache cache2;

    @BeforeClass
    public static void setUpBeforeClass(){
        cache1 = new SessionCache(500);
        cache1.set("this.should.be", "cached.across.sessions");
        cache1.set("this.also", "myvalue");
    }

    @AfterClass
    public static void tearDownAfterClass(){
        cache1.clear();
    }

    @Before
    public void setUp(){
        cache2 = new SessionCache(500);
        cache2.set("local.value", "42");
    }

    @After
    public void tearDown(){
        cache2.clear();
    }

    @Test
    public void testGlobalCache1(){
        String value = cache1.get("this.should.be");
        assertTrue("Value mismatch", value.equals("cached.across.sessions"));
        value = cache1.get("this.also");
        assertTrue("Value mismatch", value.equals("myvalue"));
    }

    @Test
    public void testGlobalCache2(){
        String value = cache1.get("this.should.be");
        assertTrue("Value mismatch", value.equals("cached.across.sessions"));
        value = cache1.get("this.also");
        assertTrue("Value mismatch", value.equals("myvalue"));
    }

    @Test
    public void testGlobalCache3(){
        String value = cache1.get("this.should.be");
        assertTrue("Value mismatch", value.equals("cached.across.sessions"));
        value = cache1.get("this.also");
        assertTrue("Value mismatch", value.equals("myvalue"));
    }

    @Test
    public void testLocalCache(){
        String value = cache2.get("local.value");
        assertTrue("Value mismatch", value.equals("42"));
        value = cache2.get("this.also");
        assertNull("Value mismatch", value);
    }

    @Test
    public void testLocalCacheSetNewValue(){
        String value = cache2.get("local.value");
        assertTrue("Value mismatch", value.equals("42"));
        cache2.set("local.value2", "43");
        value = cache2.get("local.value2");
        assertTrue("Value mismatch", value.equals("43"));
    }

    @Test
    public void testCacheEmptyAfterCleared(){
        String value = cache2.get("local.value");
        assertTrue("Value mismatch", value.equals("42"));
        cache2.set("local.value2", "43");
        value = cache2.get("local.value2");
        assertTrue("Value mismatch", value.equals("43"));
        cache2.clear();
        value = cache2.get("local.value");
        assertNull(value);
        value = cache2.get("local.value2");
        assertNull(value);
    }

    @Test
    public void testCacheSize(){
        assertTrue("Size mismatch", cache2.size() == 13);
        cache2.set("local.value2", "43");
        assertTrue("Size mismatch", cache2.size() == 27);
        cache2.set("local.value3", "44");
        assertTrue("Size mismatch", cache2.size() == 41);
        //override value
        cache2.set("local.value", "AAA");
        assertTrue("Size mismatch", cache2.size() == 42);
        cache2.remove("local.value");
        assertTrue("Size mismatch", cache2.size() == 28);
    }

    @Test
    public void testCacheRemoveKey(){
        cache2.set("local.value2", "43");
        String value = cache2.get("local.value");
        assertTrue("Value mismatch", value.equals("42"));
        value = cache2.get("local.value2");
        assertTrue("Value mismatch", value.equals("43"));
        value = cache2.remove("local.value");
        assertTrue("Value mismatch", value.equals("42"));
        value = cache2.remove("local.value2");
        assertTrue("Value mismatch", value.equals("43"));
        value = cache2.get("local.value");
        assertNull(value);
        value = cache2.get("local.value2");
        assertNull(value);
    }

}
