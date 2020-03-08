@echo off
 
REM Copyright (C) 2020 Raven Computing
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM 
REM http://www.apache.org/licenses/LICENSE-2.0
REM 
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.


REM This script implements the update instructions for the Icecrusher
REM desktop application for Windows.

REM When executed, this script will copy application files from the directory
REM specified by the first argument passed to it (Source) to the directory
REM defined by the second argument (Target). If the application target
REM directory and its children are write-protected by a user with admin
REM privileges, then this script will write a VB script to its own working
REM directory with instructions for invoking the operating system UAC and
REM requesting required privileges from the user. If the user approves of
REM the operation, then this script will be called again with admin privileges
REM and execute the copy operations.
REM A cleanup of the source directory is performed afterwards in order to free
REM up space. This script finishes its operation by calling the native
REM executable in the target directory causing the updated application
REM to launch.

:init
 setlocal DisableDelayedExpansion
 set winSysDir=System32
 set "batchPath=%~dp0"
 for %%k in (%0) do set batchName=%%~nk
 if '%1'=='PRIVILEGED' (shift /1  &  goto readArgs)
 set "vbsGetPrivileges=%~1\getPriv_%batchName%.vbs"
 setlocal EnableDelayedExpansion

 set "argSource=%~1"
 set "argTarget=%~2"

 setlocal enableextensions
 REM we wait for 5 seconds to give the JVM time to shut itself down
 ping.exe -n 6 localhost >NUL 2>&1
 goto checkPrivileges

:checkPrivileges
  cmd.exe /C "echo "ACC" > "%argTarget%\acc.txt" || REM"
  if exist "%argTarget%\acc.txt" ( goto doUpdate ) else ( goto getPrivileges )

:getPrivileges
 echo scriptdir = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName) > "%vbsGetPrivileges%"
 echo For Each strArg in WScript.Arguments >> "%vbsGetPrivileges%"
 echo args = args ^& strArg >> "%vbsGetPrivileges%"
 echo args = args ^& "?" >> "%vbsGetPrivileges%"
 echo Next >> "%vbsGetPrivileges%"
 echo args = RemoveTrailingSep(args) >> "%vbsGetPrivileges%"
 echo Set objFSO=CreateObject("Scripting.FileSystemObject") >> "%vbsGetPrivileges%"
 echo outFile= scriptdir ^& "\" ^& "cargs.txt" >> "%vbsGetPrivileges%"
 echo Set objFile = objFSO.CreateTextFile(outFile,True) >> "%vbsGetPrivileges%"
 echo objFile.Write args >> "%vbsGetPrivileges%"
 echo objFile.Close >> "%vbsGetPrivileges%"
 echo Set UAC = CreateObject("Shell.Application") >> "%vbsGetPrivileges%"
 echo args = "PRIVILEGED" >> "%vbsGetPrivileges%"
 echo execPath = scriptdir ^& "\" ^& "update.bat" >> "%vbsGetPrivileges%"
 echo UAC.ShellExecute execPath, args, "", "runas", 1 >> "%vbsGetPrivileges%"

 echo Function RemoveTrailingSep(s) >> "%vbsGetPrivileges%"
 echo 	RemoveTrailingSep = Left(s, Len(s) - 1) ^& Replace(s, "?", "", Len(s))  >> "%vbsGetPrivileges%"
 echo End Function >> "%vbsGetPrivileges%"

 goto execElevation

:execElevation
 "%SystemRoot%\%winSysDir%\WScript.exe" "%vbsGetPrivileges%" %*
 exit /B

:readArgs
 set execUpdateElev=true
 setlocal enableextensions enabledelayedexpansion
 cd /d %~dp0
 set argSource=src
 set argTarget=trgt

 for /f "tokens=*" %%a in (cargs.txt) do (
   set "x=%%a"
 )

 set i=1
 set "x!i!=%x:?=" & set /A i+=1 & set "x!i!=%"
 set "argSource=!x1!"
 set "argTarget=!x2!"

:doUpdate
 setlocal & cd /d %~dp0

 del "%argTarget%\acc.txt" > NUL 2>&1

 if "%execUpdateElev%"=="true" ( robocopy "%argSource%\icecrusher" "%argTarget%" /R:0 ) else ( robocopy "%argSource%\icecrusher" "%argTarget%" /R:0 >Nul 2>&1 )

 if "%execUpdateElev%"=="true" ( robocopy "%argSource%\icecrusher\app" "%argTarget%\app" /MIR /R:0 ) else ( robocopy "%argSource%\icecrusher\app" "%argTarget%\app" /MIR /R:0 >Nul 2>&1 )

 if "%execUpdateElev%"=="true" ( robocopy "%argSource%\icecrusher\runtime" "%argTarget%\runtime" /MIR /R:0 ) else ( robocopy "%argSource%\icecrusher\runtime" "%argTarget%\runtime" /MIR /R:0 >Nul 2>&1 )

 start /D "%argTarget%" icecrusher.exe -wasUpdated=true

 rmdir "%argSource%" /S /Q

