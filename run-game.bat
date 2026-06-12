@echo off
cd /d "%~dp0"
"C:\Users\borte\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\maven\latest\bin\mvn.cmd" -f R/pom.xml exec:java -Dexec.mainClass=com.swinggame.SwingMain
pause
