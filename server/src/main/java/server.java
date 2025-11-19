import java.awt.image.BufferedImage;

void main(String[] args) throws IOException {

    final var address = InetAddress.getByName(args[0]);

    final var port = Integer.parseInt(args[1]);

    final var imageQueue = new ArrayBlockingQueue<BufferedImage>(5);

    final var bytesQueue = new ArrayBlockingQueue<byte[]>(5);

    new RecorderThread(imageQueue).start();

    new EncoderThread(imageQueue, bytesQueue).start();

    new SenderThread(bytesQueue, address, port).start();
}