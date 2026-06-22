@echo off
REM ============================================================
REM  Construye renombrador-excel.jar y renombrador-excel.exe
REM  Requisitos en Windows:
REM    - JDK 17 o superior (incluye jpackage)
REM    - Maven (mvn en el PATH)
REM    - WiX Toolset 3.x si quieres .msi (opcional; para .exe simple no hace falta)
REM ============================================================

setlocal
cd /d "%~dp0"

echo.
echo == 1) Verificando herramientas ==
where javac >nul 2>nul || (echo ERROR: No se encontro javac. Instala un JDK 17+ y agregalo al PATH. & exit /b 1)
where mvn   >nul 2>nul || (echo ERROR: No se encontro mvn.   Instala Maven y agregalo al PATH. & exit /b 1)
where jpackage >nul 2>nul || (echo ERROR: No se encontro jpackage. Necesitas JDK 17+. & exit /b 1)

echo.
echo == 2) Compilando fat JAR con Maven ==
call mvn -q clean package
if errorlevel 1 (echo ERROR: La compilacion fallo. & exit /b 1)

if not exist "target\renombrador-excel.jar" (
    echo ERROR: No se genero target\renombrador-excel.jar
    exit /b 1
)

echo.
echo == 3) Copiando JAR a la carpeta raiz ==
copy /Y "target\renombrador-excel.jar" "renombrador-excel.jar" >nul

echo.
echo == 4) Generando .exe con jpackage ==
if exist "dist" rmdir /S /Q "dist"
mkdir "dist"
mkdir "dist\input"
copy /Y "renombrador-excel.jar" "dist\input\" >nul

jpackage ^
  --type app-image ^
  --name RenombradorExcel ^
  --input dist\input ^
  --main-jar renombrador-excel.jar ^
  --main-class com.renombrador.Main ^
  --dest dist ^
  --win-console
if errorlevel 1 (
    echo ERROR: jpackage fallo. Revisa que tengas JDK 17+ completo.
    exit /b 1
)

REM jpackage en modo app-image crea dist\RenombradorExcel\RenombradorExcel.exe
copy /Y "dist\RenombradorExcel\RenombradorExcel.exe" "RenombradorExcel.exe" >nul 2>nul

echo.
echo == LISTO ==
echo  - renombrador-excel.jar  (doble click si tienes Java; o: java -jar renombrador-excel.jar)
echo  - dist\RenombradorExcel\ (carpeta portable con RenombradorExcel.exe y runtime de Java)
echo.
endlocal
