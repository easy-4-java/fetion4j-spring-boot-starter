package net.apexes.fetion4j.core.client.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.interfaces.RSAPublicKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the auth package ({@link PasswordEncrypterV4},
 * {@link AuthDigest}, {@link AuthGeneratorV4}).
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Auth Tests")
class AuthTest {

    // 256-hex-char (128 byte) modulus + 4-hex-char exponent => valid public key string
    private static final String PK_MODULUS =
            "B4621B7F3459D8345CD228A62F1BA50DD7C04F2AA0CEE6EAFC63077F4CC3C707"
          + "AF9E28AD3CE2ABC1614D363EEA88965DB92B0D5B4D7312E9729ED215F9783328C"
          + "C0A12FB1B1C874B970672C9963EAFD4BFE5D0F876EABE539AAA158D041CD0E4B8"
          + "535496CFE98B8E8102452E2768716613BC18249F4E4C5DEE1E62C3996BCFC7010";
    private static final String PK = PK_MODULUS + "010001"; // exponent 0x010001
    private static final String NONCE = "1DBA40C5337F33821E5B4C2C168A34CB";

    @Test
    @DisplayName("PasswordEncrypterV4.encryptV4(userid, pass) returns a 40-char hex SHA1")
    void testEncryptV4WithUserid() {
        String hex = PasswordEncrypterV4.encryptV4(472371591, "123456");
        assertThat(hex).hasSize(40).matches("[0-9A-Fa-f]{40}");
    }

    @Test
    @DisplayName("PasswordEncrypterV4.encryptV4(pass) is deterministic")
    void testEncryptV4PassDeterministic() {
        String a = PasswordEncrypterV4.encryptV4("123456");
        String b = PasswordEncrypterV4.encryptV4("123456");
        assertThat(a).isEqualTo(b).hasSize(40);
    }

    @Test
    @DisplayName("PasswordEncrypterV4.encryptV4Temp returns a 40-char hex value")
    void testEncryptV4Temp() {
        String digest = PasswordEncrypterV4.encryptV4("123456");
        String tmp = PasswordEncrypterV4.encryptV4Temp(1, digest);
        assertThat(tmp).hasSize(40).matches("[0-9A-Fa-f]{40}");
    }

    @Test
    @DisplayName("AuthGeneratorV4.getCnonce returns a 32-char hex string")
    void testGetCnonce() {
        String cnonce = AuthGeneratorV4.getCnonce();
        assertThat(cnonce).hasSize(32).matches("[0-9A-Fa-f]{32}");
    }

    @Test
    @DisplayName("parsePublicKey returns an RSA public key for a well-formed key string")
    void testParsePublicKey() throws Exception {
        AuthGeneratorV4 gen = new AuthGeneratorV4();
        RSAPublicKey key = gen.parsePublicKey(PK);
        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    @DisplayName("generate returns a hex-encoded encrypted blob")
    void testGenerate() {
        AuthGeneratorV4 gen = new AuthGeneratorV4();
        String passHex = PasswordEncrypterV4.encryptV4(1, "pass");
        String aeskey = "16124BA186BE0868BD9215D60A4A4DD6A8F910AB601E0B4E0126DCEA26C41B6F";
        String result = gen.generate(PK, passHex, NONCE, aeskey);
        assertThat(result).matches("[0-9A-Fa-f]+");
    }

    @Test
    @DisplayName("AuthDigest.parse round-trips algorithm/nonce/key/signature")
    void testAuthDigestParse() {
        String digest = "Digest algorithm=\"SHA1-sess-v4\",nonce=\"1D3C\",key=\"C3C7\",signature=\"84E8\"";
        AuthDigest ad = AuthDigest.parse(digest);
        assertThat(ad.getAlgorithm()).isEqualTo("SHA1-sess-v4");
        assertThat(ad.getNonce()).isEqualTo("1D3C");
        assertThat(ad.getKey()).isEqualTo("C3C7");
        assertThat(ad.getSignature()).isEqualTo("84E8");
        assertThat(ad.getResponse()).isNull();
    }

    @Test
    @DisplayName("AuthDigest.toString without response lists all four fields")
    void testAuthDigestToStringNoResponse() {
        AuthDigest ad = AuthDigest.parse(
                "Digest algorithm=\"SHA1-sess-v4\",nonce=\"1D3C\",key=\"C3C7\",signature=\"84E8\"");
        assertThat(ad.toString())
                .startsWith("Digest ")
                .contains("algorithm=\"SHA1-sess-v4\"")
                .contains("nonce=\"1D3C\"")
                .contains("key=\"C3C7\"")
                .contains("signature=\"84E8\"");
    }

    @Test
    @DisplayName("AuthDigest.generateResponse populates response using a valid RSA public key")
    void testGenerateResponseWithValidKey() {
        AuthDigest ad = AuthDigest.parse(
                "Digest algorithm=\"SHA1-sess-v4\",nonce=\"1D3C\",key=\"" + PK + "\",signature=\"84E8\"");
        ad.generateResponse(buildAccount());
        assertThat(ad.getResponse()).isNotBlank();
        assertThat(ad.toString())
                .startsWith("Digest ")
                .contains("response=\"");
    }

    private static net.apexes.fetion4j.core.Account buildAccount() {
        // Account's constructor is package-private; build via reflection from the auth package.
        try {
            java.lang.reflect.Constructor<net.apexes.fetion4j.core.Account> ctor =
                    net.apexes.fetion4j.core.Account.class.getDeclaredConstructor(
                            long.class, int.class, String.class);
            ctor.setAccessible(true);
            net.apexes.fetion4j.core.Account acc =
                    ctor.newInstance(13900000000L, 12345, "sip:12345@fetion.com.cn;p=1");
            acc.setPassword("123456");
            return acc;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("AuthGeneratorV4.encrypt returns null for a null public key")
    void testEncryptNullKey() {
        AuthGeneratorV4 gen = new AuthGeneratorV4();
        assertThat(gen.encrypt(null, new byte[] { 1, 2 })).isNull();
    }

    @Test
    @DisplayName("AuthGeneratorV4.decrypt returns null for a null private key")
    void testDecryptNullKey() {
        AuthGeneratorV4 gen = new AuthGeneratorV4();
        assertThat(gen.decrypt(null, new byte[] { 1, 2 })).isNull();
    }
}
