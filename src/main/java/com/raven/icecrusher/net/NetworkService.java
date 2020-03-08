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

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * A network service is responsible for performing network IO related tasks in the
 * background and to anynchronously notify the API user of the result. 
 * 
 * <p>This interface provides static utility methods returning a <code>NetworkService</code>
 * object capable of connecting to the network resource specified by a 
 * <code>ResourceLocator</code>
 * 
 * <p>Optionally, a <code>Parcel</code> object can be given to the above mentioned methods 
 * which will then be passed to the concrete network service implementation. That parcel
 * will then be sent to the connection endpoint. 
 *
 */
public interface NetworkService {

    /**
     * Performs an asynchronous connection attempt to the network endpoint of this 
     * network service. In order to get notified of the result, you must set a 
     * <code>ResultHandler</code> by calling 
     * {@link NetworkService#setOnResult(ResultHandler)}.<br>
     * It is recommended to specify a handler before calling <code>connect()</code>.<br>
     * This method should only be called on the FX application thread
     */
    public void connect();

    /**
     * Cancels a currently running connection, if applicable, and restarts this Service.<br>
     * This method should only be called on the FX application thread
     */
    public void retry();

    /**
     * Cancels this connection if it is currently connected or trying to connect to the 
     * network endpoint of this service
     */
    public void abort();

    /**
     * Specifies a {@link ResultHandler} which gets called when the background connection 
     * finishes passing the result to the handler's <code>onResult()</code> method
     * 
     * @param resultHandler The <code>ResultHandler</code> for handling the 
     *                      asynchronous response
     */
    public void setOnResult(ResultHandler resultHandler);

    /**
     * Binds the progress property of the specified <code>ProgressIndicator</code> to the 
     * progress property of the underlying network task
     * 
     * @param indicator The <code>ProgressIndicator</code> to bind
     */
    public void bindIndicator(ProgressIndicator indicator);

    /**
     * Binds the progress property of the specified <code>ProgressIndicator</code> to the 
     * progress property of the underlying network task and the relatve progress value 
     * message to the String property of the specified <code>Label</code>
     * 
     * @param indicator The <code>ProgressIndicator</code> to bind
     * @param label The <code>Label</code> to bind
     */
    public void bindIndicator(ProgressIndicator indicator, Label label);

    /**
     * Returns a network service for connecting to the network resource specified by the URL 
     * of the <code>ResourceLocator</code> passed to this method
     * 
     * @param locator The <code>ResourceLocator</code> of the resource to connect to
     * @return A <code>NetworkService</code> for the specified resource. When calling 
     *         <code>connect()</code> on the returned network service, it will make a 
     *         connection attempt to that resource
     */
    public static NetworkService getService(ResourceLocator locator){
        return getService(locator, null);
    }

    /**
     * Returns a network service for connecting to the network resource specified by the URL 
     * of the <code>ResourceLocator</code> passed to this method. The provided 
     * <code>Parcel</code> will be sent to the network endpoint
     * 
     * @param locator The <code>ResourceLocator</code> of the resource to connect to
     * @param parcel The <code>Parcel</code> data to send to the endpoint
     * @return A <code>NetworkService</code> for the specified resource. When calling 
     *         <code>connect()</code> on the returned network service, it will make a 
     *         connection attempt to that resource
     */
    public static NetworkService getService(ResourceLocator locator, Parcel parcel){
        return new HttpNetworkService(locator, parcel);
    }
}
