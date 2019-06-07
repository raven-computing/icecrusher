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

package com.raven.icecrusher.util;

import com.raven.icecrusher.application.StackedApplication;
import com.raven.icecrusher.ui.dialog.Dialogs;

/**
 * Utility class providing static methods for handling Exceptions in a consistent 
 * and configurable way. If some application code catches an Exception that it cannot
 * further handle in a reasonable way, then this class can be consulted to settle the
 * situation by dispatching the caught Exception to this class instead of silently 
 * ignoring it.
 * 
 * <p>This class does not handle uncaught Exceptions by application code.
 *
 */
public final class ExceptionHandler {

	private ExceptionHandler(){ }
	
	/**
	 * Handles the provided Exception in the default way
	 * 
	 * @param throwable The throwable Exception to handle
	 */
	public static void handle(final Throwable throwable){
		if(Const.DEBUG){
			System.err.println("----- ["+ExceptionHandler.class.getSimpleName()+"] "+"catched Exception: -----");
			throwable.printStackTrace();
			System.err.println("----- ["+ExceptionHandler.class.getSimpleName()+"] "+"end stack trace -----");
		}
	}
	
	/**
	 * Shows an error dialog to the user on the application's main Stage
	 * 
	 * @param throwable The throwable Exception to handle and show
	 */
	public static void showDialog(final Throwable throwable){
		Dialogs.showExceptionDialog(StackedApplication.getMainStage(), throwable);
	}
	
}
