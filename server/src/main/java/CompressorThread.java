import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class CompressorThread extends Thread {

    private final BlockingQueue<BufferedImage> inputQueue;

    private final BlockingQueue<byte[]> outputQueue;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(1_000_000);

    public CompressorThread(BlockingQueue<BufferedImage> inputQueue, BlockingQueue<byte[]> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }


    @Override
    public void run() {
        try {
            while (true) {

                var img = inputQueue.take();

                buffer.reset();

                ImageIO.write(img, "jpg", buffer);

                outputQueue.put(buffer.toByteArray());
            }
        } catch (InterruptedException | IOException ignored) {}
    }
}