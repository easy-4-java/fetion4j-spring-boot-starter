package net.apexes.fetion4j.core.client.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;

/**
 * Unit tests for {@link TcpTransfer} using a loopback echo server.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("TcpTransfer Tests")
class TcpTransferTest {

    @Test
    @DisplayName("startTransfer connects to a local server and write/read round-trips a SIPC message")
    void testStartWriteRead() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread acceptor = new Thread(() -> {
                try (Socket client = server.accept()) {
                    // echo the bytes back
                    byte[] buf = new byte[4096];
                    int n = client.getInputStream().read(buf);
                    OutputStream out = client.getOutputStream();
                    out.write(buf, 0, n);
                    out.flush();
                    Thread.sleep(50);
                } catch (Exception ignored) {
                }
            });
            acceptor.setDaemon(true);
            acceptor.start();

            TcpTransfer transfer = new TcpTransfer("127.0.0.1", port);
            transfer.startTransfer();
            assertThat(transfer.isClosed()).isFalse();
            assertThat(transfer.getTransferName()).startsWith("TcpTransfer-");

            RequestMessage request = new RequestMessage(Sipc.METHOD_R);
            request.setCallId(1);
            request.setSequence(1);
            transfer.write(request);
            // read the echoed message back
            Object read = transfer.read();
            assertThat(read).isNotNull();
            transfer.stopTransfer();
            assertThat(transfer.isClosed()).isTrue();
        }
    }
}
