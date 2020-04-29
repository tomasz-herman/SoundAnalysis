package pl.edu.pw.mini.hermant.audio.window;

public abstract class AudioWindow {

    public abstract String getName();
    public abstract float calculateCoefficient(int index, int sampleSize);

    @Override
    public String toString() {
        return getName();
    }
}
