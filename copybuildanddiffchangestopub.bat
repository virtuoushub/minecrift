SET MINECRIFT_SRCROOT=C:\minecrift-src
SET MINECRIFTPUB_SRCROOT=C:\minecrift-public

rmdir /S /Q %MINECRIFTPUB_SRCROOT%\mcp908\src\minecraft
xcopy /E %MINECRIFT_SRCROOT% %MINECRIFTPUB_SRCROOT%\mcp908\src\minecraft\
cd %MINECRIFTPUB_SRCROOT%
call build.bat
call getchanges.bat
