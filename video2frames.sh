#!/bin/bash
mkdir "$1.temp"
mkdir "$1.temp/frames"
ffmpeg -i "$1" -vsync 0 -q:v 10 -vf "crop=in_w:in_h/2:0:in_h/2" -start_number 0 "$1.temp/frames/%08d.jpg" -hide_banner
pasue