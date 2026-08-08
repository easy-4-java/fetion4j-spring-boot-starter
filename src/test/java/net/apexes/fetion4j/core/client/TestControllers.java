package net.apexes.fetion4j.core.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.sipc.ResponseMessage;

/**
 * Test helpers for building a {@link Controller} whose package-private
 * {@code submit} returns a canned response. Lives in the {@code client} package
 * so it can invoke the package-private method.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
public final class TestControllers {

    private TestControllers() {
    }

    /** Mocks a Controller and stubs its package-private submit to return the given response. */
    public static Controller controllerReturning(ResponseMessage response) throws Exception {
        Controller controller = mock(Controller.class);
        when(controller.submit(any(), anyLong())).thenReturn(response);
        return controller;
    }

    /** Mocks a Controller whose submit returns null (timeout). */
    public static Controller controllerReturningNull() throws Exception {
        Controller controller = mock(Controller.class);
        when(controller.submit(any(), anyLong())).thenReturn(null);
        return controller;
    }

    /** Builds a stubbed FetionContext exposing the given UserInfo (with a Contact) and an Account. */
    public static FetionContext contextWithUserInfo(UserInfo info) {
        FetionContext ctx = mock(FetionContext.class);
        when(ctx.getUserInfo()).thenReturn(info);
        when(ctx.getLogHandler()).thenReturn(mock(LogHandler.class));
        when(ctx.getAccount()).thenReturn(AccountTestSupport.newAccount());
        when(ctx.getMachineCode()).thenReturn("MACHINE");
        return ctx;
    }

    /** Uses doReturn to stub a Controller submit response (alternative style). */
    public static Controller controllerWithResponse(Controller controller, ResponseMessage response)
            throws Exception {
        doReturn(response).when(controller).submit(any(), anyLong());
        return controller;
    }

    /** Stubs a Controller mock to return the given responses in order across successive submits. */
    public static Controller controllerWithResponses(ResponseMessage... responses) throws Exception {
        Controller controller = mock(Controller.class);
        if (responses.length == 1) {
            when(controller.submit(any(), anyLong())).thenReturn(responses[0]);
        } else {
            when(controller.submit(any(), anyLong())).thenReturn(responses[0],
                    java.util.Arrays.copyOfRange(responses, 1, responses.length));
        }
        return controller;
    }

    /** Stubs createSubAcitivity on the controller to return a new SubActivity bound to the context. */
    public static Controller withSubActivity(Controller controller, FetionContext context)
            throws Exception {
        net.apexes.fetion4j.core.client.activity.SubActivity sub =
                new net.apexes.fetion4j.core.client.activity.SubActivity(context, controller, 2);
        when(controller.createSubAcitivity()).thenReturn(sub);
        return controller;
    }

    /**
     * Stubs submit on a mock controller to pair the first response with its request (so
     * {@code Activity.verify} can read it back), then return the second response on the next call.
     */
    public static Controller controllerWithVerifyFlow(
            ResponseMessage firstResponse, ResponseMessage secondResponse) throws Exception {
        Controller controller = mock(Controller.class);
        org.mockito.Mockito.when(controller.submit(any(), anyLong()))
                .thenAnswer(invocation -> {
                    net.apexes.fetion4j.core.sipc.RequestMessage req = invocation.getArgument(0);
                    firstResponse.setRequestMessage(req);
                    return firstResponse;
                })
                .thenReturn(secondResponse);
        return controller;
    }
}
