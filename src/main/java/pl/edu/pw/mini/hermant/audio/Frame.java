package pl.edu.pw.mini.hermant.audio;

import org.apache.commons.math3.complex.Complex;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pl.edu.pw.mini.hermant.audio.window.AudioWindow;
import org.jtransforms.fft.FloatFFT_1D;
import pl.edu.pw.mini.hermant.audio.window.RectangleAudioWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Frame {
    public static final double SAMPLE_RATE = 44100.0;
    public static final int SAMPLES_PER_FRAME = 1000;
    public static final double FRAME_TIME = SAMPLES_PER_FRAME / SAMPLE_RATE;
    public static final double SAMPLE_TIME = 1.0f / SAMPLE_RATE;

    private List<Float> samples;
    private List<FourierPoint> frequencies;

    private float volume, ste, zcr;
    private int frameStart;

    private float basicToneFrequency;

    public Frame(List<Float> samples, int frameStart) {
        this.samples = samples;
        this.frameStart = frameStart;
        calculateVolume();
        calculateFrequencies(new RectangleAudioWindow());
        calculateBasicTone(new RectangleAudioWindow());
    }

    public Stream<Float> getSamplesStream() {
        return samples.stream();
    }

    public float calculateBasicTone(AudioWindow window) {
//        if(volume < 0.02) return 0;
        float[] frame = new float[samples.size() * 2];
        for (int i = 0, samplesSize = samples.size(); i < samplesSize; i++) {
            Float sample = samples.get(i);
            sample = sample * window.calculateCoefficient(i, samplesSize);
            frame[i] = sample;
        }
        FloatFFT_1D fft = new FloatFFT_1D(samples.size());
        fft.realForwardFull(frame);
        float[] temp = new float[samples.size() * 2];
        for (int i = 0; i < samples.size(); i += 2) {
            float re = frame[i];
            float im = frame[i + 1];
            float sig = (float)Math.sqrt(re * re + im * im);
            sig = (float) Math.log10(sig);
            temp[i >> 1] = sig;
        }
        fft.realInverseFull(temp, false);
        float maxFrequency = 0;
        float maxAmplitude = Float.NEGATIVE_INFINITY;
        float[] reals = new float[samples.size()];
        for (int i = 0; i < temp.length; i+=2) {
            reals[i >> 1] = temp[i];
        }
        for (int j = 1; j <= reals.length; j++) {
            float freq = (float) SAMPLE_RATE / ((float) j);
            float amp = reals[j - 1];
            if (freq >= 50 && freq <= 400) {
                if (maxAmplitude < amp) {
                    maxAmplitude = amp;
                    maxFrequency = freq;
                }
            }
        }
        System.out.println(maxAmplitude + " " + volume);
        if(maxAmplitude < 12 || volume < 0.1 || maxFrequency == 0) basicToneFrequency = maxFrequency = 0;
        basicToneFrequency = maxFrequency;
        return basicToneFrequency;
    }

    public void calculateFrequencies(AudioWindow window) {
        frequencies = new ArrayList<>();
        float[] frame = new float[2 * samples.size()];
        for (int i = 0, samplesSize = samples.size(); i < samplesSize; i++) {
            Float sample = samples.get(i);
            sample = sample * window.calculateCoefficient(i, samplesSize);
            frame[i] = sample;
        }
        FloatFFT_1D fft = new FloatFFT_1D(samples.size());
        fft.realForwardFull(frame);
        for (int i = 0; i < samples.size(); i += 2) {
            float re = frame[i];
            float im = frame[i + 1];
            frequencies.add(TransformComplex(re, im, i >> 1));
        }
    }

    @Contract("_, _, _ -> new")
    private static @NotNull FourierPoint TransformComplex(float re, float im, int index) {
        float frequency = (float) index / Frame.SAMPLES_PER_FRAME * (float) SAMPLE_RATE;
        float amplitude = (float) Math.sqrt(re * re + im * im);
        return new FourierPoint(frequency, amplitude);
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
        zcr *= (float) samples.size() / SAMPLES_PER_FRAME;
    }

    public boolean isVoiceless() {
        return ste < 0.005 && !isSilence();
    }

    public boolean isVoiced() {
        return ste > 0.005;
    }

    public boolean isSilence() {
        return (zcr > 48 && volume < 0.02) || (volume < 0.01 && zcr > 24) || volume < 0.005;
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

    public int getFrameStart() {
        return frameStart;
    }

    public List<FourierPoint> getFrequencies() {
        return frequencies;
    }
}
