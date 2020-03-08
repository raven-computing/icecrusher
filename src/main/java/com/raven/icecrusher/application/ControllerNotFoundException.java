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

/**
 * Runtime exception which occurs when an object of type <code>Controller</code>
 * is being searched for but could not be found. Please note that this exception 
 * does not necessarily mean that the concrete Controller class does generally not
 * exist, but may be caused by the circumstance that no instance of the Controller 
 * in question is residing inside the internal activity stack of an application.
 *
 */
public class ControllerNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ControllerNotFoundException(){ 
        super();
    }

    public ControllerNotFoundException(String message){
        super(message);
    }

    public ControllerNotFoundException(Throwable cause){
        super(cause);
    }

    public ControllerNotFoundException(String message, Throwable cause){
        super(message, cause);
    }

    public ControllerNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace){

        super(message, cause, enableSuppression, writableStackTrace);
    }

}
