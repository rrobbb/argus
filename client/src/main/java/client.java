import java.awt.image.BufferedImage;

void main(String[] args) {

    final var port = Integer.parseInt(args[0]);

    var processingQueue = new LinkedBlockingQueue<byte[]>();

    var decodingQueue = new LinkedBlockingQueue<byte[]>();

    var imageQueue = new LinkedBlockingQueue<BufferedImage>();

    new ReceiverThread(processingQueue, port).start();

    new AssemblyThread(processingQueue, decodingQueue).start();

    new DecoderThread(decodingQueue, imageQueue).start();

    new RendererThread(imageQueue).start();
}
