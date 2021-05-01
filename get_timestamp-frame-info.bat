@ECHO OFF
ffprobe.exe "%~1" -select_streams v -show_entries frame=coded_picture_number,pkt_pts_time -of csv=p=0:nk=1 -v 0 > "%~1.txt"
MOVE "%~1.txt" "%~1.temp"
EXIT