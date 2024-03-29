@echo off
setlocal

set "targetSubPath=splc23-views"

rem Get the current directory
for %%A in ("%CD%") do set "currentDir=%%~nxA"

rem Check if the current directory ends with the target sub-path
if "%currentDir:~-12%"=="%targetSubPath%" (
    cd ..\..
    docker build -t diff-detective-views -f replication\splc23-views\Dockerfile .
    @pause
) else (
    echo error: the script must be run from inside the splc23-views directory, i.e., DiffDetective\replication\%targetSubPath%
)
endlocal

