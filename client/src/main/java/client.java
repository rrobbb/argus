import java.awt.image.BufferedImage;

void main(String[] args) {

    final var port = Integer.parseInt(args[0]);

    var imageQueue = new ArrayBlockingQueue<BufferedImage>(1);

    new DecoderThread(port, false, imageQueue).start();

    new RendererThread(imageQueue).start();
}
