Name ImageRail

SetCompressor /SOLID lzma
#SetCompress off   # uncomment this and comment above line for quick test builds

# Defines
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION 1.2
!define PATCH_LEVEL 1
!define FULL_VERSION "${VERSION}.${PATCH_LEVEL}"
!define COMPANY ""
!define URL ""
!define PLATFORM "windows-x86"

!define BUILD_DIR "..\..\..\build"  # path args to File etc. are relative to .nsi file
!define STAGE_DIR "${BUILD_DIR}\${PLATFORM}"

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
OutFile ${BUILD_DIR}\ImageRail-${FULL_VERSION}-${PLATFORM}.exe
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
    File /r ${STAGE_DIR}\*.*

    CreateDirectory "$SMPROGRAMS\ImageRail"
    CreateShortCut "$SMPROGRAMS\ImageRail\ImageRail.lnk" "$INSTDIR\ImageRail_Windows.bat" "" "$INSTDIR\ImageRail.ico" 0
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
    Delete $INSTDIR\ImageRail_Windows.bat
    Delete $INSTDIR\ImageRail.ico
    RmDir /r /REBOOTOK $INSTDIR\doc
    RmDir /r /REBOOTOK $INSTDIR\features
    RmDir /r /REBOOTOK $INSTDIR\icons 
    RmDir /r /REBOOTOK $INSTDIR\jre
    RmDir /r /REBOOTOK $INSTDIR\lib

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

    ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString
    ReadRegStr $R1 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion
    StrCmp $R0 "" done

    MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
    '$(^Name) version $R1 is already installed.  We recommend removing $\n \
    this older version before continuing to upgrade to version ${FULL_VERSION}. $\n$\n \
    Click "OK" to automatically remove the previous version and continue, $\n \
    or "Cancel" to cancel this upgrade.' \
    IDOK uninst
    Abort
 
    # Run the uninstaller
uninst:
    ClearErrors
    # Silent, and do not copy the uninstaller to a temp file
    ExecWait '"$R0" /S _?=$INSTDIR'

done:
FunctionEnd


# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd


