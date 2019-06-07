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

import java.io.IOException;

import com.raven.common.io.DataFrameSerializer;
import com.raven.common.struct.DataFrame;
import com.raven.common.struct.DefaultDataFrame;
import com.raven.common.struct.Row;
import com.raven.common.struct.RowItem;

/**
 * Models a feedback object to be sent over the network.
 *
 */
public class Feedback implements Row {
	
    @RowItem("Content")
	private String content;
    
    @RowItem("E-Mail")
	private String email;
    
    @RowItem("App-Version")
	private String appVersion;
	
	public Feedback(){ }

	public Feedback(final String content, final String email){
		this.content = content;
		this.email = email;
	}

	public String getContent(){
		return this.content;
	}

	public void setContent(final String content){
		this.content = content;
	}

	public String getEmail(){
		return this.email;
	}

	public void setEmail(final String email){
		this.email = email;
	}
	
	public String getAppVersion(){
		return this.appVersion;
	}

	public void setAppVersion(final String appVersion){
		this.appVersion = appVersion;
	}
	
	public static String serialize(final Feedback feedback){
		final DataFrame df = new DefaultDataFrame(Feedback.class);
		df.addRow(feedback);
		try{
			return DataFrameSerializer.toBase64(df);
		}catch(IOException ex){
			ExceptionHandler.handle(ex);
			return "n/a";
		}
	}
	
	public static String contentType(){
		return "application/df; charset=utf-8";
	}
	
}
