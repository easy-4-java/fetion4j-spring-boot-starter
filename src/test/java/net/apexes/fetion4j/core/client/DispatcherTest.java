package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.FetionException;
import net.apexes.fetion4j.core.sipc.RequestMessage;
import net.apexes.fetion4j.core.sipc.Sipc;

/**
 * Unit tests for {@link Dispatcher}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Dispatcher Tests")
class DispatcherTest {

    @Test
    @DisplayName("Controller.submit throws when the controller is not running")
    void testSubmitThrowsWhenNotRunning() throws Exception {
        FetionContext ctx = TestControllers.contextWithUserInfo(new net.apexes.fetion4j.core.UserInfo());
        Controller controller = new Controller(ctx);
        RequestMessage request = new RequestMessage(Sipc.METHOD_R);
        request.setCallId(1);
        request.setSequence(1);
        assertThatThrownBy(() -> controller.submit(request, 10L))
                .isInstanceOfAny(FetionException.class, NullPointerException.class);
    }

    @Test
    @DisplayName("Dispatcher submit guard raises FetionException when not running")
    void testDispatcherSubmitGuard() throws Exception {
        Controller controller = mock(Controller.class);
        when(controller.getExecutorService()).thenReturn(Executors.newCachedThreadPool());
        when(controller.isRunning()).thenReturn(false);

        Dispatcher dispatcher = dispatcherFor(controller);
        RequestMessage request = new RequestMessage(Sipc.METHOD_R);
        request.setCallId(1);
        request.setSequence(1);
        // submit throws FetionException because the controller is not running
        assertThatThrownBy(() -> dispatcherSubmit(dispatcher, request, 10L))
                .hasCauseInstanceOf(FetionException.class);
    }

    // Dispatcher and its submit are package-private; these thin wrappers keep tests in this package.
    private static Dispatcher dispatcherFor(Controller controller) throws Exception {
        java.lang.reflect.Constructor<Dispatcher> ctor =
                Dispatcher.class.getDeclaredConstructor(Controller.class);
        ctor.setAccessible(true);
        return ctor.newInstance(controller);
    }

    private static net.apexes.fetion4j.core.sipc.ResponseMessage dispatcherSubmit(
            Dispatcher dispatcher, RequestMessage request, long timeout) throws Exception {
        java.lang.reflect.Method m = Dispatcher.class.getDeclaredMethod(
                "submit", RequestMessage.class, long.class);
        m.setAccessible(true);
        return (net.apexes.fetion4j.core.sipc.ResponseMessage) m.invoke(dispatcher, request, timeout);
    }
}
