@ 2>/dev/null # 2>nul & echo off & goto BOF

:BOF
... setup java home here ...
@echo off
%JAVA_HOME%\bin\java %JAVA_OPTS% -jar "%~dpnx0" %*
exit /B %errorlevel%
