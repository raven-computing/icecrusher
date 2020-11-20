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

package com.raven.icecrusher.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.raven.common.io.CSVReader;
import com.raven.common.io.CSVWriter;
import com.raven.common.io.DataFrameSerializer;
import com.raven.common.struct.DataFrame;
import com.raven.icecrusher.application.StackedApplication;
import com.raven.icecrusher.ui.FileTab;
import com.raven.icecrusher.ui.OneShotSnackbar;
import com.raven.icecrusher.util.EditorFile;
import com.raven.icecrusher.util.ExceptionHandler;

import javafx.application.Platform;
import javafx.concurrent.Task;

import static com.raven.common.io.DataFrameSerializer.DF_FILE_EXTENSION;

/**
 * Utility class for handling reading and writing of files
 * and other file related tasks.<br>
 * Heavy operations will be performed on a background thread.
 *
 */
public class Files {

    /** The default name for a newly created file which is not persisted yet **/
    public static final String DEFAULT_NEW_FILENAME = "untitled";

    private Files(){ }

    /**
     * Persists the specified <code>DataFrame</code> as the specified file.
     * All information on how exactly the DataFrame will be persisted, for example
     * as a .df or CSV file, will be retrieved from the provided
     * <code>EditorFile</code> object.<br>
     * This operation will be performed on a background thread
     * 
     * @param file The EditorFile object representing the file to persist.
     *             Must not be null
     * @param df The DataFrame to persist as the content of the above file.
     *           Must not be null
     * @return A <code>CompletableFuture</code> that completes when the
     *         operation has finished
     */
    public static CompletableFuture<Void> persistFile(final EditorFile file,
            final DataFrame df){

        try{
            if(file.isImported()){//Imported CSV file
                return new CSVWriter(file)
                        .useSeparator(file.getCSVSeparator())
                        .withHeader(file.hasCSVHeader())
                        .writeAsync(df);

            }else{//Normal DataFrame file
                return DataFrameSerializer.writeFileAsync(file, df);
            }
        }catch(Exception ex){
            Platform.runLater(() -> {
                ExceptionHandler.showDialog(ex);
            });
            final CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    /**
     * Reads the specified file from the filesystem. All information on how exactly
     * the file will be read, for example as a .df or CSV file, is being retrieved from
     * the provided <code>EditorFile</code> object.<br>
     * This operation will be performed on a background thread
     * 
     * @param file The EditorFile object representing the file to read from
     *             the filesystem. Must not be null
     * @param delegate The ConcurrentSinlgeReader callback, called when this
     *                 operation has finished. Must not be null
     */
    public static void readFile(final EditorFile file,
            final ConcurrentSingleReader delegate){

        try{
            if(file.isImported()){
                readImported(file, delegate);
            }else{
                DataFrameSerializer.readFileAsync(file).handleAsync((df, ex) -> {
                    if(df != null){
                        Platform.runLater(() -> delegate.onRead(
                                new FileTab(file, sanitize(df))));

                    }else{
                        Platform.runLater(() -> {
                            delegate.onRead(null);
                            if(ex != null){
                                ExceptionHandler.showDialog(ex);
                            }
                        });
                    }
                    return null;
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
     * Reads all files in the provided list from the filesystem. All information
     * on how exactly the files will be read, for example as a .df or CSV file, is
     * being retrieved from the provided <code>EditorFile</code> object within
     * the list.<br>
     * The entire operation will be performed on a background thread
     * 
     * @param files The List of EditorFile objects to read from the filesystem.
     *              If this list is empty, an empty list is passed
     *              to the callback as well
     * @param delegate The ConcurrentBulkReader callback, called when
     *                 this operation has finished. Must not be null
     */
    public static void readAllFiles(final List<EditorFile> files,
            final ConcurrentBulkReader delegate){

        final List<FileTab> list = new ArrayList<>();
        final Task<Void> task = new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                for(final EditorFile file : files){
                    if(isCancelled()){ break; }
                    if(file.exists()){
                        try{
                            if(file.isImported()){
                                final DataFrame df = sanitize(new CSVReader(file)
                                        .withHeader(file.hasCSVHeader())
                                        .useSeparator(file.getCSVSeparator())
                                        .read());
                                
                                list.add(new FileTab(file, df));
                            }else{
                                final DataFrame df = sanitize(
                                        DataFrameSerializer.readFile(file));

                                list.add(new FileTab(file, df));
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
     * Adds a <i>.df</i> file extension to the filename of the
     * specified <code>EditorFile</code> object.<br>
     * All other attributes are not altered
     * 
     * @param file The EditorFile to modify
     * @return An EditorFile object which has the same attributes as
     *         the EditorFile of the argument, but with an added .df file extension
     */
    public static EditorFile addExtensionToFile(final EditorFile file){
        final EditorFile tmp = new EditorFile(
                file.getAbsolutePath() + DF_FILE_EXTENSION);

        tmp.setImported(file.isImported());
        tmp.setCSVSeparator(file.getCSVSeparator());
        return tmp;
    }

    private static void readImported(final EditorFile file,
            final ConcurrentSingleReader delegate){
        
        final Task<Void> task = new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                if(file.exists()){
                    try{
                        DataFrame df = new CSVReader(file)
                                .withHeader(file.hasCSVHeader())
                                .useSeparator(file.getCSVSeparator())
                                .read();

                        if(df != null){
                            Platform.runLater(() -> delegate.onRead(
                                    new FileTab(file, sanitize(df))));

                        }else{
                            Platform.runLater(() -> delegate.onRead(null));
                        }
                    }catch(IOException ex){
                        Platform.runLater(() -> {
                            final String msg = ex.getMessage();
                            if(msg.startsWith("Improperly")){
                                OneShotSnackbar.showFor(
                                        StackedApplication.getRootPane(),
                                        ex.getMessage());

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
