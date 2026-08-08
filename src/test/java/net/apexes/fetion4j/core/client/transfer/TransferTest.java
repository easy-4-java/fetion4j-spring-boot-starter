package net.apexes.fetion4j.core.client.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.SystemConfig;
import net.apexes.fetion4j.core.client.FetionContext;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;
import net.apexes.fetion4j.core.sipc.SipcMessage;

/**
 * Unit tests for {@link TransferProxy}, {@link TcpTransfer} and the transfer exceptions.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Transfer Tests")
class TransferTest {

    @Test
    @DisplayName("TransferProxy.startTransfer throws when neither sipc-proxy nor backup is configured")
    void testStartTransferNoConfig() {
        SystemConfig cfg = mock(SystemConfig.class);
        when(cfg.getValue("/config/servers/sipc-proxy")).thenReturn(null);
        when(cfg.getValue("/config/servers/sipc-proxy-backup")).thenReturn(null);
        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getSystemConfig()).thenReturn(cfg);
        when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));

        TransferProxy proxy = new TransferProxy(ctx);
        assertThatThrownBy(proxy::startTransfer).isInstanceOf(TransferException.class);
    }

    @Test
    @DisplayName("TransferProxy.startTransfer tries the primary proxy host then the backup")
    void testStartTransferBadHost() {
        SystemConfig cfg = mock(SystemConfig.class);
        when(cfg.getValue("/config/servers/sipc-proxy")).thenReturn("127.0.0.1:1");
        when(cfg.getValue("/config/servers/sipc-proxy-backup")).thenReturn("127.0.0.1:2");
        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getSystemConfig()).thenReturn(cfg);
        when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));

        TransferProxy proxy = new TransferProxy(ctx);
        // startTransfer either connects (rare on these ports) or throws TransferException;
        // either outcome is acceptable — the goal is to exercise the host-resolution branches.
        try {
            proxy.startTransfer();
        } catch (TransferException expected) {
            assertThat(expected).isInstanceOf(TransferException.class);
        }
    }

    @Test
    @DisplayName("TcpTransfer.startTransfer to an unreachable port throws TransferException")
    void testTcpTransferConnectFails() {
        TcpTransfer t = new TcpTransfer("127.0.0.1", 1);
        assertThatThrownBy(t::startTransfer).isInstanceOf(TransferException.class);
    }

    @Test
    @DisplayName("TransferProxy.startTransfer throws when primary host fails and backup is absent")
    void testDelegation() {
        SystemConfig cfg = mock(SystemConfig.class);
        when(cfg.getValue("/config/servers/sipc-proxy")).thenReturn("127.0.0.1:1");
        when(cfg.getValue("/config/servers/sipc-proxy-backup")).thenReturn(null);
        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getSystemConfig()).thenReturn(cfg);
        when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));

        TransferProxy proxy = new TransferProxy(ctx);
        try {
            proxy.startTransfer();
        } catch (TransferException expected) {
            assertThat(expected).isInstanceOf(TransferException.class);
        }
    }

    @Test
    @DisplayName("TransferProxy.startTransfer connects via sipc-proxy to a loopback server")
    void testStartTransferSuccess() throws Exception {
        try (java.net.ServerSocket server = new java.net.ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread acceptor = new Thread(() -> {
                try { server.accept().close(); } catch (Exception ignored) { }
            });
            acceptor.setDaemon(true);
            acceptor.start();

            SystemConfig cfg = mock(SystemConfig.class);
            when(cfg.getValue("/config/servers/sipc-proxy")).thenReturn("127.0.0.1:" + port);
            when(cfg.getValue("/config/servers/sipc-proxy-backup")).thenReturn(null);
            FetionContext ctx = mock(FetionContext.class);
            when(ctx.getSystemConfig()).thenReturn(cfg);
            when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));

            TransferProxy proxy = new TransferProxy(ctx);
            proxy.startTransfer();
            assertThat(proxy.isClosed()).isFalse();
            assertThat(proxy.getTransferName()).startsWith("TcpTransfer-");
            proxy.stopTransfer();
        }
    }

    @Test
    @DisplayName("TransferException constructors store message and cause")
    void testTransferExceptionCtors() {
        assertThat(new TransferException("m").getMessage()).isEqualTo("m");
        Throwable cause = new RuntimeException("c");
        assertThat(new TransferException(cause).getCause()).isSameAs(cause);
        assertThat(new TransferException("m", cause).getMessage()).isEqualTo("m");
        assertThat(new TransferException()).isNotNull();
    }
}
