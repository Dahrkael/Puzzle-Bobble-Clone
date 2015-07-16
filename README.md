#Puzzle Bobble Clone

Project for the CFGS DAM mobile development subject.
Made in 5 afternoons! (needs some polishing)

Controls:
- Tap on the top half of the screen to rotate the cannon.
- Tap the cannon to shoot.


Made in Java with libGDX for desktop and android:
- PuzzleBobble
	Main project, heres the games source code
- PuzzleBobble-Desktop
	Proxy project for running the desktop version
	Used during development to speed up work
	
- PuzzleBobble-Android
	Proxy project to build the android apk
	All resources are in the assets folder
	

The game as the name states is a puzzle bobble clone.
I made all the graphics apart from the code, thats why it looks so ugh.
Also I lost parts of the code, so I had to rewrite them from the decompiled apk,
so don't panic if you find some ugly snippet, or lack of comments.

About the inner workings, the game loads each round from the rounds folder
from json files, which specify the position and color of each sphere, in order from
1 to whatever number of files exist. The collision detection is grid based, to keep it simple.
The shooting spheres are randomly generated based on the remaining spheres.

