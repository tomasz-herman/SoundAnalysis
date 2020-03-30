package pl.edu.pw.mini.hermant.audio;

import java.util.List;
import java.util.stream.Stream;

public class Frame {
    public static final double SAMPLE_RATE = 44100.0;
    public static final int SAMPLES_PER_FRAME = 1000;
    public static final double FRAME_TIME = SAMPLES_PER_FRAME / SAMPLE_RATE;

    private List<Float> samples;

    private float volume, ste, zcr;

    public Frame(List<Float> samples) {
        this.samples = samples;
        calculateVolume();
    }

    public Stream<Float> getSamplesStream() {
        return samples.stream();
    }

    public void calculateVolume() {
        volume = 0.0f;
        for (Float sample : samples) volume += sample * sample;
        ste = (volume /= SAMPLES_PER_FRAME);
        volume = (float) Math.sqrt(ste);
    }

    public void calculateZeroCrossingRate() {
        zcr = 0.0f;
        for (int i = 0, samplesSize = samples.size(); i < samplesSize - 1; i++) {
            float sample = samples.get(i);
            float nextSample = samples.get(i + 1);
            if ((Math.signum(sample) - Math.signum(nextSample)) != 0) zcr += 1.0f;
        }
        zcr *= (float)samples.size() / SAMPLES_PER_FRAME;
    }

    public boolean isVoiceless() {
        return ste < 0.005 && !isSilence();
    }

    public boolean isVoiced() {
        return ste > 0.005;
    }

    public boolean isSilence() {
        return ( zcr > 48 && volume < 0.02 ) || (volume < 0.01 && zcr > 24) || volume < 0.005;
    }

    public float getVolume() {
        return volume;
    }

    public float getShortTimeEnergy() {
        return ste;
    }

    public float getZeroCrossingRate() {
        return zcr;
    }
}
