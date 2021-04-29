@ECHO OFF
start "" /d "%~dp0" "%~dp0ffmpeg.exe" -i "%~1" -vsync 0 -start_number 0 "%~1".temp\frames\%%08d.jpg -hide_banner
EXIT