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

package com.raven.icecrusher.ui.plot;

/**
 * Runtime exception thrown to indicate that an input cannot be 
 * converted or represented as a valid date.
 *
 */
public class DateFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int index = -1;

    public DateFormatException(){
        super();
    }

    public DateFormatException(final int index){
        super();
        this.index = index;
    }

    public DateFormatException(final String message){
        super(message);
    }

    public DateFormatException(final Throwable cause){
        super(cause);
    }

    public DateFormatException(final String message, final Throwable cause){
        super(message, cause);
    }

    public DateFormatException(final String message, final Throwable cause, 
            final boolean enableSuppression, final boolean writableStackTrace){

        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getCauseIndex(){
        return this.index;
    }

}
