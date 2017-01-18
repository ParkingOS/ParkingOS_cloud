@echo off
rem %cd%表示当前目录，注意等号前后没有空格
set folderName=%cd%
rem 过滤当前目录下的jar文件名
for /f "delims=\" %%a in ('dir /b /a-d /o-d "%folderName%*.jar"') do (
  java -Xms1024m -Xmx1024m -jar %%a
  break
)
@pause