import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public final class EncoderThread extends Thread {

    private final BlockingQueue<BufferedImage> inputQueue;

    private final BlockingQueue<byte[]> outputQueue;

    public EncoderThread(BlockingQueue<BufferedImage> inputQueue, BlockingQueue<byte[]> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {

        final var buffer = new ByteArrayOutputStream(1_000_000);

        BufferedImage image;

        try {

            while (!isInterrupted()) {

                image = inputQueue.take();

                buffer.reset();

                ImageIO.write(image, "jpg", buffer);

                outputQueue.put(buffer.toByteArray());
            }
        } catch (InterruptedException | IOException ignored) {}
    }
}