;This file will be executed next to the application bundle image
;I.e. current directory will contain folder icecrusher with application files
[Setup]
AppId={{com.raven.icecrusher}}
AppName=Icecrusher
AppVersion=2.3.1
AppVerName=Icecrusher 2.3.1
AppPublisher=Raven Computing
AppComments=An editor and analysis tool for DataFrame files
AppCopyright=Copyright (C) 2020
DefaultDirName={localappdata}\Icecrusher
DisableStartupPrompt=No
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=No
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=Raven Computing
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=icecrusher-2.3.1
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=icecrusher\icecrusher.ico
UninstallDisplayIcon={app}\icecrusher.ico
UninstallDisplayName=Icecrusher
WizardImageStretch=No
WizardSmallImageFile=icecrusher-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "icecrusher\icecrusher.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "icecrusher\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Icecrusher"; Filename: "{app}\icecrusher.exe"; IconFilename: "{app}\icecrusher.ico"; Check: returnTrue()
Name: "{commondesktop}\Icecrusher"; Filename: "{app}\icecrusher.exe";  IconFilename: "{app}\icecrusher.ico"; Check: returnFalse()


[Run]
Filename: "{app}\icecrusher.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\icecrusher.exe"; Description: "{cm:LaunchProgram,Icecrusher}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\icecrusher.exe"; Parameters: "-install -svcName ""Icecrusher"" -svcDesc ""Icecrusher"" -mainExe ""icecrusher.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\icecrusher.exe "; Parameters: "-uninstall -svcName Icecrusher -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
