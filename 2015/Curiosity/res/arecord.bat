arecord -D plughw:1,0 -t raw -c 1 -f U8 -r 16000 -R 2000000 | nc -l -p 5001
