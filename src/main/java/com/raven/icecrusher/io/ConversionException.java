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

package com.raven.icecrusher.io;

/**
 * An Exception to indicate a failed column conversion from a specific type 
 * to another type.
 *
 */
public class ConversionException extends Exception {

    private static final long serialVersionUID = 1L;

    private String offendingValue;
    private int rowIndex;

    /**
     * Constructs a new <code>ConversionException</code> with null as the detail message.
     * The cause is not initialized
     */
    public ConversionException(){
        super();
    }

    /**
     * Constructs a new <code>ConversionException</code> with the specified detail message.
     * The cause is not initialized
     *
     * @param message The detail message of the new exception
     */
    public ConversionException(String message){
        super(message);
    }

    /**
     * Constructs a new <code>ConversionException</code> with the specified cause and a 
     * detail message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     *
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A null value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown)
     */
    public ConversionException(Throwable cause){
        super(cause);
    }

    /**
     * Constructs a new <code>ConversionException</code> with the specified detail
     * message and cause. <p>Note that the detail message associated with
     * the cause is <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message The detail message to use
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link #getCause()} method). (A null value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown)
     */
    public ConversionException(String message, Throwable cause){
        super(message, cause);
    }

    /**
     * Constructs a new <code>ConversionException</code> with the specified detail 
     * message, cause, suppression enabled or disabled, and writable stack trace 
     * enabled or disabled.
     *
     * @param message The detail message
     * @param cause The cause. (A null value is permitted,
     *              and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression Whether or not suppression is enabled
     *                          or disabled
     * @param writableStackTrace Whether or not the stack trace should
     *                           be writable
     */
    public ConversionException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace){

        super(message, cause, enableSuppression, writableStackTrace);
    }

    private ConversionException(String message, String offendingValue, int rowIndex){
        super(message);
        this.offendingValue = offendingValue;
        this.rowIndex = rowIndex;
    }

    public String getOffendingValue(){
        return this.offendingValue;
    }

    public String getFormattedOffendingValue(){
        if((offendingValue != null) && (offendingValue.length() > 10)){
            return this.offendingValue.substring(0, 7) + "...";
        }
        return this.offendingValue;
    }

    public int getRowIndex(){
        return this.rowIndex;
    }

    public static ConversionException with(String message,
            String offendingValue, int rowIndex){
        
        return new ConversionException(message, offendingValue, rowIndex);
    }

    public static ConversionException with(String message,
            Object offendingValue, int rowIndex){
        
        return new ConversionException(message, 
                ((offendingValue != null) 
                        ? offendingValue.toString() 
                                : null), rowIndex);
    }

}
