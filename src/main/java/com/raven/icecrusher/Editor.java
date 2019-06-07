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

package com.raven.icecrusher;

import com.raven.common.util.ArgumentParseException;
import com.raven.common.util.ArgumentParser;
import com.raven.icecrusher.application.Resources;
import com.raven.icecrusher.application.StackedApplication;
import com.raven.icecrusher.application.Controller.ArgumentBundle;
import com.raven.icecrusher.base.Activity;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.EditorConfiguration;

import javafx.stage.Stage;

import static com.raven.icecrusher.application.Resources.IC_ICECRUSHER;
import static com.raven.icecrusher.util.EditorConfiguration.*;
import static com.raven.icecrusher.util.EditorConfiguration.Section.*;

/**
 * Main class of the Icecrusher application.
 *
 */
public class Editor extends StackedApplication {
	
	private static EditorConfiguration config;
	private static boolean isNative;
	private static boolean wasUpdated;
	
	@Override
	public void onStart(Stage stage) throws Exception{
		stage.setTitle(Const.APPLICATION_NAME);
		stage.getIcons().add(Resources.image(IC_ICECRUSHER));
		if(wasUpdated){
			config.set(UPDATER, CONFIG_UPDATE_AVAILABLE, false);
			config.transferAll();
		}
		final ArgumentBundle bundle = new ArgumentBundle();
		bundle.addArgument(BUNDLE_KEY_SCENE_WIDTH, config.doubleOf(WINDOW, CONFIG_WINDOW_WIDTH));
		bundle.addArgument(BUNDLE_KEY_SCENE_HEIGHT, config.doubleOf(WINDOW, CONFIG_WINDOW_HEIGHT));
		startActivity(Activity.MAIN, bundle);
	}
	
	@Override
	public void onStop(){
		config.persistConfiguration();
		if(config.booleanOf(GLOBAL, CONFIG_RECALL_TABS)){
			config.persistHistory();
		}
	}
	
	@Override
	public void onWindowResized(final double width, final double height){
		if(config != null){
			config.set(WINDOW, CONFIG_WINDOW_WIDTH, getMainStage().getWidth());
			config.set(WINDOW, CONFIG_WINDOW_HEIGHT, getMainStage().getHeight());
		}
	}
	
	/**
	 * Gets the boolean variable set at startup to indicate whether this application 
	 * is running as a self contained native application bundle
	 * 
	 * @return True if this application instance was launched through the 
	 *         native executable, false otherwise
	 */
	public static boolean isNative(){
		return isNative;
	}
	
	/**
	 * Gets the boolean variable set at startup to indicate whether this application 
	 * was recently updated
	 * 
	 * @return True if this application was launched through a native update 
	 *         script, indicating that this application was updated to a 
	 *         newer version. False if it is a regular launch
	 */
	public static boolean wasUpdated(){
		return wasUpdated;
	}

	public static void main(String[] args){
		config = getConfiguration();
		
		final ArgumentParser ap = new ArgumentParser.Builder()
				.booleanArg("-isNative")
				.booleanArg("-wasUpdated")
				.build();
				
		try{
			ap.parse(args);
		}catch(ArgumentParseException ex){
			//all args are optional. Ignore
		}
		isNative = ap.getBooleanArg("-isNative", false);
		wasUpdated = ap.getBooleanArg("-wasUpdated", false);
		
		launch(args);
	}

}