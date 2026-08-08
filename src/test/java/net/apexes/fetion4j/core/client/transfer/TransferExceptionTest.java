package net.apexes.fetion4j.core.client.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TransferException}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("TransferException Tests")
class TransferExceptionTest {

    @Test
    @DisplayName("Constructor (Throwable) stores the cause")
    void testCauseConstructor() {
        Throwable cause = new java.io.IOException("io");
        TransferException ex = new TransferException(cause);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("Constructor (String) stores the message")
    void testMessageConstructor() {
        TransferException ex = new TransferException("msg");
        assertThat(ex.getMessage()).isEqualTo("msg");
    }

    @Test
    @DisplayName("Constructor (String, Throwable) stores both")
    void testMessageAndCauseConstructor() {
        Throwable cause = new RuntimeException("r");
        TransferException ex = new TransferException("msg", cause);
        assertThat(ex.getMessage()).isEqualTo("msg");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("No-arg constructor creates an instance")
    void testNoArgConstructor() {
        assertThat(new TransferException()).isNotNull();
    }
}
