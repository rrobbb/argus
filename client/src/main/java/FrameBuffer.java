import java.io.IOException;
import java.util.Arrays;

public class FrameBuffer {

    private final byte[][] chunks;

    private final int expectedSize;

    private int receivedChunks = 0, receivedBytes = 0;

    private final long startTime;

    private long lastTime;

    public FrameBuffer(int total, int expectedSize) {
        chunks = new byte[total][];
        startTime = System.currentTimeMillis();
        lastTime = startTime;
        this.expectedSize = expectedSize;
    }

    public void addChunk(int index, byte[] data) {
        if (chunks[index] == null) {
            chunks[index] = data;
            receivedChunks++;
            receivedBytes += data.length;
            lastTime = System.currentTimeMillis();
        }
    }

    public boolean isComplete() { return receivedChunks == chunks.length; }

    public boolean isExpired(long timeoutMs) { return System.currentTimeMillis() - startTime > timeoutMs; }

    public boolean isStalled(long stallTimeoutMs) { return System.currentTimeMillis() - lastTime > stallTimeoutMs && !isComplete(); }

    public byte[] join() throws IOException {

        if (!isComplete()) throw new IllegalStateException("Cannot join an incomplete frame.");

        var buffer = new byte[expectedSize];

        int offset = 0;

        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, buffer, offset, chunk.length);
            offset += chunk.length;
        }

        if (offset != buffer.length) return Arrays.copyOf(buffer, offset);

        return buffer;
    }
}
