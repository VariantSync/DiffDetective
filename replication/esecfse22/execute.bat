@echo off
setlocal

set "targetSubPath=esecfse22"

rem Get the current directory
for %%A in ("%CD%") do set "currentDir=%%~nxA"

rem Check if the current directory ends with the target sub-path
if "%currentDir:~-9%"=="%targetSubPath%" (
docker run --rm -v "%cd%\results":"/home/sherlock/results" diff-detective %*
) else (
    echo error: the script must be run from inside the esecfse22 directory, i.e., DiffDetective\replication\%targetSubPath%
)
endlocal
