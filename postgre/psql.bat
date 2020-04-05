@echo off

REM Set up for win1252 encoding. Should work on most systems?
REM chcp 1252 
REM  -h localhost -p [LISTENPORT] postgres [SUPERUSER] [CLENCODE]
IF NOT "%7"=="DEFAULT" set PGCLIENTENCODING=%7
IF "%7"=="DEFAULT" set PGCLIENTENCODING=SQL_ASCII

REM Launch psql with all existing parameters
psql.exe %1 %2 %3 %4 %5 %6

REM If psql exits with errorlevel 2, we have a connection error, so show it instead of just closing.
REM For other errors or normal shutdown, just exit.
if errorlevel 2 if not errorlevel 3 pause
