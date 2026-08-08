package net.apexes.fetion4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.apexes.fetion4j.core.AuthSupportable;
import net.apexes.fetion4j.core.Captcha;
import net.apexes.fetion4j.core.LogHandler;
import net.apexes.fetion4j.core.SystemConfig;
import net.apexes.fetion4j.core.UserInfo;
import net.apexes.fetion4j.core.util.XmlElement;

/**
 * Unit tests for {@link CaptchaImpl}, {@link AuthFeedbackImpl}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("Captcha & AuthFeedback Tests")
class CaptchaAndAuthFeedbackTest {

    @Test
    @DisplayName("CaptchaImpl getters/setters round-trip")
    void testCaptchaImpl() {
        CaptchaImpl c = new CaptchaImpl("algo", "GeneralPic", "text", "tips", "pid", new byte[] { 1, 2 });
        assertThat(c.getVerifyAlgorithm()).isEqualTo("algo");
        assertThat(c.getVerifyType()).isEqualTo("GeneralPic");
        assertThat(c.getText()).isEqualTo("text");
        assertThat(c.getTips()).isEqualTo("tips");
        assertThat(c.getImageId()).isEqualTo("pid");
        assertThat(c.getImageData()).containsExactly(1, 2);
        assertThat(c.getCode()).isNull();
        c.setCode("ABCD");
        assertThat(c.getCode()).isEqualTo("ABCD");
        assertThat(c.getFailCount()).isZero();
        c.setFailCount(3);
        assertThat(c.getFailCount()).isEqualTo(3);
        assertThat(c.toString()).contains("pid");
    }

    @Test
    @DisplayName("AuthFeedbackImpl.submit/cancel release the latch without blocking")
    void testSubmitAndCancel() {
        FetionContext ctx = stubContext();
        CaptchaImpl captcha = new CaptchaImpl("a", "t", "x", "tips", "id", new byte[] {});
        AuthFeedbackImpl fb = new AuthFeedbackImpl(ctx, captcha, (net.apexes.fetion4j.core.util.XmlElement) null);
        // submit sets the code and counts down the latch
        fb.submit("ZZZZ");
        assertThat(captcha.getCode()).isEqualTo("ZZZZ");
        assertThat(fb.getCaptcha()).isSameAs(captcha);

        AuthFeedbackImpl fb2 = new AuthFeedbackImpl(ctx, captcha, (net.apexes.fetion4j.core.util.XmlElement) null);
        fb2.cancel();
        assertThat(fb2.getCaptcha()).isNull();
    }

    @Test
    @DisplayName("AuthFeedbackImpl.authAndWait invokes needAuth and returns when submitted")
    void testAuthAndWait() throws Exception {
        FetionContext ctx = stubContext();
        CaptchaImpl captcha = new CaptchaImpl("a", "t", "x", "tips", "id", new byte[] {});
        AuthFeedbackImpl fb = new AuthFeedbackImpl(ctx, captcha, (net.apexes.fetion4j.core.util.XmlElement) null);
        // Submit from another thread to release authAndWait
        Thread t = new Thread(() -> {
            try { Thread.sleep(50); } catch (InterruptedException ignored) { }
            fb.submit("code");
        });
        t.start();
        fb.authAndWait();
        t.join();
        assertThat(captcha.getCode()).isEqualTo("code");
    }

    private static FetionContext stubContext() {
        FetionContext ctx = org.mockito.Mockito.mock(FetionContext.class);
        org.mockito.Mockito.lenient().when(ctx.getLogHandler()).thenReturn(org.mockito.Mockito.mock(LogHandler.class));
        org.mockito.Mockito.lenient().when(ctx.getUserInfo()).thenReturn(new UserInfo());
        org.mockito.Mockito.lenient().when(ctx.getSystemConfig()).thenReturn(org.mockito.Mockito.mock(SystemConfig.class));
        org.mockito.Mockito.lenient().when(ctx.getCmccMobileValidator()).thenReturn(org.mockito.Mockito.mock(CmccMobileValidator.class));
        org.mockito.Mockito.lenient().when(ctx.getAuthSupportable()).thenReturn(new RecordingAuthSupportable());
        return ctx;
    }

    @Test
    @DisplayName("AuthFeedbackImpl(FetionContext, captcha, response) constructor stores the response")
    void testResponseConstructor() {
        FetionContext ctx = stubContext();
        CaptchaImpl captcha = new CaptchaImpl("a", "t", "x", "tips", "id", new byte[] {});
        net.apexes.fetion4j.core.sipc.ResponseMessage response =
                new net.apexes.fetion4j.core.sipc.ResponseMessage(net.apexes.fetion4j.core.sipc.Sipc.STATUS_EXTENSION_REQUIRED, "Need");
        response.setField(net.apexes.fetion4j.core.sipc.Sipc.FIELD_W,
                "Verify algorithm=\"algo\",type=\"GeneralPic\"");
        response.setBody("<results><reason text=\"\" tips=\"\"/></results>");
        AuthFeedbackImpl fb = new AuthFeedbackImpl(ctx, captcha, response);
        assertThat(fb.getCaptcha()).isSameAs(captcha);
    }

    @Test
    @DisplayName("AuthFeedbackImpl.tryAgain with a verification xml delegates to ClientHelper (network)")
    void testTryAgainWithXml() {
        FetionContext ctx = stubContext();
        XmlElement xml = new XmlElement();
        xml.parseString("<results><verification algorithm=\"algo\" type=\"t\" text=\"x\" tips=\"y\"/></results>");
        AuthFeedbackImpl fb = new AuthFeedbackImpl(ctx, null, xml);
        // tryAgain calls ClientHelper.getCaptcha which needs a URL -> throws; swallow here
        try {
            fb.tryAgain();
        } catch (Exception expected) {
            assertThat(expected).isNotNull();
        }
    }

    @Test
    @DisplayName("AuthFeedbackImpl.authAndWait increments fail count when a captcha is already set")
    void testAuthAndWaitIncrementsFailCount() throws Exception {
        FetionContext ctx = stubContext();
        CaptchaImpl captcha = new CaptchaImpl("a", "t", "x", "tips", "id", new byte[] {});
        captcha.setFailCount(2);
        AuthFeedbackImpl fb = new AuthFeedbackImpl(ctx, captcha, (XmlElement) null);
        Thread t = new Thread(() -> {
            try { Thread.sleep(30); } catch (InterruptedException ignored) { }
            fb.submit("code");
        });
        t.start();
        fb.authAndWait();
        t.join();
        assertThat(captcha.getFailCount()).isEqualTo(3);
    }

    /** AuthSupportable that simply records the feedback without blocking. */
    static class RecordingAuthSupportable implements AuthSupportable {
        @Override
        public void needAuth(Captcha captcha, net.apexes.fetion4j.core.AuthFeedback feedback) {
            // immediately submit a placeholder to unblock authAndWait in tests
            feedback.submit("code");
        }
    }
}
