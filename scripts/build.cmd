@echo off
setlocal
rem 13RP-Demo build helper: set local JDK/Maven env then run command
rem Use system env vars if already set, otherwise fallback to local defaults
if not defined JAVA_HOME set "JAVA_HOME=C:\Users\Administrator\.jdks\ms-21.0.12"
if not defined MAVEN_HOME set "MAVEN_HOME=D:\IDEA\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

if "%1"=="backend" goto backend
if "%1"=="backend-run" goto backend-run
echo Usage: build.cmd [backend^|backend-run]
exit /b 1

:backend
cd /d "%~dp0..\backend"
call mvn.cmd package -DskipTests
exit /b %errorlevel%

:backend-run
cd /d "%~dp0..\backend"
start "13RP backend" cmd /k "java -jar target\rd13-demo-0.3.0.jar"
exit /b 0
