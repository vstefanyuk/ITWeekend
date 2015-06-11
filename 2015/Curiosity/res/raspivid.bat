raspivid -t 0 -w 640 -h 360 -fps 8 -b 8000000 -i pause --timed 2147483647,2000 -o - | nc -l -p 5001

