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

import java.lang.reflect.Method;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Default implementation of a <code>ProxyController</code>.<br>
 * In order to avoid memory leaks, this concrete implementation does not hold 
 * an enduring internal reference of the Controller the proxy represents. After 
 * each method invocation the internal reference is discarded, which means that
 * any subsequent calls to the same or any other method will require the proxy
 * to obtain an internal reference of the underlying Controller again.
 *
 */
public class DefaultProxyController implements ProxyController {

    private Class<? extends Controller> type;

    protected DefaultProxyController(final Class<? extends Controller> type){
        this.type = type;
    }

    @Override
    public Object call(String key) throws Exception{
        return callController(key, new Object[]{});
    }

    @Override
    public Object call(String key, Object... args) throws Exception{
        return callController(key, args);
    }

    private Object callController(String key, Object... args) throws Exception{
        final Method method = findExposedMethodFor(key);
        if(method == null){
            throw new IllegalArgumentException("No exposed method found in " 
                    + type.getName() + " for key: " + key);

        }
        return method.invoke(StackedApplication.getControllerByClass(type), args);
    }

    @Override
    public Stage getStage() throws ControllerNotFoundException{
        return StackedApplication.getControllerByClass(type).getStage();
    }

    @Override
    public Pane getRootNode() throws ControllerNotFoundException{
        return StackedApplication.getControllerByClass(type).getRootNode();
    }

    @Override
    public Parent getLocalRootNode() throws ControllerNotFoundException{
        return StackedApplication.getControllerByClass(type).getLocalRootNode();
    }

    private Method findExposedMethodFor(final String key){
        Method method = null;
        for(final Method m : type.getMethods()){
            final Exposed annotation = m.getAnnotation(Exposed.class);
            if(annotation == null){
                continue;
            }
            final String value = annotation.value();
            if((value == null) || (value.isEmpty())){
                if(m.getName().equals(key)){
                    method = m;
                    break;
                }
            }else{
                if(value.equals(key)){
                    method = m;
                    break;
                }
            }
        }
        return method;
    }

}
