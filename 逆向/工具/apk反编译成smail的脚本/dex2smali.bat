java -jar ..\baksmali-2.5.2.jar d --code-offsets classes.dex -o smali
for /L %%i in (2,1,65535) do java -jar ..\baksmali-2.5.2.jar d --code-offsets classes%%i.dex -o smali%%i
pause