import java.awt.*;
import java.awt.image.BufferedImage;

void main(String[] args) throws AWTException, IOException {

    final var imageQueue = new ArrayBlockingQueue<BufferedImage>(5);

    final var bytesQueue = new ArrayBlockingQueue<byte[]>(5);

    final var recorderThread = new RecorderThread(imageQueue);

    final var compressorThread = new CompressorThread(imageQueue, bytesQueue);

    final var senderThread = new SenderThread(bytesQueue, InetAddress.getByName(args[0]), 60000);

    recorderThread.start();

    compressorThread.start();

    senderThread.start();
}