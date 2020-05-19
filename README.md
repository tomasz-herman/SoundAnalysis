# Sound Analysis

Application to analyze sound files in time or frequency domain.  
It uses `ffmpeg` to turn any audio or video file into raw audio data.
Java 8+ is required to run this application.

### Run the project

To run the application do the following steps:

- Make sure to be in the root directory of the project
- Clean and build the project, run the command:
```
mvn install
```
This will also generate a jar file with all the dependencies which we will run once it has been created.
- Run the `main` method in `Main.java` by running:
```
mvn exec:java
```
- Alternatively, you can run the `main` method in `Main.java` in your chosen IDE, e.g. IntelliJ

### Functionality

Analysis in time domain:
- amplitude chart
- volume chart
- short time energy chart
- zero crossing rate chart
- silence/voiceless speech/voiced speech

Analysis in frequency domain:
- frequency chart
- frequency spectrum chart
- base tone chart

Clip parameters:
- volume
- volume dynamic range
- average short time energy
- low short time energy ratio
- high zero crossing rate ratio
- standard deviation of the zcr
- music/speech recognition

### Gallery

**Amplitude chart**

![silence](https://raw.githubusercontent.com/tomasz-herman/SoundAnalysis/master/gallery/silence.png)

**Spectrum**

![spectrum_1](https://raw.githubusercontent.com/tomasz-herman/SoundAnalysis/master/gallery/spectrum_1.png)
![Spectrum_2](https://raw.githubusercontent.com/tomasz-herman/SoundAnalysis/master/gallery/spectrum_2.png)

**Base tone**

![base_tone_1](https://raw.githubusercontent.com/tomasz-herman/SoundAnalysis/master/gallery/base_tone_1.png)
![base_tone_2](https://raw.githubusercontent.com/tomasz-herman/SoundAnalysis/master/gallery/base_tone_2.png)

**Clip_parameters**

![clip_parameters](https://raw.githubusercontent.com/tomasz-herman/SoundAnalysis/master/gallery/clip_parameters.png)