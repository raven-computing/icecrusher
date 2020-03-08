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

package com.raven.icecrusher.net;

import java.io.UnsupportedEncodingException;

/**
 * Represents a package to send over an asynchronous network operation.<br>
 * This is used both to carry additional header fields in the request as well as
 * a payload when using, for example an HTTP POST request.
 *
 */
public class Parcel {

    private byte[] data;
    private Header header;

    /**
     * Constructs a new emtpy <code>Parcel</code>
     */
    public Parcel(){ }

    /**
     * Constructs a new <code>Parcel</code> with the specified payload data
     * 
     * @param data The payload of this parcel
     */
    public Parcel(final byte[] data){
        this.data = data;
    }

    /**
     * Constructs a new <code>Parcel</code> with the specified payload text
     * 
     * @param data The text payload of this parcel
     */
    public Parcel(final String data){
        try{
            this.data = data.getBytes("UTF-8");
        }catch(UnsupportedEncodingException ex){
            throw new IllegalStateException(ex.getCause());
        }
    }

    /**
     * Gets the raw bytes of this parcel
     * 
     * @return The bytes of this parcel. May be null
     */
    public byte[] getBytes(){
        return this.data;
    }

    /**
     * Gets the payload of this parcel converted to a String
     * 
     * @return The String payload of this parcel. May be null
     */
    public String getString(){
        if(data == null){
            return null;
        }
        try{
            return new String(data, "UTF-8");
        }catch(UnsupportedEncodingException ex){
            throw new IllegalStateException(ex.getCause());
        }
    }

    /**
     * Gets the request header of this parcel
     * 
     * @return The header of this parcel. May be null
     */
    public Header getHeader(){
        return this.header;
    }

    /**
     * Adds a header field to this parcel. Subsequent calls will override 
     * the header field with the specified key
     * 
     * @param key The key of the header field to add
     * @param value The value of the header field to add
     */
    public void addHeader(final String key, final String value){
        if(header == null){
            this.header = initializeHeader();
        }
        this.header.add(key, value);
    }

    /**
     * Sets the header of this parcel
     * 
     * @param header The <code>Header</code> to be used by this parcel
     */
    public void setHeader(final Header header){
        this.header = header;
    }

    /**
     * Indicates whether this parcel has a header set
     * 
     * @return True if this parcel has any header fields set, false if no header 
     *         fields have been set for this parcel
     */
    public boolean hasHeader(){
        return (header != null);
    }

    /**
     * Indicates whether this parcel has payload data
     * 
     * @return True if some payload has been set for this parcel, false otherwise
     */
    public boolean payloadIsEmpty(){
        return ((data == null) || (data.length == 0));
    }

    private Header initializeHeader(){
        return new Header();
    }

}
