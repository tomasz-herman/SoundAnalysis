package pl.edu.pw.mini.hermant.audio;

import pl.edu.pw.mini.hermant.io.FFMPEGAudioReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Clip {
    public static final double SAMPLE_RATE = 44100.0f;
    public static final int SAMPLES_PER_FRAME = 1000;
    public static final double FRAME_TIME = SAMPLES_PER_FRAME / SAMPLE_RATE;
    public static final double SAMPLE_TIME = 1.0f / SAMPLE_RATE;

    private List<Float> samples;
    private List<Frame> frames; // 22ms frames

    private float volume, ste, vdr, minVolume, maxVolume, avgZCR, lster, hzcrr, zstd;

    public Clip(String file) throws IOException, InterruptedException {
        samples = FFMPEGAudioReader.readFile(file);
        calculateVolume();
        makeFrames();
        frames.parallelStream().forEach(Frame::calculateVolume);
        frames.parallelStream().forEach(Frame::calculateZeroCrossingRate);
        calculateVolumeDynamicRange();
        calculateAverageZeroCrossingRate();
        calculateHighZeroCrossingRateRatio();
        calculateLowShortTimeEnergyRatio();
        calculateStandardDeviationOfTheZCR();
    }

    private void makeFrames() {
        frames = new ArrayList<>();
        int i = 0;
        for (int size = samples.size() / SAMPLES_PER_FRAME; i < size; i++)
            frames.add(new Frame(samples.subList(i * SAMPLES_PER_FRAME, (i + 1) * SAMPLES_PER_FRAME), i * SAMPLES_PER_FRAME));
        List<Float> lastList = samples.subList(i * SAMPLES_PER_FRAME, samples.size());
        if (lastList.size() > SAMPLES_PER_FRAME / 10) frames.add(new Frame(lastList, i * SAMPLES_PER_FRAME));
    }

    public List<Frame> getOverlappingFrames(float overlap) {
        List<Frame> frames = new ArrayList<>();
        int advance = (int)((1 - overlap) * SAMPLES_PER_FRAME);
        if(advance <= 0) advance = 1;
        int i = 0;
        while(i + SAMPLES_PER_FRAME < getSamplesNum()) {
            Frame frame = new Frame(samples.subList(i, i + SAMPLES_PER_FRAME), i);
            frames.add(frame);
            i += advance;
        }
        return frames;
    }


    private void calculateVolume() {
        volume = 0;
        for (Float sample : samples) volume += sample * sample;
        ste = (volume /= getSamplesNum());
        volume = (float) Math.sqrt(ste);
    }

    private void calculateLowShortTimeEnergyRatio() {
        float sum = 0.0f;
        for (Frame frame : frames) {
            sum += (Math.signum(0.5f * ste - frame.getShortTimeEnergy()) + 1);
        }
        lster = sum / (2 * getFramesNum());
    }

    private void calculateHighZeroCrossingRateRatio() {
        float sum = 0.0f;
        for (Frame frame : frames) {
            sum += Math.signum(frame.getZeroCrossingRate() - 1.5f * avgZCR) + 1;
        }
        hzcrr = sum / (2 * getFramesNum());
    }

    private void calculateStandardDeviationOfTheZCR() {
        float sum = 0.0f;
        for (Frame frame : frames)
            sum += (frame.getZeroCrossingRate() - avgZCR) * (frame.getZeroCrossingRate() - avgZCR);
        zstd = (float) Math.sqrt(sum / getFramesNum());
    }

    public float getLowShortTimeEnergyRatio() {
        return lster;
    }

    public float getHighZeroCrossingRateRatio() {
        return hzcrr;
    }

    public float getStandardDeviationOfTheZCR() {
        return zstd;
    }

    private void calculateVolumeDynamicRange() {
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (Frame frame : frames) {
            if (frame.getVolume() < min) min = frame.getVolume();
            if (frame.getVolume() > max) max = frame.getVolume();
        }
        vdr = (max - min) / max;
        minVolume = min;
        maxVolume = max;
    }

    private void calculateAverageZeroCrossingRate() {
        avgZCR = (float) frames.stream().mapToDouble(Frame::getZeroCrossingRate).average().orElse(0.0);
    }

    public float getMinVolume() {
        return minVolume;
    }

    public float getMaxVolume() {
        return maxVolume;
    }

    public float getVolume() {
        return volume;
    }

    public float getShortTimeEnergy() {
        return ste;
    }

    public List<Float> getSamples() {
        return samples;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public int getSamplesNum() {
        return samples.size();
    }

    public int getFramesNum() {
        return frames.size();
    }

    public int getFramesNum(float overlap) {
        int advance = (int)((1 - overlap) * SAMPLES_PER_FRAME);
        if(advance <= 0) advance = 1;
        int i = 0;
        int k = 0;
        while(i + SAMPLES_PER_FRAME < getSamplesNum()) {
            k++;
            i += advance;
        }
        return k;
    }

    public float getVolumeDynamicRange() {
        return vdr;
    }

    public float getAverageZeroCrossingRate() {
        return avgZCR;
    }

    public boolean isMusic() {
        return lster < 0.5f;
    }
}
