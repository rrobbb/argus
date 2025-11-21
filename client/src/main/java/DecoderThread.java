import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;

public final class DecoderThread extends Thread {

    private final int port;

    private final boolean isServerMode;

    private final BlockingQueue<BufferedImage> imageQueue;

    public DecoderThread(int port, boolean isServerMode, BlockingQueue<BufferedImage> imageQueue) {
        this.port = port;
        this.isServerMode = isServerMode;
        this.imageQueue = imageQueue;
    }

    @Override
    public void run() {

        Socket connectionSocket = null;

        try (final var serverSocket = new ServerSocket(port) ) {

            if (isServerMode) {

                System.out.println("Client in ascolto sulla porta " + port + "...");

                connectionSocket = serverSocket.accept();

                System.out.println("Connessione accettata dal server.");

            } else {

                System.out.println("Client si connette a localhost:" + port + "...");

                connectionSocket = new Socket("localhost", port);

                System.out.println("Connessione stabilita.");
            }

            var inputStream = connectionSocket.getInputStream();

            try (final var grabber = new FFmpegFrameGrabber(inputStream, 0)) {

                grabber.setFormat("mpegts");
                grabber.setVideoCodecName("h264_videotoolbox");
                grabber.setOption("analyzeduration", "5000000");
                grabber.setOption("probesize", "1000000");
                grabber.start();

                try (final var converter = new Java2DFrameConverter()) {

                    Frame frame;

                    BufferedImage decodedImage;

                    while (!isInterrupted() && (frame = grabber.grab()) != null) {

                        if (frame.image != null) {
                            decodedImage = converter.getBufferedImage(frame);
                            imageQueue.offer(decodedImage);
                        }
                    }
                }

                grabber.stop();

            } catch (Exception e) {

                System.err.println("Errore durante la decodifica o I/O: " + e.getMessage());
            }

        } catch (IOException e) {

            System.err.println("Errore di connessione: " + e.getMessage());

        } finally {

            try {

                if (connectionSocket != null) connectionSocket.close();

            } catch (IOException ignored) {}

            Thread.currentThread().interrupt();
        }
    }
}