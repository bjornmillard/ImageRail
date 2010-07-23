Name ImageRail

SetCompressor /SOLID lzma
#SetCompress off   # uncomment this and comment above line for quick test builds

# Defines
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION 1.051
#!define PATCH_LEVEL 01
!define COMPANY ""
!define URL ""

!ifndef PATCH_LEVEL
  !define FULL_VERSION "${VERSION}"
  !define PATCH_LEVEL 0
!else
  !define FULL_VERSION "${VERSION}.${PATCH_LEVEL}"
!endif

# MUI defines
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-uninstall.bmp"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-uninstall.bmp"

# Included files
!include Sections.nsh
!include MUI.nsh

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE English

# Installer attributes
OutFile ImageRail-${FULL_VERSION}-win32.exe
InstallDir $PROGRAMFILES\ImageRail
CRCCheck on
XPStyle on
ShowInstDetails hide
VIProductVersion "${VERSION}.${PATCH_LEVEL}.0"
VIAddVersionKey ProductName ImageRail
VIAddVersionKey ProductVersion "${FULL_VERSION}"
VIAddVersionKey FileVersion ""
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# Installer sections
Section -Main SEC0000
    SetOutPath $INSTDIR
    SetOverwrite on

    File ImageRail.jar
    File HDF5XML.jar
    File ImageRail_Windows.bat
    File /r /x .svn doc
    File /r /x .svn icons
    File /r /x .svn jars
    File /r /x .svn jre6

    CreateDirectory $INSTDIR\features
    SetOutPath $INSTDIR\features
    File src\features\*.java
    File bin\features\*.class   

    CreateDirectory $INSTDIR\segmentors
    SetOutPath $INSTDIR\segmentors
    File src\segmentors\*.java
    File bin\segmentors\*.class   

    CreateDirectory "$SMPROGRAMS\ImageRail"
    CreateShortCut "$SMPROGRAMS\ImageRail\ImageRail.lnk" "$INSTDIR\ImageRail_Windows.bat" "" "$INSTDIR\icons\ImageRail.ico" 0
    CreateShortCut "$SMPROGRAMS\ImageRail\Uninstall ImageRail.lnk" "$INSTDIR\uninstall.exe"
    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

Section -post SEC0002
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${FULL_VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o un.Main UNSEC0000
    Delete $INSTDIR\ImageRail.jar
    Delete $INSTDIR\HDF5XML.jar
    Delete $INSTDIR\ImageRail_Windows.bat
    RmDir /r /REBOOTOK $INSTDIR\doc
    RmDir /r /REBOOTOK $INSTDIR\icons 
    RmDir /r /REBOOTOK $INSTDIR\jars
    RmDir /r /REBOOTOK $INSTDIR\jre6

    RmDir $INSTDIR\features
    RmDir $INSTDIR\segmentors

    RmDir /r "$SMPROGRAMS\ImageRail"
    DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section un.post UNSEC0001
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function .onInit
    InitPluginsDir
FunctionEnd


# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd


