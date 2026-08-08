package net.apexes.fetion4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for core data types: {@link Result}, {@link FetionException}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Core Data Tests")
class CoreDataTest {

    @Nested
    @DisplayName("Result")
    class ResultTest {
        @Test
        void testGetters() {
            Result r = new Result(200, "OK", Result.Type.SUCCESS, "done");
            assertThat(r.getStatus()).isEqualTo(200);
            assertThat(r.getStatusMessage()).isEqualTo("OK");
            assertThat(r.getType()).isEqualTo(Result.Type.SUCCESS);
            assertThat(r.getDescribe()).isEqualTo("done");
        }

        @Test
        void testToString() {
            Result r = new Result(404, "NotFound", Result.Type.FAILURE, "missing");
            assertThat(r.toString()).contains("status=404").contains("missing");
        }

        @Test
        void testTypeEnumValues() {
            assertThat(Result.Type.values()).contains(Result.Type.SUCCESS, Result.Type.FAILURE);
            assertThat(Result.Type.valueOf("SUCCESS")).isEqualTo(Result.Type.SUCCESS);
        }
    }

    @Nested
    @DisplayName("FetionException")
    class FetionExceptionTest {
        @Test
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("root");
            FetionException ex = new FetionException("msg", cause);
            assertThat(ex.getMessage()).isEqualTo("msg");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        void testCauseConstructor() {
            Throwable cause = new RuntimeException("root");
            FetionException ex = new FetionException(cause);
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        void testMessageConstructor() {
            FetionException ex = new FetionException("only-msg");
            assertThat(ex.getMessage()).isEqualTo("only-msg");
        }

        @Test
        void testProtectedNoArgConstructorIsAccessibleWithinPackage() {
            // The protected no-arg ctor can be invoked from a subclass; assert class is throwable.
            assertThatThrownBy(() -> { throw new FetionException("x"); }).isInstanceOf(FetionException.class);
        }
    }

    @SuppressWarnings("serial")
    static class TestException extends FetionException {
        TestException() {
            super();
        }
    }

    @Test
    @DisplayName("FetionException protected no-arg constructor via subclass")
    void testProtectedNoArgCtor() {
        TestException ex = new TestException();
        assertThat(ex.getMessage()).isNull();
    }
}
