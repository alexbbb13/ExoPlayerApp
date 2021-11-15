A test app with the ExoPlayer and the accelerometers functionality

1. Using Exoplayer (https://developer.android.com/guide/topics/media/exoplayer), load
and play a video file 4 seconds after launch. Video file
(http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingO
nBullrun.mp4). Video file should be loaded over http.
2. Using the user’s location, a change of 10 meters of the current and previous location
will reset the video and replay from the start.
3. A shake of the device should pause the video.
4. Using gyroscope events, rotation along the z-axis should be able to control the
current time where the video is playing. While rotation along the x-axis should control
the volume of the sound.