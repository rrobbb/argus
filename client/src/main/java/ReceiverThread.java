import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public final class ReceiverThread extends Thread {

    private static final int MAX_UDP_PACKET = 65000;

    private final LinkedBlockingQueue<byte[]> processingQueue;

    private final int port;

    public ReceiverThread(LinkedBlockingQueue<byte[]> processingQueue, int port) {

        this.processingQueue = processingQueue;

        this.port = port;
    }

    @Override
    public void run() {

        final var buffer = new byte[MAX_UDP_PACKET];

        DatagramPacket packet;

        int packetLength;

        byte[] packetData;

        try (var socket = new DatagramSocket(port)) {

            socket.setReceiveBufferSize(4_000_000);

            while (!isInterrupted()) {

                packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                packetLength = packet.getLength();

                packetData = Arrays.copyOf(buffer, packetLength);

                boolean success = processingQueue.offer(packetData);

                if (!success) System.err.println("Processing Queue Full. Dropping packet.");
            }

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}
