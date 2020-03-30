package pl.edu.pw.mini.hermant.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class FFMPEGAudioReader {

    @Nullable
    public static List<Float> readFile(String file) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        Process ffmpeg = runtime.exec(new String[]{"ffmpeg", "-i", file, "-vn", "-ar", "44100", "-ac", "1", "-f", "f32le", "-"});
        List<Float> samples = processOutput(ffmpeg.getInputStream());
        String error = processError(ffmpeg.getErrorStream());
        int exit = ffmpeg.waitFor(); // Get exit code;
        if (exit != 0) System.err.println(error);
        return exit == 0 ? samples : null;
    }

    @NotNull
    private static String processError(@NotNull InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        int read;
        byte[] buffer = new byte[(1024 + 1)];
        while ((read = stream.read(buffer, 0, 1024)) != -1) {
            String err = new String(buffer, 0, read);
            builder.append(err);
        }
        return builder.toString();
    }

    @NotNull
    public static List<Float> processOutput(InputStream stream) throws IOException {
        List<Float> samples = new ArrayList<>();
        int read;
        int offset = 0;
        byte[] buffer = new byte[Float.BYTES * (1024 + 1)];
        int length;
        int residualLength;
        while ((read = stream.read(buffer, offset, Float.BYTES * 1024)) != -1) {
            length = read + offset;
            residualLength = length % Float.BYTES;
            if (residualLength == 0) {
                processBuffer(samples, buffer, length);
                offset = 0;
            } else {
                length -= residualLength;
                System.err.println(length);
                processBuffer(samples, buffer, length);
                System.arraycopy(buffer, length, buffer, 0, residualLength);
                offset = residualLength;
            }
        }
        return samples;
    }

    private static void processBuffer(List<Float> samples, byte[] buffer, int length) {
        int index = 0;
        float sample_value;
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
        while (index < length) {
            sample_value = byteBuffer.getFloat(index);
            index += Float.BYTES;
            samples.add(sample_value);
        }
    }
}
