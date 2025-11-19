import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public final class AssemblyThread extends Thread {

    private static final int HEADER_SIZE = 12;

    private static final long FRAME_TIMEOUT_MS = 5000;

    private static final int CLEANUP_INTERVAL = 200;

    private final LinkedBlockingQueue<byte[]> processingQueue, decodingQueue;

    public AssemblyThread(LinkedBlockingQueue<byte[]> processingQueue, LinkedBlockingQueue<byte[]> decodingQueue) {
        this.processingQueue = processingQueue;
        this.decodingQueue = decodingQueue;
    }

    @Override
    public void run() {

        var frames = new ConcurrentHashMap<Integer, FrameBuffer>();

        FrameBuffer frameBuffer;

        byte[] packetData, chunk, imageData;

        int frameID, index, total, expectedSize, packetLength;

        int packetCount = 0;

        try {

            while (!isInterrupted()) {

                packetData = processingQueue.take();

                packetLength = packetData.length;

                if (packetLength < HEADER_SIZE) continue;

                frameID = ((packetData[0] & 0xFF) << 24) | ((packetData[1] & 0xFF) << 16) | ((packetData[2] & 0xFF) << 8) | (packetData[3] & 0xFF);

                index = ((packetData[4] & 0xFF) << 8) | (packetData[5] & 0xFF);

                total = ((packetData[6] & 0xFF) << 8) | (packetData[7] & 0xFF);

                expectedSize = ((packetData[8] & 0xFF) << 24) | ((packetData[9] & 0xFF) << 16) | ((packetData[10] & 0xFF) << 8) | (packetData[11] & 0xFF);

                chunk = Arrays.copyOfRange(packetData, HEADER_SIZE, packetLength);

                frames.putIfAbsent(frameID, new FrameBuffer(total, expectedSize));

                frameBuffer = frames.get(frameID);

                frameBuffer.addChunk(index, chunk);

                if (frameBuffer.isComplete()) {

                    try {

                        imageData = frameBuffer.join();

                        frames.remove(frameID);

                        decodingQueue.offer(imageData);

                    } catch (IOException e) {

                        System.err.println("Failed to decode frame " + frameID);
                    }
                }

                packetCount++;

                if (packetCount >= CLEANUP_INTERVAL) {
                    frames.entrySet().removeIf(e -> e.getValue().isExpired(FRAME_TIMEOUT_MS));
                    packetCount = 0;
                }
            }

        } catch (InterruptedException e) {

            throw new RuntimeException(e);
        }
    }
}
