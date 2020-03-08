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

package com.raven.icecrusher.io.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.raven.icecrusher.application.Resources;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.ExceptionHandler;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Abstract helper class for all concrete <code>UpdateExecutor</code> implementations.<br>
 * Provides methods for common tasks during the update process. 
 *
 */
public abstract class AbstractUpdateExecutor {

    public interface AsyncOperation {

        public void onResult(boolean success);
    }

    private UpdateInfo updateInfo;
    private UpdateRoutine updateRoutine;

    private File tmpUpdateRoot;
    private String tmpUpdatePackageRoot;
    private byte[] downloadedPackage;

    protected AbstractUpdateExecutor(final UpdateInfo updateInfo, 
            final UpdateRoutine updateRoutine){

        this.updateInfo = updateInfo;
        this.updateRoutine = updateRoutine;
    }

    protected boolean checkRuntimeChanged(){
        return (this.updateInfo.getRuntimeId() != Const.APPLICATION_RUNTIME_ID);
    }

    protected String getDownloadVerification(){
        return hashOfBytes(downloadedPackage);
    }

    protected File getTmpUpdateRootDir(){
        return this.tmpUpdateRoot;
    }

    protected String getTmpUpdatePackageRootDir(){
        if(this.tmpUpdatePackageRoot.endsWith(File.separator)){
            return this.tmpUpdatePackageRoot
                    .substring(0, tmpUpdatePackageRoot.length()-1);

        }
        return this.tmpUpdatePackageRoot;
    }

    protected String remoteInstructionVerificationOf(final byte[] bytes){
        return hashOfBytes(bytes);
    }

    private File getTempUpdateDirectory() throws IOException{
        String path = System.getProperty("java.io.tmpdir");
        if((path == null) || (path.isEmpty())){
            throw new IOException("Unable to find system temporary directory");
        }
        if(!path.endsWith(File.separator)){
            path = path + File.separator;
        }
        return new File(path + createTempUpdateDirectory());
    }

    protected UpdateInfo getUpdateInfo(){
        return this.updateInfo;
    }

    protected UpdateRoutine getUpdateRoutine(){
        return this.updateRoutine;
    }

    protected byte[] getDownloadedPackage(){
        return this.downloadedPackage;
    }

    protected void setDownloadedPackage(final byte[] data){
        this.downloadedPackage = data;
    }

    protected void doFinalize(){
        System.exit(0);
    }

    protected void extract(final AsyncOperation handler){
        new Thread(new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                boolean success;
                try{
                    extract();
                    success = true;
                }catch(IOException ex){
                    ExceptionHandler.handle(ex);
                    success = false;
                }
                if(handler != null){
                    final boolean b = success;
                    Platform.runLater(() -> {
                        handler.onResult(b);
                    });
                }
                return null;
            }
        }).start();
    }

    protected void copyLocalInstructions(final String filename, 
            final AsyncOperation handler){

        new Thread(new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                boolean success;
                try{
                    copyLocal(filename);
                    success = true;
                }catch(IOException ex){
                    ExceptionHandler.handle(ex);
                    success = false;
                }
                if(handler != null){
                    final boolean b = success;
                    Platform.runLater(() -> {
                        handler.onResult(b);
                    });
                }
                return null;
            }
        }).start();
    }

    protected void copyRemoteInstructions(final byte[] bytes, final String filename, 
            final AsyncOperation handler){

        new Thread(new Task<Void>(){
            @Override
            protected Void call() throws Exception{
                boolean success;
                try{
                    copyRemote(bytes, filename);
                    success = true;
                }catch(IOException ex){
                    ExceptionHandler.handle(ex);
                    success = false;
                }
                if(handler != null){
                    final boolean b = success;
                    Platform.runLater(() -> {
                        handler.onResult(b);
                    });
                }
                return null;
            }
        }).start();
    }

    private void extract() throws IOException{
        tmpUpdateRoot = getTempUpdateDirectory();
        ensureExists(tmpUpdateRoot);
        final byte[] buffer = new byte[32768];
        final ByteArrayInputStream bais = new ByteArrayInputStream(getDownloadedPackage());
        try(final ZipInputStream zip = new ZipInputStream(bais)){
            ZipEntry entry = zip.getNextEntry();
            tmpUpdatePackageRoot = entry.getName();
            while(entry != null){
                final File fileEntry = fileByEntry(tmpUpdateRoot, entry);
                if(entry.isDirectory()){
                    fileEntry.mkdir();
                }else{
                    try(final FileOutputStream fos = new FileOutputStream(fileEntry)){
                        int i = -1;
                        while((i = zip.read(buffer, 0, buffer.length)) != -1){
                            fos.write(buffer, 0, i);
                        }
                    }
                }
                zip.closeEntry();
                entry = zip.getNextEntry();
            }
        }
    }

    private void copyLocal(final String filename) throws IOException{
        final byte[] bytes = Resources.bytes(Const.DIR_UPDATE, filename);
        if(bytes == null){
            throw new IOException("No update resource was found with name: " + filename);
        }
        String path = this.tmpUpdateRoot.getAbsolutePath();
        if(!path.endsWith(File.separator)){
            path = path + File.separator;
        }
        writeInstructionFile(bytes, new File(path + filename));
    }

    private void copyRemote(final byte[] bytes, final String filename) throws IOException{
        String path = this.tmpUpdateRoot.getAbsolutePath();
        if(!path.endsWith(File.separator)){
            path = path + File.separator;
        }
        writeInstructionFile(bytes, new File(path + filename));
    }

    private void writeInstructionFile(final byte[] bytes, final File file) throws IOException{
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final byte[] buffer = new byte[4096];
        try(final FileOutputStream fos = new FileOutputStream(file)){
            int i = -1;
            while((i = bais.read(buffer, 0, buffer.length)) != -1){
                fos.write(buffer, 0, i);
            }
        }
        if(!file.setExecutable(true, false)){
            throw new IOException("Unable to set instruction file to executable: " + file);
        }
    }

    private void ensureExists(final File directory) throws IOException{
        if(!directory.exists()){
            if(!directory.mkdir()){
                throw new IOException("Unable to create temporary update root directory: " 
                        + directory);

            }
        }
    }

    private File fileByEntry(final File parentDir, final ZipEntry entry) throws IOException{
        final File file = new File(parentDir, entry.getName());
        if(!file.getCanonicalPath().startsWith(parentDir.getCanonicalPath() + File.separator)){
            throw new IOException("ZipEntry " + entry.getName() 
            + "is not part of target root directory " 
            + parentDir.getAbsolutePath());

        }
        return file;
    }

    private String hashOfBytes(final byte[] bytes){
        try{
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            return bytesToHex(md.digest());
        }catch(NoSuchAlgorithmException ex){
            ExceptionHandler.handle(ex);
        }
        return null;
    }

    private String createTempUpdateDirectory(){
        final byte[] suffix = new byte[6];
        new Random().nextBytes(suffix);
        return Const.APPLICATION_NAME + "-" + bytesToHex(suffix);
    }

    private String bytesToHex(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length*2);
        for(final byte b: bytes){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
