@echo off
setlocal

set "targetSubPath=thesis-es"

rem Get the current directory
for %%A in ("%CD%") do set "currentDir=%%~nxA"

rem Check if the current directory ends with the target sub-path

if "%currentDir:~-9%"=="%targetSubPath%" (
    cd ..\..
    docker build -t diff-detective-unparse -f replication\thesis-es\Dockerfile .
    @pause
) else (
    echo error: the script must be run from inside the thesis-es directory, i.e., DiffDetective\replication\%targetSubPath%
)
endlocal

