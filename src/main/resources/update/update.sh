#!/bin/bash

# Copyright (C) 2021 Raven Computing
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
#
# This script implements the update instructions for the Icecrusher 
# desktop application for Linux/Debian.
#
# When executed, this script will copy application files from the directory
# specified by the first argument passed to it (Source) to the directory 
# defined by the second argument (Target). Since application files 
# under /opt are owned and write protected by root, the script will look up
# the effective user ID of the underlying user and call itself again 
# with pkexec. Therefore, the system PolicyKit will prompt the user to enter 
# his password in order to authorise the action. If the user chooses to deny 
# authorisation, then the update process cannot finish. 
#
# If authorised, the script then starts to copy application specific files to
# the app target directory. After having copied all files from its source, the
# script then continues to recursively traverse the target app directory and 
# check if each file does also exist in the corresponding source directory. If
# the file does not exist, it will be deleted from the app target causing the
# app target directory to become an exact copy of the provided source.
# The script then continues to do the same for the runtime directory of the 
# application installation directory.
#
# Finally, all files present in the source root directory are copied into the
# target root directory without deleting any files from the target directory.
# A cleanup of the source directory is performed afterwards in order to free
# up space. After that last step, this script finishes its operation by calling 
# the native executable in the target directory causing the updated application
# to launch.
#


#Updatable directories
APP_DIR="app";
RUNTIME_DIR="runtime";

#Startup arguments
ARG_SOURCE=$1;
ARG_TARGET=$2;

#Current working directory
CWD=$(pwd);

#Wait for 5 seconds to give the JVM time to shut itself down.
#Overwriting application files while the JVM is still running
#may cause errors because of file locks
sleep 5;

if [ -z "$ARG_SOURCE" ]; then
  echo "ARG_SOURCE is not specified";
  exit 1;
fi
if [ -z "$ARG_TARGET" ]; then
  echo "ARG_TARGET is not specified";
  exit 1;
fi

function remove_unused () {
  local varDir="$1";
  for file in "$ARG_TARGET/$varDir/"*; do
  if [[ -d "$file" ]]; then
    remove_unused "$varDir/$(basename $file)";
  else
    if ! [[ -f "$ARG_SOURCE/$varDir/$(basename $file)" ]]; then
      rm -df "$ARG_TARGET/$varDir/$(basename $file)";
    fi
  fi
  done
}


#Make sure this script is called with effective user ID of 0 (zero)
if ! [ $(id -u) = 0 ]; then
  #Requesting root privileges (PolKit)
  pkexec "$CWD/update.sh" "$ARG_SOURCE" "$ARG_TARGET"
  #Launch Icecrusher again
  "$ARG_TARGET/icecrusher" -wasUpdated=true &
  #Cleanup source
  rm -rdf "$ARG_SOURCE";
  exit 0;
fi

#Replace files in app directory
if [ -d "$ARG_SOURCE/$APP_DIR" ]; then
  cp -R "$ARG_SOURCE/$APP_DIR" "$ARG_TARGET";
  remove_unused "$APP_DIR";
fi

#Replace files in runtime directory
if [ -d "$ARG_SOURCE/$RUNTIME_DIR" ]; then
  cp -R "$ARG_SOURCE/$RUNTIME_DIR" "$ARG_TARGET";
  remove_unused "$RUNTIME_DIR";
fi

#Copy all files from application root directory
for file in "$ARG_SOURCE/"*; do
  if ! [[ -d "$file" ]]; then
    cp "$file" "$ARG_TARGET/$(basename $file)";
  fi
done
exit 0;
