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
 * Represents a result of an asynchronous network operation.<br>
 * This type is used for all result types, not only for successful operations.
 * The status of the underlying operation can be queried by calling 
 * <code>getStatus()</code>.
 * 
 * <p>Depending on the expected response type (content-type), the response payload
 * can be accessed by either calling <code>getBytes()</code> for arbitrary binary 
 * data, or <code>getString</code>  which will automatically convert the payload data
 * to a String object.
 *
 */
public class NetworkResult {

    /**
     *Enumerates all defined Status keys.
     */
    public enum Status {

        /**
         * Indicates a successful connection. Please note that this code is different 
         * from the HTTP status code in that a connection is still considered successful 
         * even if the server returned a HTTP failure code like 500
         */
        SUCCESS,

        /**
         * Indicates a cancelled connection
         */
        CANCELLED,

        /**
         * Indicates a failed connection. A connection is considered failed if either the 
         * connection attempt was unsuccessful or the connection failed during the 
         * transmission of data
         */
        FAILURE;
    }

    private Status status;
    private Header responseHeader;
    private int responseCode;
    private byte[] data;

    protected NetworkResult(){
        this.responseCode = -1;
    }

    /**
     * Gets the raw bytes of the response payload
     * 
     * @return The bytes returned by the connection endpoint. May be null
     */
    public byte[] getBytes(){
        return this.data;
    }

    /**
     * Gets the response payload as a String
     * 
     * @return The text data returned by the connection endpoint. May be null
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
     * Gets the HTTP response code of the connection endpoint
     * 
     * @return The response code of the underlying connection
     */
    public int getResponseCode(){
        return this.responseCode;
    }

    /**
     * Gets the header of the connection response
     * 
     * @return A <code>Header</code> holding all header fields of the 
     *         connection response
     */
    public Header getResponseHeader(){
        return this.responseHeader;
    }

    /**
     * Gets the status of the underlying connection. This can be used to find 
     * out about failed connection attempts or cancelled operations
     * 
     * @return The <code>Status</code> of the underlying connection 
     */
    public Status getSatus(){
        return this.status;
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        final String nl = System.lineSeparator();
        if(status != null){
            sb.append("Status: " + this.status);
            sb.append(nl);
        }
        sb.append("Response code: " + this.responseCode);
        sb.append(nl);
        if(responseHeader != null){
            sb.append(responseHeader.toString());
        }
        if(data != null){
            sb.append(nl);
            sb.append("Data:");
            sb.append(nl);
            sb.append(getString());
        }
        return sb.toString();
    }

    protected void setBytes(final byte[] data){
        this.data = data;
    }

    protected void setStatus(final Status status){
        this.status = status;
    }

    protected void setResponseCode(final int responseCode){
        this.responseCode = responseCode;
    }

    protected void setResponseHeader(final Header responseHeader){
        this.responseHeader = responseHeader;
    }

    /**
     * Builder class for constructing <code>NetworkResult</code> objects.
     *
     */
    protected static class Builder {

        private NetworkResult result;

        protected Builder(){
            this.result = new NetworkResult();
        }

        protected Builder bytes(final byte[] data){
            this.result.setBytes(data);
            return this;
        }

        protected Builder responsHeader(final Header responseHeader){
            this.result.setResponseHeader(responseHeader);
            return this;
        }

        protected Builder responseCode(final int responseCode){
            this.result.setResponseCode(responseCode);
            return this;
        }

        protected Builder status(final Status status){
            this.result.setStatus(status);
            return this;
        }

        protected NetworkResult build(){
            return this.result;
        }
    }

}
