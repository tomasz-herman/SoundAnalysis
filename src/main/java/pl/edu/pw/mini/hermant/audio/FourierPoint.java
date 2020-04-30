package pl.edu.pw.mini.hermant.audio;

public class FourierPoint {
    public float frequency;
    public float amplitude;

    public FourierPoint(float frequency, float amplitude) {
        this.frequency = frequency;
        this.amplitude = amplitude;
    }

    public float getFrequency() {
        return frequency;
    }

    public float getAmplitude() {
        return amplitude;
    }
}
