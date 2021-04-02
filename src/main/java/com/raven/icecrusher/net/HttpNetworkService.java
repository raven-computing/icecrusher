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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Concrete <code>NetworkSerice</code> implementation for making HTTP requests to 
 * network endpoints.
 *
 */
public class HttpNetworkService extends Service<NetworkResult> implements NetworkService {

    private ResourceLocator locator;
    private Parcel parcel;
    private DoubleProperty progressProperty;
    private StringProperty relProgressProperty;

    protected HttpNetworkService(final ResourceLocator locator, final Parcel parcel){
        if(locator == null){
            throw new IllegalArgumentException("Locator must not be null");
        }
        this.locator = locator;
        this.parcel = parcel;
    }

    @Override
    public void bindIndicator(final ProgressIndicator indicator){
        this.progressProperty = indicator.progressProperty();
    }

    @Override
    public void bindIndicator(ProgressIndicator indicator, Label label){
        this.progressProperty = indicator.progressProperty();
        this.relProgressProperty = label.textProperty();
    }

    @Override
    public void abort(){
        this.cancel();
    }

    @Override
    public void connect(){
        try{
            this.start();
        }catch(IllegalStateException ex){
            throw new IllegalStateException("NetworkService was already connected"
                    + " or connecting", ex);
        }
    }

    @Override
    public void retry(){
        try{
            this.restart();
        }catch(IllegalStateException ex){
            throw ex;
        }
    }

    @Override
    public void setOnResult(ResultHandler resultHandler){
        this.setOnSucceeded((e) -> {
            if(progressProperty != null){
                progressProperty.unbind();
            }
            if(relProgressProperty != null){
                relProgressProperty.unbind();
            }
            resultHandler.onResult(getValue());
        });
    }

    @Override
    protected Task<NetworkResult> createTask(){
        final NetworkConnector connector= new NetworkConnector(locator, parcel);
        if(progressProperty != null){
            this.progressProperty.unbind();
            this.progressProperty.bind(connector.progressProperty());
        }
        if(relProgressProperty != null){
            this.relProgressProperty.unbind();
            this.relProgressProperty.bind(connector.messageProperty());
        }
        return connector;
    }

}
