# name the installer
OutFile "Server_Install.exe"

Name "Home Server Application"

# define the directory to install to, the desktop in this case as specified  
# by the predefined $DESKTOP variable
InstallDir $DESKTOP

# default section start; every NSIS script has at least one section.
Section
    SetOutPath $INSTDIR
    File target\server-1.0-jar-with-dependencies.jar
    Rename $INSTDIR\server-1.0-jar-with-dependencies.jar $INSTDIR\server-1.0.jar
    File start-server.bat
    File .env
    MessageBox MB_OK "This application requires java 19 to be installed, and to manually run it from a command line. Services hasn't been figured out yet."
# default section end
SectionEnd