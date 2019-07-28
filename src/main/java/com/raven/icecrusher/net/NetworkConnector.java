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

package com.raven.icecrusher.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

import com.raven.icecrusher.net.NetworkResult.Status;
import com.raven.icecrusher.util.ExceptionHandler;

import javafx.concurrent.Task;

/**
 * Connector class for performing HTTP connections to web endpoints.<br>
 * Connection attempts and reading/writing of streams is performed on a 
 * background thread.
 * 
 */
public class NetworkConnector extends Task<NetworkResult> {

    private HttpURLConnection connection;
    private ResourceLocator locator;
    private Parcel parcel;

    protected NetworkConnector(final ResourceLocator locator){
        this(locator, null);
    }

    protected NetworkConnector(final ResourceLocator locator, final Parcel parcel){
        if(locator == null){
            throw new IllegalArgumentException("Locator must not be null");
        }
        this.locator = locator;
        this.parcel = parcel;

    }

    @Override
    protected NetworkResult call() throws Exception{
        NetworkResult result = new NetworkResult();
        result.setStatus(Status.SUCCESS);//set as default. May get overridden later
        try{
            openConnection();
            this.connection.setRequestMethod(locator.getMethod());
            this.connection.setRequestProperty("Accept-Language", "en-US,en,q=0.5");
            this.connection.setRequestProperty("Accept-Encoding", "UTF-8");
            this.connection.setConnectTimeout(10000);
            if(parcel != null){
                if(parcel.hasHeader()){
                    addHeaderFieldsToRequest(parcel.getHeader());
                }
            }
            if(isOutputMethod() && (parcel != null) && !parcel.payloadIsEmpty()){
                this.connection.setDoOutput(true);
                sendPayload();
            }

            final int responseCode = connection.getResponseCode();
            if((responseCode >= 200) && (responseCode < 300)){//HTTP 2xx Status codes
                result = receivePayload();
            }
            result.setResponseCode(responseCode);
            result.setResponseHeader(receiveResponseHeader());
        }catch(Exception ex){
            ExceptionHandler.handle(ex);
            result = new NetworkResult.Builder().status(Status.FAILURE).build();
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        return result;
    }

    /**
     * Opens a HttpURLConnection/HttpsURLConnection on the URL object specified
     * by the internal resource locator
     * 
     * @throws MalformedURLException If no protocol is specified,
     *                               or an unknown protocol is found
     * @throws IOException If a connection cannot be opened or if an I/O exception occurs
     */
    private void openConnection() throws MalformedURLException, IOException{
        final String url = this.locator.getUrl();
        if(url.startsWith("https")){
            this.connection = (HttpsURLConnection) new URL(url).openConnection();
        }else{
            this.connection = (HttpURLConnection) new URL(url).openConnection();
        }
    }

    /**
     * Writes parcel payload data on the buffered output stream of the underlying connection
     * 
     * @throws IOException If an I/O exception occurs during writing
     */
    private void sendPayload() throws IOException{
        try(final BufferedOutputStream os = new BufferedOutputStream(
                connection.getOutputStream())){
            
            os.write(parcel.getBytes());
            os.flush();
        }
    }

    /**
     * Constructs and returns the header of the connection response
     * 
     * @return The HTTP header of the endpoint response
     */
    private Header receiveResponseHeader(){
        final Header header = new Header();
        for(final Map.Entry<String, List<String>> field : connection.getHeaderFields()
                .entrySet()){
            
            final StringBuilder sb = new StringBuilder();
            final List<String> values = field.getValue();
            for(final String s : values){
                sb.append(s);
            }
            if((field.getKey() != null) && (sb.length() > 0)){
                header.add(field.getKey(), sb.toString());
            }
        }
        return header;
    }

    /**
     * Constructs and returns a NetworkResult object for the response
     * of the underlying connection
     * 
     * @return A NetworkResult object for the underlying connection
     * @throws IOException If an I/O exception occurs
     */
    private NetworkResult receivePayload() throws IOException{
        NetworkResult result = null;
        final int responseCode = connection.getResponseCode();
        final long contentLength = connection.getContentLengthLong();
        byte[] buffer = new byte[32768];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
        final BufferedInputStream is = new BufferedInputStream(
                connection.getInputStream());
        
        try{
            int i = -1;
            while((i = is.read(buffer, 0, buffer.length)) != -1){
                if(isCancelled()){
                    result = createCancelledResult(responseCode);
                    break;
                }
                baos.write(buffer, 0, i);
                if(contentLength >= 0){
                    final int size = baos.size();
                    updateProgress(size, contentLength);
                    updateMessage(String.valueOf(
                            (int)(((float)size/(float)contentLength)*100)) + "%");
                    
                }
            }
        }finally{
            is.close();
        }
        if(!isCancelled()){
            result = new NetworkResult.Builder()
                    .bytes(baos.toByteArray())
                    .responseCode(responseCode)
                    .status(Status.SUCCESS)
                    .build();
        }
        return result;
    }

    /**
     * Adds all fields of the provided Header object to the underlying connection request
     * 
     * @param header The header to add
     */
    private void addHeaderFieldsToRequest(final Header header){
        for(final HeaderField field : header){
            this.connection.setRequestProperty(field.getKey(), field.getValue());
        }
    }

    /**
     * Indicates whether the HTTP method of the ResourceLocator is either POST or PUT
     * 
     * @return True if the HTTP method is POST or PUT. False for any other HTTP method
     */
    private boolean isOutputMethod(){
        final String httpMethod = this.locator.getMethod();
        return ((httpMethod.equals("POST") || (httpMethod.equals("PUT"))));
    }

    /**
     * Creates a NetworkResult object for cancelled connections
     * 
     * @param responseCode The response code of the connection
     * @return A NetworkResult representing a cancelled connection
     */
    private NetworkResult createCancelledResult(final int responseCode){
        return new NetworkResult.Builder()
                .responseCode(responseCode)
                .status(Status.CANCELLED)
                .build();
    }

}
