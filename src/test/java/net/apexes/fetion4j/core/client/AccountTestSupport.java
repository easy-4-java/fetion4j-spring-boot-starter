package net.apexes.fetion4j.core.client;

import net.apexes.fetion4j.core.Account;

/**
 * Test helper that builds an {@link Account} via reflection (its constructor is package-private).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
final class AccountTestSupport {

    private AccountTestSupport() {
    }

    static Account newAccount() {
        try {
            java.lang.reflect.Constructor<Account> ctor =
                    Account.class.getDeclaredConstructor(long.class, int.class, String.class);
            ctor.setAccessible(true);
            Account acc = ctor.newInstance(13900000000L, 12345, "sip:12345@fetion.com.cn;p=1");
            acc.setPassword("123456");
            return acc;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
