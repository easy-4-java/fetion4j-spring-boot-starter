package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.user.Buddy;
import net.apexes.fetion4j.core.user.Relation;
import net.apexes.fetion4j.core.user.User;

/**
 * Unit tests for {@link SimpleProviderFactory}, {@link SimpleProvider}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("SimpleProvider Tests")
class SimpleProviderTest {

    private FetionContext contextWith(LogHandler handler, UserInfo userInfo) {
        FetionContext ctx = mock(FetionContext.class);
        lenient().when(ctx.getLogHandler()).thenReturn(handler);
        lenient().when(ctx.getUserInfo()).thenReturn(userInfo);
        return ctx;
    }

    @Test
    @DisplayName("SimpleProviderFactory.create returns a SimpleProvider bound to the mobile number")
    void testFactoryCreate() {
        FetionContext ctx = contextWith(mock(LogHandler.class), new UserInfo());
        SimpleProviderFactory factory = new SimpleProviderFactory(ctx);
        assertThat(factory.create(13900000000L)).isInstanceOf(SimpleProvider.class);
    }

    @Test
    @DisplayName("SimpleProvider notifies log handler for each NotifyListener event")
    void testNotifyEvents() {
        LogHandler handler = mock(LogHandler.class);
        FetionContext ctx = contextWith(handler, new UserInfo());
        SimpleProvider provider = new SimpleProvider(ctx, 13900000000L);

        provider.transfeError("err", new RuntimeException("e"));
        provider.changedUser(new User(1));
        Buddy b = new Buddy(2, "u", "n", Relation.BUDDY);
        provider.changedBuddy(b, "v");
        provider.addedBuddy(b, "v");
        provider.deletedBuddy(b, "v");
        provider.smsCountChanged(1, 2);

        verify(handler).error(eq(SimpleProvider.class), eq("err"), any());
        verify(handler).debug(eq(SimpleProvider.class), org.mockito.ArgumentMatchers.contains("更新联系人"));
    }

    @Test
    @DisplayName("SimpleProvider.readSystemConfig returns null when the file is absent")
    void testReadSystemConfigMissing() {
        FetionContext ctx = contextWith(mock(LogHandler.class), new UserInfo());
        SimpleProvider provider = new SimpleProvider(ctx, 7654321L);
        assertThat(provider.readSystemConfig()).isNull();
    }

    @Test
    @DisplayName("SimpleProvider.readUserInfo returns null when the file is absent")
    void testReadUserInfoMissing() {
        FetionContext ctx = contextWith(mock(LogHandler.class), new UserInfo());
        SimpleProvider provider = new SimpleProvider(ctx, 1111111L);
        assertThat(provider.readUserInfo()).isNull();
    }

    @Test
    @DisplayName("SimpleProvider.createdAccount is a no-op")
    void testCreatedAccountNoOp() {
        FetionContext ctx = contextWith(mock(LogHandler.class), new UserInfo());
        SimpleProvider provider = new SimpleProvider(ctx, 2222222L);
        provider.createdAccount(null); // no exception expected
    }
}
