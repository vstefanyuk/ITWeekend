cd D:\Projects\Curiosity\res\video\mplayer\
nc64.exe %1 %2 | "mplayer.exe" - -fps 24 -demuxer h264es -wid %3
