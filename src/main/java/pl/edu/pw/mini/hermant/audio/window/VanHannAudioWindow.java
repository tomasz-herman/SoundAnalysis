package pl.edu.pw.mini.hermant.audio.window;

public class VanHannAudioWindow extends AudioWindow {

    @Override
    public String getName() {
        return "Van Hann Audio Window";
    }

    @Override
    public float calculateCoefficient(int index, int sampleSize) {
        return (float) (0.5 * (1.0 - Math.cos(2 * Math.PI * index / (sampleSize - 1))));
    }
}
