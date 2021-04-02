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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;

/**
 * Represents a header for a network connection. This class is used both for 
 * request headers as well as response headers.
 *
 */
public class Header implements Iterable<HeaderField> {

    private Map<String, String> fields;

    /**
     * Constructs a new <code>Header</code> with no fields set
     */
    public Header(){
        this.fields = new HashMap<>();
    }

    /**
     * Adds and possibly replaces a header field with the specified key and value
     * to this header
     * 
     * @param key The key of the header field to add
     * @param value The value of the header field to add
     */
    public void add(final String key, final String value){
        this.fields.put(key, value);
    }

    /**
     * Returns the value of the header field with the specified key
     * 
     * @param key The key of the header field to get
     * @return The value of the header field with the specified key. May be null
     */
    public String valueOf(final String key){
        return this.fields.get(key);
    }

    /**
     * Gets the header field with the specified key
     * 
     * @param key The key of the header field to get
     * @return The header field with the specified key. May be null
     */
    public HeaderField getField(final String key){
        final String value = this.fields.get(key);
        return ((value != null) ? HeaderField.of(key, value) : null);
    }

    @Override
    public Iterator<HeaderField> iterator(){
        return new HeaderIterator(fields.entrySet());
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        final String nl = System.lineSeparator();
        for(final HeaderField field : this){
            sb.append("[");
            sb.append(field.getKey());
            sb.append(": ");
            sb.append(field.getValue());
            sb.append("]");
            sb.append(nl);
        }
        return sb.toString();
    }

    /**
     * Iterator for <code>Header</code>.
     *
     */
    public static class HeaderIterator implements Iterator<HeaderField> {

        private Iterator<Map.Entry<String, String>> iter;

        protected HeaderIterator(final Set<Map.Entry<String, String>> set){
            this.iter = set.iterator();
        }

        @Override
        public boolean hasNext(){
            return this.iter.hasNext();
        }

        @Override
        public HeaderField next(){
            final Map.Entry<String, String> e = this.iter.next();
            return HeaderField.of(e.getKey(), e.getValue());
        }

    }

}

/**
 * Represents a header field (key-value pair) which is used inside a 
 * connection <code>Header</code>.
 *
 */
class HeaderField extends Pair<String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <code>HeaderField</code> with the specified key and value
     * 
     * @param key The key of the header field
     * @param value The value of the header field
     */
    public HeaderField(String key, String value){
        super(key, value);
    }

    /**
     * Tests this <code>HeaderField</code> for equality with another
     * <code>Object</code>.
     *
     * <p>If the <code>Object</code> to be tested is not a
     * <code>HeaderField</code> or is null, then this method
     * returns <code>false</code>.
     *
     * <p>Two <code>HeaderField</code>s are considered equal if and only if
     * both keys are equal
     *
     * @param o The <code>Object</code> to test for equality
     * @return True if the given <code>Object</code> is equal to this 
     *         <code>HeaderField</code>, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }
        if(this == o){
            return true;
        }
        if(o instanceof Pair){
            final HeaderField pair = (HeaderField) o;
            if(((getKey() != null) && getKey().equals(pair.getKey()))){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return getKey() + ": " + getValue();
    }

    /**
     * Constructs a new <code>HeaderField</code> with the specified key and value
     * 
     * @param key The key of the header field
     * @param value The value of the header field
     * @return A <code>HeaderField</code> with the specified key and value set
     */
    public static HeaderField of(final String key, final String value){
        return new HeaderField(key, value);
    }
}
