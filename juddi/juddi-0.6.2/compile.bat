@echo off
set CATALINE_HOME=%COUGAAR_INSTALL_PATH%\sys
ant -Dbasedir=%JUDDI_HOME% compile
