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

package com.raven.icecrusher.application;

import java.util.HashMap;
import java.util.Map;

/**
 * A cache storing user related preferences during a session.
 *
 */
public class SessionCache extends Cache {
    
    private Map<String, String> cache;
    private int size;
    private int limit;
    
    /**
     * Constructs a new <code>SessionCache</code> with the specified size restriction
     * 
     * @param limit The size restriction of the SessionCache to be constructed
     */
    public SessionCache(final int limit){
        this.cache = new HashMap<>();
        this.limit = limit;
    }

    @Override
    public String get(final String key){
        return this.cache.get(key);
    }
    
    @Override
    public String get(final String key, final String defaultValue){
        final String val = get(key);
        return (val != null) ? val : defaultValue;
    }

    @Override
    public String set(final String key, final String value){
        final String previous = this.cache.get(key);
        if(previous != null){
            this.size -= previous.length();
        }
        if(value != null){
            this.size += value.length();
            if(size > limit){
                clear();
                this.size += key.length() + value.length();
            }
        }
        this.cache.put(key, value);
        return previous;
    }
    
    @Override
    public String remove(final String key){
        this.size -= key.length();
        final String previous = this.cache.remove(key);
        if(previous != null){
            this.size -= previous.length();
        }
        return previous;
    }

    @Override
    public int size(){
        return this.size;
    }

    @Override
    public void clear(){
        this.cache.clear();
        this.size = 0;
    }
    
    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        for(final Map.Entry<String, String> e : cache.entrySet()){
            sb.append("[");
            sb.append(e.getKey());
            sb.append("] -> ");
            if(e.getValue() != null){
                sb.append("'");
                sb.append(e.getValue());
                sb.append("'");
            }else{
                sb.append("null");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
