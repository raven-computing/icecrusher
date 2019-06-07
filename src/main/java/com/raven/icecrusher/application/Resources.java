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

package com.raven.icecrusher.application;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.raven.icecrusher.util.Const;

import javafx.scene.image.Image;

/**
 * Resources of this project.
 *
 */
public class Resources {
	
	public static final String IC_ICECRUSHER = "icecrusher.png";
	public static final String IC_FOLDER_BLACK = "ic_folder_black_48dp.png";
	public static final String IC_FOLDER_WHITE = "ic_folder_white_48dp.png";
	
	private static Properties res;
	
	static {
		res = new Properties();
		try{
			res.load(new BufferedInputStream(Resources.class.getClass().getResourceAsStream(Const.DIR_CONFIGS+"project.properties")));
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Loads and returns a resource image
	 * 
	 * @param image The image to load. Should be one of the Resources.IC_*-constants
	 * @return The image loaded
	 * @throws Exception If the image could not be found or loaded
	 */
	public static Image image(final String image) throws Exception{
		return new Image(Resources.class.getClass().getResourceAsStream(Const.DIR_ICON+image));
	}
	
	/**
	 * Loads and returns bytes of a resource 
	 * 
	 * @param IMAGE The image to load. Should be one of the Resources.IC_*-constants
	 * @return The image loaded
	 * @throws Exception If the image could not be found or loaded
	 */
	/**
	 * Loads and returns bytes of a resource
	 * 
	 * @param directory The directory the resource is located at
	 * @param resource The name of the resource
	 * @return The requested resource as an array of bytes or null if no resource could be found 
	 *         in the specified directory with the specified name
	 * @throws IOException If an I/O error occurs during reading of the resource
	 */
	public static byte[] bytes(final String directory, final String resource) throws IOException{
		final InputStream is = Resources.class.getClass().getResourceAsStream(directory + resource);
		if(is != null){
			final BufferedInputStream bis = new BufferedInputStream(is);
			final byte[] buffer = new byte[4096];
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
			try{
				int i = -1;
				while((i = bis.read(buffer, 0, buffer.length)) != -1){
					baos.write(buffer, 0, i);
				}
			}finally{
				bis.close();
			}
			return baos.toByteArray();
		}
		return null;
	}
	
	/**
	 * Returns a project property value
	 * 
	 * @param key The key of the property to get
	 * @return The property value associated with the specified key, or null 
	 *         if the specified property is not set
	 */
	public static String property(final String key){
		return res.getProperty(key);
	}
	
	/**
	 * Returns a project property value converted to an integer
	 * 
	 * @param key The key of the property to get
	 * @return The integer property value associated with the specified key, or 0 (zero)
	 *         if the specified property is not set or cannot be converted to an integer
	 */
	public static int integerProperty(final String key){
		final String value = res.getProperty(key);
		if(value != null){
			try{
				return Integer.valueOf(value);
			}catch(NumberFormatException ex){
				return 0;
			}
		}
		return 0;
	}

}
