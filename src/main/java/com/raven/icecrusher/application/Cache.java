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

import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

/**
 * Abstract class defining methods application caches must implement.
 * A Cache maps plain string keys to plain string values and may be size restricted.<br>
 * This class also provides access to application caches via static methods.
 *
 */
public abstract class Cache {
    
    private static SessionCache sessionCache;
    
    /**
     * Gets the cache entry with the specified key
     * 
     * @param key The key of the cache entry to get. Must not be null
     * @return The cache entry with the specified key, or null if the cache
     *         does not have an entry with the specified key
     */
    public abstract String get(final String key);
    
    /**
     * Gets the cache entry with the specified key or returns the specified default value
     * 
     * @param key The key of the cache entry to get. Must not be null
     * @param defaultValue The default value to return if the cache does not
     *                     have an entry with the specified key
     * @return The cache entry with the specified key, or the default value if the cache
     *         does not have an entry with the specified key
     */
    public abstract String get(final String key, final String defaultValue);
    
    /**
     * Sets the cache entry with the specified key to the specified value. This method
     * might overwrite a preexisting value
     * 
     * @param key The key of the cache entry to set. Must not be null
     * @param value The value of the cache entry to set
     * @return The previous cache value with the specified key. May be null
     */
    public abstract String set(final String key, final String value);
    
    /**
     * Removes the cache entry with the specified key
     * 
     * @param key The key of the cache entry to remove. Must not be null
     * @return The value of the cache entry removed, May be null
     */
    public abstract String remove(final String key);
    
    /**
     * Indicates the approximate size of the cache. Please note that this does not
     * necessarily indicate the size of the cache as laid out in memory
     * 
     * @return The size of the cache
     */
    public abstract int size();
    
    /**
     * Clears the cache and discards all entries
     */
    public abstract void clear();
    
    /**
     * Gets a reference to the application session cache
     * 
     * @return The <code>SessionCache</code> of the application
     */
    public static Cache session(){
        if(sessionCache == null){
            final int size = getConfiguration().memoryOf(GLOBAL, CONFIG_CACHE_SESSION_SIZE);
            sessionCache = new SessionCache((size > 500) ? size : 2000000);
        }
        return sessionCache;
    }
}
