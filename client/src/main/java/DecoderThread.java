import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class DecoderThread extends Thread {

    private final BlockingQueue<byte[]> decodingQueue;

    private final BlockingQueue<BufferedImage> imagesQueue;

    public DecoderThread(BlockingQueue<byte[]> decodingQueue, BlockingQueue<BufferedImage> imagesQueue) {
        this.decodingQueue = decodingQueue;
        this.imagesQueue = imagesQueue;
    }

    @Override
    public void run() {

        byte[] imageData;

        BufferedImage image;

        try {

            while (!isInterrupted()) {

                imageData = decodingQueue.take();

                image = ImageIO.read(new ByteArrayInputStream(imageData));

                if (image != null) {

                    boolean success = imagesQueue.offer(image, 100, TimeUnit.MILLISECONDS);

                    if (!success) System.out.println("Display Queue Full. Frame discarded.");
                }
            }

        } catch (InterruptedException ignored) {

            Thread.currentThread().interrupt();

        } catch (IOException e) {

            System.err.println("Decoding failed due to IOException: " + e.getMessage());
        }
    }
}
