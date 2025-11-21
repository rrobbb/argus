import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.io.IOException;
import java.io.PipedOutputStream;

public final class EncoderThread extends Thread {

    private final PipedOutputStream out;

    private final int FPS;

    public EncoderThread(PipedOutputStream out, int FPS) {

        this.out = out;

        this.FPS = FPS;

        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {

        Frame frame;

        final long TARGET_DELAY_MS = 1000 / FPS;

        try (final var grabber = getGrabber("1")) {

            final var recorder = getRecorder(grabber);

            grabber.start();

            recorder.start();

            while (!isInterrupted()) {

                long startTime = System.currentTimeMillis();

                frame = grabber.grab();

                if (frame != null && frame.image != null) recorder.record(frame);

                long elapsedTime = System.currentTimeMillis() - startTime;

                long sleepTime = TARGET_DELAY_MS - elapsedTime;

                if (sleepTime > 0) Thread.sleep(sleepTime);
            }

        } catch (IOException | InterruptedException ignored) {

            Thread.currentThread().interrupt();
        }
    }

    private FFmpegFrameGrabber getGrabber(String deviceID) {

        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        final var inputFormat = Utils.getInputFormat();

        final var grabber = new FFmpegFrameGrabber(deviceID);

        grabber.setFormat(inputFormat);
        grabber.setFrameRate(FPS);
        grabber.setImageWidth(screenSize.width);
        grabber.setImageHeight(screenSize.height);

        grabber.setOption("hwaccel", "videotoolbox");

        grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);

        return grabber;
    }

    private FFmpegFrameRecorder getRecorder(FFmpegFrameGrabber grabber) {

        final var recorder = new FFmpegFrameRecorder(out, grabber.getImageWidth(), grabber.getImageHeight());

        recorder.setVideoCodecName("h264_videotoolbox");
        // recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mpegts");
        recorder.setOption("color_range", "mpeg");
        recorder.setOption("tune", "zerolatency");
        recorder.setFrameRate(FPS);
        recorder.setGopSize(FPS * 2);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setVideoBitrate(4000000);

        return recorder;
    }
}
