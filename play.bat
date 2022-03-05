@ECHO OFF
mkdir bin
xcopy /s /i /y /d "textures" "bin/textures"
xcopy /s /i /y /d "tracks" "bin/tracks"
javac -d "bin" RacetrackGame.java
cd bin
java RacetrackGame