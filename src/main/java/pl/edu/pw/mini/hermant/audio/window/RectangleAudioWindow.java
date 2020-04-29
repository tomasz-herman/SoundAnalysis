package pl.edu.pw.mini.hermant.audio.window;

public class RectangleAudioWindow extends AudioWindow {

    @Override
    public String getName() {
        return "Rectangle Audio Window";
    }

    @Override
    public float calculateCoefficient(int index, int sampleSize) {
        return 1.0f;
    }
}
