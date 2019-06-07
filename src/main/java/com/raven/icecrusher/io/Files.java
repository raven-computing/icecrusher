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

package com.raven.icecrusher.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.raven.common.io.CSVFileReader;
import com.raven.common.io.CSVFileWriter;
import com.raven.common.io.ConcurrentReader;
import com.raven.common.io.ConcurrentWriter;
import com.raven.common.io.DataFrameSerializer;
import com.raven.common.struct.DataFrame;
import com.raven.icecrusher.application.StackedApplication;
import com.raven.icecrusher.ui.OneShotSnackbar;
import com.raven.icecrusher.util.EditorFile;
import com.raven.icecrusher.util.ExceptionHandler;

import javafx.application.Platform;
import javafx.concurrent.Task;

import static com.raven.common.io.DataFrameSerializer.DF_FILE_EXTENSION;

/**
 * Utility class for handling reading and writing of files and other file related tasks.<br>
 * Heavy operations will be performed on a background thread.
 *
 */
public class Files {
	
	/** The default name for a newly created file which is not persisted yet **/
	public static final String DEFAULT_NEW_FILENAME = "untitled";

	private Files(){ }
	
	/**
	 * Persists the specified <code>DataFrame</code> as the specified file. All information on how exactly
	 * the DataFrame will be persisted, for example as a .df or CSV file, is being retrieved from the provided 
	 * <code>EditorFile</code> object.<br>
	 * This operation will be performed on a background thread
	 * 
	 * @param file The EditorFile object representing the file to persist. Must not be null
	 * @param df The DataFrame to persist as the content of the above file. Must not be null
	 * @param delegate The ConcurrentWriter callback, called when this operation has finished. Must not be null
	 */
	public static void persistFile(final EditorFile file, final DataFrame df, final ConcurrentWriter delegate){
		try{
			if(file.isImported()){//Imported CSV file
				if(!file.hasCSVHeader()){
					final String[] names = df.getColumnNames();
					df.removeColumnNames();
					new CSVFileWriter(file).useSeparator(file.getCSVSeparator()).parallelWrite(df, (f) -> {
						Platform.runLater(() -> delegate.onWritten(f));
					});
					df.setColumnNames(names);
				}else{
					new CSVFileWriter(file).useSeparator(file.getCSVSeparator()).parallelWrite(df, (f) -> {
						Platform.runLater(() -> delegate.onWritten(f));
					});
				}
			}else{//Normal DataFrame file
				DataFrameSerializer.parallelWriteFile(file, df, (f) -> {
					Platform.runLater(() -> delegate.onWritten(f));
				});
			}
		}catch(Exception ex){
			Platform.runLater(() -> {
				delegate.onWritten(null);
				ExceptionHandler.showDialog(ex);
			});
		}
	}
	
	/**
	 * Reads the specified file from the filesystem. All information on how exactly the file will be read,
	 * for example as a .df or CSV file, is being retrieved from the provided <code>EditorFile</code> object.<br>
	 * This operation will be performed on a background thread
	 * 
	 * @param file The EditorFile object representing the file to read from the filesystem. Must not be null
	 * @param delegate The ConcurrentReader callback, called when this operation has finished. Must not be null
	 */
	public static void readFile(final EditorFile file, final ConcurrentReader delegate){
		try{
			if(file.isImported()){
				readImported(file, delegate);
			}else{
				DataFrameSerializer.parallelReadFile(file, (df) -> {
					Platform.runLater(() -> delegate.onRead(sanitize(df)));
				});
			}
		}catch(Exception ex){
			Platform.runLater(() -> {
				delegate.onRead(null);
				ExceptionHandler.showDialog(ex);
			});
		}
	}
	
	/**
	 * Reads all files in the provided list from the filesystem. All information on how exactly the files will be read,
	 * for example as a .df or CSV file, is being retrieved from the provided <code>EditorFile</code> object within 
	 * the list.<br>
	 * The entire operation will be performed on a background thread
	 * 
	 * @param files The List of EditorFile objects to read from the filesystem. If this list is empty, an empty 
	 *        list is passed to the callback as well
	 * @param delegate The ConcurrentBulkReader callback, called when this operation has finished. Must not be null
	 */
	public static void readAllFiles(final List<EditorFile> files, final ConcurrentBulkReader delegate){
		final List<DataFrame> list = new ArrayList<>();
		final Task<Void> task = new Task<Void>(){
			@Override
			protected Void call() throws Exception{
				for(final EditorFile file : files){
					if(isCancelled()){ break; }
					if(file.exists()){
						try{
							if(file.isImported()){
								list.add(sanitize(new CSVFileReader(file, file.hasCSVHeader())
										.useSeparator(file.getCSVSeparator()).read()));
								
							}else{
								list.add(sanitize(DataFrameSerializer.readFile(file)));
							}
						}catch(IOException ex){
							//Error while reading specific file
							ExceptionHandler.handle(ex);
						}
					}
				}
				Platform.runLater(() -> delegate.onRead(list));
				return null;
			}
		};
		new Thread(task).start();
	}
	
	/**
	 * Adds a <i>.df</i> file extension to the filename of the specified <code>EditorFile</code> object.<br>
	 * All other attributes are not altered
	 * 
	 * @param file The EditorFile to modify
	 * @return An EditorFile object which has the same attributes as the EditorFile of the argument, but with an added
	 *         .df file extension
	 */
	public static EditorFile addExtensionToFile(final EditorFile file){
		final EditorFile tmp = new EditorFile(file.getAbsolutePath()+DF_FILE_EXTENSION);
		tmp.setImported(file.isImported());
		tmp.setCSVSeparator(file.getCSVSeparator());
		return tmp;
	}
	
	private static void readImported(final EditorFile file, final ConcurrentReader delegate){
		final Task<Void> task = new Task<Void>(){
			@Override
			protected Void call() throws Exception{
				if(file.exists()){
					try{
						DataFrame df = new CSVFileReader(file, file.hasCSVHeader())
                                                 .useSeparator(file.getCSVSeparator()).read();

						Platform.runLater(() -> delegate.onRead(sanitize(df)));
					}catch(IOException ex){
						Platform.runLater(() -> {
							final String msg = ex.getMessage();
							if(msg.startsWith("Improperly")){
								OneShotSnackbar.showFor(StackedApplication.getRootPane(), ex.getMessage());
							}
							delegate.onRead(null);
						});
					}
				}
				return null;
			}
		};
		new Thread(task).start();
	}
	
	private static DataFrame sanitize(final DataFrame df){
		return ((df != null) ? DataFrames.sanitize(df) : null);
	}

}
