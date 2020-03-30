package pl.edu.pw.mini.hermant.audio;

import pl.edu.pw.mini.hermant.io.FFMPEGAudioReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Clip {
    public static final float SAMPLE_RATE = 44100.0f;
    public static final int SAMPLES_PER_FRAME = 1000;
    public static final float FRAME_TIME = SAMPLES_PER_FRAME / SAMPLE_RATE;
    public static final float SAMPLE_TIME = 1.0f / SAMPLE_RATE;

    private List<Float> samples;
    private List<Frame> frames; // 22ms frames

    private float volume, ste;

    public Clip(String file) throws IOException, InterruptedException {
        samples = FFMPEGAudioReader.readFile(file);
        calculateVolume();
        makeFrames();
        frames.parallelStream().forEach(Frame::calculateVolume);
        frames.parallelStream().forEach(Frame::calculateZeroCrossingRate);
    }

    private void makeFrames(){
        frames = new ArrayList<>();
        int i = 0;
        for (int size = samples.size() / SAMPLES_PER_FRAME; i < size; i++)
            frames.add(new Frame(samples.subList(i * SAMPLES_PER_FRAME, (i + 1) * SAMPLES_PER_FRAME)));
        List<Float> lastList = samples.subList(i * SAMPLES_PER_FRAME, samples.size());
        if(lastList.size() > SAMPLES_PER_FRAME / 10) frames.add(new Frame(lastList));
    }

    public void calculateVolume(){
        volume = 0;
        for (Float sample : samples) volume += sample * sample;
        ste = (volume /= SAMPLES_PER_FRAME);
        volume = (float)Math.sqrt(ste);
    }

    public float getVolume() {
        return volume;
    }

    public float getShortTimeEnergy() {
        return ste;
    }

    public List<Float> getSamples(){
        return samples;
    }

    public List<Frame> getFrames(){
        return frames;
    }

    public int getSamplesNum() {
        return samples.size();
    }

    public int getFramesNum() {
        return frames.size();
    }


}
