import java.awt.*;
import java.awt.image.BufferedImage;

void main(String[] args) throws AWTException, IOException {

    final var address = InetAddress.getByName(args[0]);

    final var port = Integer.parseInt(args[1]);

    final var imageQueue = new ArrayBlockingQueue<BufferedImage>(5);

    final var bytesQueue = new ArrayBlockingQueue<byte[]>(5);

    final var recorderThread = new RecorderThread(imageQueue);

    final var compressorThread = new EncoderThread(imageQueue, bytesQueue);

    final var senderThread = new SenderThread(bytesQueue, address, port);

    recorderThread.start();

    compressorThread.start();

    senderThread.start();
}