import java.awt.image.BufferedImage;

void main(String[] args) throws IOException {

    final var ADDRESS = InetAddress.getByName(args[0]);

    final var PORT = Integer.parseInt(args[1]);

    final var FPS = Integer.parseInt(args[2]);

    final var imageQueue = new ArrayBlockingQueue<BufferedImage>(5);

    final var bytesQueue = new ArrayBlockingQueue<byte[]>(5);

    new RecorderThread(imageQueue, FPS).start();

    new EncoderThread(imageQueue, bytesQueue).start();

    new SenderThread(bytesQueue, ADDRESS, PORT).start();
}