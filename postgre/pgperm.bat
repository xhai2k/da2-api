@ECHO OFF

REM Set permissions on a postgresql installation
REM
REM Usage: pgperm.bat serverdir datadir account skipcacls
REM
REM Permissions set for service account
REM  serverdir: DENY WRITE, READ
REM  datadir:   CHANGE
REM  serverdir\tmp: CHANGE

if not exist %1\tmp mkdir %1\tmp

IF "%4"=="1" GOTO DataDir
cacls %1 /E /T /D %3 >> %1\tmp\pgperm.log 2>&1
if errorlevel 1 goto err
cacls %1 /E /T /G %3:R >> %1\tmp\pgperm.log 2>&1
if errorlevel 1 goto err

:DataDir
if not exist %2 mkdir %2

IF "%4"=="1" GOTO ExitNicely
cacls %2 /E /T /P %3:C >> %1\tmp\pgperm.log 2>&1
if errorlevel 1 goto err
cacls %1\tmp /E /T /P %3:C >> %1\tmp\pgperm.log 2>&1
if errorlevel 1 goto err

:ExitNicely
del %1\tmp\pgperm.log
exit 0

:err
exit 1

