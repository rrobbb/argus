import java.awt.*;
import java.awt.image.BufferedImage;

void main() throws AWTException, IOException {

    final var imageQueue = new ArrayBlockingQueue<BufferedImage>(5);

    final var bytesQueue = new ArrayBlockingQueue<byte[]>(5);

    final var recorderThread = new RecorderThread(imageQueue);

    final var compressorThread = new CompressorThread(imageQueue, bytesQueue);

    final var senderThread = new SenderThread(bytesQueue, InetAddress.getByName("10.152.73.81"), 5900);

    recorderThread.start();

    compressorThread.start();

    senderThread.start();
}