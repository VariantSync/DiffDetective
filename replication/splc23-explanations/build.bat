@echo off
setlocal

set "targetSubPath=splc23-explanations"

rem Get the current directory
for %%A in ("%CD%") do set "currentDir=%%~nxA"

rem Check if the current directory ends with the target sub-path
if "%currentDir:~-19%"=="%targetSubPath%" (
    cd ..\..
    docker build -t diff-detective -f replication\splc23-explanations\Dockerfile .
    @pause
) else (
    echo error: the script must be run from inside the splc23-explanations directory, i.e., DiffDetective\replication\%targetSubPath%
)
endlocal
