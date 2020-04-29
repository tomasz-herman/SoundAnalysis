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

    public void calculateBasicTone(AudioWindow window) {
        float[] frame = new float[2 * samples.size()];
        for (int i = 0, samplesSize = samples.size(); i < samplesSize; i++) {
            Float sample = samples.get(i);
            sample = sample * window.calculateCoefficient(i, samplesSize);
            frame[i] = sample;
        }
        FloatFFT_1D fft = new FloatFFT_1D(samples.size());
        FloatFFT_1D fft2 = new FloatFFT_1D(samples.size() / 2);
        float[] half = new float[samples.size()];
        fft.realForwardFull(frame);
        for (int i = 0; i < samples.size(); i+=2) {
            float re = frame[i];
            float im = frame[i + 1];
            float sig = re * re + im * im;
            if(sig <= 0.000001) sig = 0.0f;
            else sig = (float)Math.log10(sig);
            half[i] = sig;
        }
        fft2.realInverseFull(half, true);
        float reF = half[0];
        float imF = half[1];
        Complex first = new Complex(reF, imF);
        Complex[] cepstrum = new Complex[samples.size() / 2];
        for (int i = 0; i < cepstrum.length; i+=2) {
            Complex c = new Complex(half[i], half[i+1]);
//            cepstrum[i] = c.divide(first);
            System.out.println(c);
        }
    }

    public void calculateFrequencies(AudioWindow window){
        frequencies = new ArrayList<>();
        float[] frame = new float[2 * samples.size()];
        for (int i = 0, samplesSize = samples.size(); i < samplesSize; i++) {
            Float sample = samples.get(i);
            sample = sample * window.calculateCoefficient(i, samplesSize);
            frame[i] = sample;
        }
        FloatFFT_1D fft = new FloatFFT_1D(samples.size());
        fft.realForwardFull(frame);
        for (int i = 0; i < samples.size(); i+=2) {
            float re = frame[i];
            float im = frame[i + 1];
            frequencies.add(TransformComplex(re, im, i >> 1));
        }
    }

    @Contract("_, _, _ -> new")
    private static @NotNull FourierPoint TransformComplex(float re, float im, int index)
    {
        float frequency = (float)index / Frame.SAMPLES_PER_FRAME * (float) SAMPLE_RATE;
        float amplitude = (float)Math.sqrt(re * re + im * im);
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

    public int getFrameStart() {
        return frameStart;
    }

    public List<FourierPoint> getFrequencies() {
        return frequencies;
    }
}
