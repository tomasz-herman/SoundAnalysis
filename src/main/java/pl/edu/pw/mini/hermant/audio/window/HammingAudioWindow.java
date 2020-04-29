package pl.edu.pw.mini.hermant.audio.window;

public class HammingAudioWindow extends AudioWindow {

    @Override
    public String getName() {
        return "Hamming Audio Window";
    }

    @Override
    public float calculateCoefficient(int index, int sampleSize) {
        return (float) (0.54 - 0.46 * Math.cos(2 * Math.PI * index / (sampleSize - 1)));
    }
}
