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

package com.raven.icecrusher.io.update;

import java.io.File;
import java.net.HttpURLConnection;

import com.raven.icecrusher.application.StackedApplication;
import com.raven.icecrusher.io.update.UpdateInfo.Instruction;
import com.raven.icecrusher.io.update.UpdateInfo.InstructionType;
import com.raven.icecrusher.io.update.UpdateInfo.OperatingSystem;
import com.raven.icecrusher.io.update.UpdateInfo.PackageType;
import com.raven.icecrusher.net.NetworkService;
import com.raven.icecrusher.net.ResourceLocator;
import com.raven.icecrusher.net.NetworkResult.Status;
import com.raven.icecrusher.util.Const;
import com.raven.icecrusher.util.ExceptionHandler;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

/**
 * Concrete implementation of an <code>UpdateExecutor</code> for Linux/Debian.<br>
 * The activation cycle of the UpdateExecutor interface and UpdateRoutine applies.
 *
 */
public class LinuxUpdateExecutor extends AbstractUpdateExecutor implements UpdateExecutor {

    private static final String LOCAL_INSTRUCTION_FILE = "update.sh";

    //keep the reference actively in memory to prevent the GC from
    //collecting it while downloading large update packages
    private NetworkService service;

    protected LinuxUpdateExecutor(final UpdateInfo updateInfo, final UpdateRoutine updateRoutine){
        super(updateInfo, updateRoutine);
    }

    @Override
    public void downloadPackage(ProgressIndicator indicator, Label label){
        final boolean runtimeChanged = super.checkRuntimeChanged();
        this.service = NetworkService.getService(runtimeChanged 
                ? ResourceLocator.UPDATE_PACKAGE_LINUX_FULL 
                : ResourceLocator.UPDATE_PACKAGE_LINUX_APP);

        this.service.bindIndicator(indicator, label);
        this.service.setOnResult((result) -> {
            final boolean success = ((result.getSatus() == Status.SUCCESS) 
                    && (result.getResponseCode() == HttpURLConnection.HTTP_OK)
                    && (result.getBytes() != null)
                    && (result.getBytes().length > 0));

            if(result.getSatus() == Status.CANCELLED){
                //evict from memory
                service = null;
                return;
            }
            super.setDownloadedPackage(result.getBytes());
            super.getUpdateRoutine().onPackageDownloaded(success);
        });
        this.service.connect();
    }

    @Override
    public void verifyDownload(){
        boolean isValid = false;
        final String hash = super.getDownloadVerification();
        if((hash != null) && (!hash.isEmpty())){
            final String checksum = getUpdateInfo().getPackageChecksum(OperatingSystem.LINUX, 
                    (checkRuntimeChanged() ? PackageType.FULL : PackageType.APP));

            if(checksum != null){
                isValid = (hash.equals(checksum));
            }
        }
        super.getUpdateRoutine().onDownloadVerified(isValid);
    }

    @Override
    public void extractPackage(){
        super.extract((success) -> {
            getUpdateRoutine().onExtracted(success);
        });
    }

    @Override
    public void setupInstructions(){
        final Instruction instruct = super.getUpdateInfo().getInstruction(OperatingSystem.LINUX);
        if(instruct != null){
            if(instruct.getType() == InstructionType.COPY_LOCAL){
                this.copyLocalInstructions();
            }else if(instruct.getType() == InstructionType.COPY_REMOTE){
                if(instruct.getID() != Const.UPDATE_LINUX_INSTRUCTION_ID){
                    this.copyRemoteInstructions(instruct);
                }else{
                    this.copyLocalInstructions();
                }
            }else{
                super.getUpdateRoutine().onInstructionsProvided(false);
            }
        }else{
            super.getUpdateRoutine().onInstructionsProvided(false);
        }
    }

    @Override
    public void doUpdate(){
        final File rootDir = super.getTmpUpdateRootDir();
        String path = rootDir.getAbsolutePath();
        if(!path.endsWith(File.separator)){
            path = path + File.separator;
        }
        File targetDir = new File(StackedApplication.getApplicationDirectory());
        final String targetDirPath = targetDir.getAbsolutePath();
        if((targetDirPath.endsWith("app") || (targetDirPath.endsWith("app/")))){
            targetDir = targetDir.getParentFile();
        }
        final String execution = (path + LOCAL_INSTRUCTION_FILE);
        final String argTarget = targetDir.getAbsolutePath();
        final String argSource = path + super.getTmpUpdatePackageRootDir();
        try{
            final ProcessBuilder proc = new ProcessBuilder(execution, argSource, argTarget);
            proc.directory(rootDir);
            proc.start();
            super.doFinalize();
        }catch(Exception ex){
            ExceptionHandler.showDialog(ex);
        }
    }

    @Override
    public void cancel(){
        this.service.abort();
    }	

    private void copyLocalInstructions(){
        super.copyLocalInstructions(LOCAL_INSTRUCTION_FILE, (success) -> {
            super.getUpdateRoutine().onInstructionsProvided(success);
        });
    }

    private void copyRemoteInstructions(final Instruction instruct){
        this.service = NetworkService.getService(ResourceLocator.UPDATE_INSTRUCT_LINUX);
        this.service.setOnResult((result) -> {
            final boolean isOK = ((result.getSatus() == Status.SUCCESS)
                    && (result.getResponseCode() == HttpURLConnection.HTTP_OK)
                    && (result.getBytes() != null)
                    && (result.getBytes().length > 0));

            if(isOK){
                final String hash1 = instruct.getFileChecksum();
                final String hash2 = super.remoteInstructionVerificationOf(result.getBytes());
                boolean isValid = false;
                if((hash1 != null) && (!hash1.isEmpty() && (hash2 != null) && (!hash2.isEmpty()))){
                    isValid = (hash1.equals(hash2));
                }
                if(!isValid){
                    super.getUpdateRoutine().onInstructionsProvided(false);
                    return;
                }

                super.copyRemoteInstructions(result.getBytes(), LOCAL_INSTRUCTION_FILE, (success) -> {
                    super.getUpdateRoutine().onInstructionsProvided(success);
                });
            }else{
                super.getUpdateRoutine().onInstructionsProvided(false);
            }
        });
        this.service.connect();
    }

}
