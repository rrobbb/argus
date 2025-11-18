import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

static final int PORT = 5900;
static final int MAX_UDP_PACKET = 65000;
static final long FRAME_TIMEOUT_MS = 5000;

void main() throws IOException {

    try (var socket = new DatagramSocket(PORT)) {

        var frames = new ConcurrentHashMap<Integer, FrameBuffer>();

        var renderer = new Renderer();

        var renderQueue = new LinkedBlockingQueue<BufferedImage>();

        new Thread(() -> {

            while (true) {
                try {
                    var image = renderQueue.take();
                    renderer.display(image);
                } catch (InterruptedException ignored) {}
            }
        }).start();

        var buffer = new byte[MAX_UDP_PACKET];

        while (true) {

            var packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);

            byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

            int frameID = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) |
                ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

            int index = ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
            int total = ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);

            byte[] chunk = Arrays.copyOfRange(data, 8, data.length);

            frames.putIfAbsent(frameID, new FrameBuffer(total));
            FrameBuffer frameBuffer = frames.get(frameID);
            frameBuffer.addChunk(index, chunk);

            if (frameBuffer.isComplete()) {
                try {
                    byte[] imgData = frameBuffer.join();
                    frames.remove(frameID);

                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgData));
                    if (img != null) {
                        renderQueue.offer(img);
                        System.out.println("Rendered frame " + frameID);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to decode frame " + frameID);
                }
            }

            frames.entrySet().removeIf(e -> e.getValue().isExpired(FRAME_TIMEOUT_MS));
        }
    }
}

private static class FrameBuffer {

    private final byte[][] chunks;
    private int received = 0;
    private final long startTime;

    public FrameBuffer(int total) {
        chunks = new byte[total][];
        startTime = System.currentTimeMillis();
    }

    public void addChunk(int index, byte[] data) {
        if (chunks[index] == null) {
            chunks[index] = data;
            received++;
        }
    }

    public boolean isComplete() {
        return received == chunks.length;
    }

    public boolean isExpired(long timeoutMs) {
        return System.currentTimeMillis() - startTime > timeoutMs;
    }

    public byte[] join() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] chunk : chunks) {
            if (chunk != null) out.write(chunk);
        }
        return out.toByteArray();
    }
}
