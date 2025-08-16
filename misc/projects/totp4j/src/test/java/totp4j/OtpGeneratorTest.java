package totp4j;

import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * yitsushi/totp-cli/internal/security/otp_test.go
 */
class OtpGeneratorTest {

    OtpGenerator otp = new OtpGenerator();

    @ParameterizedTest
    @CsvSource({
            "1970, 1, 1, 0, 0, 59, 0, 007459",
            "2005, 3, 18, 1, 58, 29, 0, 227921",
            "2005, 3, 18, 1, 58, 31, 0, 638051",
            "2009, 2, 13, 23, 31, 30, 0, 144100",
            "2016, 9, 16, 12, 40, 12, 0, 346566",
            "2033, 5, 18, 3, 33, 20, 0, 810915",
            "2603, 10, 11, 11, 33, 20, 0, 041334",
    })
    void generateOTPCode(int y, int m, int d, int hour, int min, int s, int ns, String expected) {
        String input = new Base32().encodeToString("82394783472398472348".getBytes(StandardCharsets.UTF_8));
        Instant when = LocalDateTime.of(y, m, d, hour, min, s, ns).toInstant(UTC);
        assertEquals(expected, otp.generate(input, when, 6).getCode());
    }

    @ParameterizedTest
    @CsvSource({
            "1970, 1, 1, 0, 0, 59, 0, 53007459",
            "2005, 3, 18, 1, 58, 29, 0, 97227921",
            "2005, 3, 18, 1, 58, 31, 0, 89638051",
            "2009, 2, 13, 23, 31, 30, 0, 49144100",
            "2016, 9, 16, 12, 40, 12, 0, 13346566",
            "2033, 5, 18, 3, 33, 20, 0, 44810915",
            "2603, 10, 11, 11, 33, 20, 0, 28041334",
    })
    void generateOTPCode_length8(int y, int m, int d, int hour, int min, int s, int ns, String expected) {
        String input = new Base32().encodeToString("82394783472398472348".getBytes(StandardCharsets.UTF_8));
        Instant when = LocalDateTime.of(y, m, d, hour, min, s, ns).toInstant(UTC);
        assertEquals(expected, otp.generate(input, when, 8).getCode());
    }

    @ParameterizedTest
    @CsvSource({
            "1970, 1, 1, 0, 0, 59, 0, 066634",
            "2005, 3, 18, 1, 58, 29, 0, 597310",
            "2005, 3, 18, 1, 58, 31, 0, 174182",
            "2009, 2, 13, 23, 31, 30, 0, 623746",
            "2016, 9, 16, 12, 40, 12, 0, 330739",
            "2033, 5, 18, 3, 33, 20, 0, 556617",
            "2603, 10, 11, 11, 33, 20, 0, 608345",
    })
    void generateOTPCode_SpaceSeparatedToken(int y, int m, int d, int hour, int min, int s, int ns, String expected) {
        String input = "37kh vdxt c5hj ttfp ujok cipy jy";
        Instant when = LocalDateTime.of(y, m, d, hour, min, s, ns).toInstant(UTC);
        assertEquals(expected, otp.generate(input, when, 6).getCode());
    }

    @ParameterizedTest
    @CsvSource({
            "1970, 1, 1, 0, 0, 59, 0, 866149",
            "2005, 3, 18, 1, 58, 29, 0, 996077",
            "2005, 3, 18, 1, 58, 31, 0, 421761",
            "2009, 2, 13, 23, 31, 30, 0, 903464",
            "2016, 9, 16, 12, 40, 12, 0, 997249",
            "2033, 5, 18, 3, 33, 20, 0, 210476",
            "2603, 10, 11, 11, 33, 20, 0, 189144",
    })
    void generateOTPCode_NonPaddedHashes(int y, int m, int d, int hour, int min, int s, int ns, String expected) {
        String input = "a6mryljlbufszudtjdt42nh5by";
        Instant when = LocalDateTime.of(y, m, d, hour, min, s, ns).toInstant(UTC);
        assertEquals(expected, otp.generate(input, when, 6).getCode());
    }

    @Disabled("decide how to handle go err")
    @ParameterizedTest
    @CsvSource({
            "1970, 1, 1, 0, 0, 59, 0,''",
            "2005, 3, 18, 1, 58, 29, 0,''",
    })
    void generateOTPCode_InvalidPadding(int y, int m, int d, int hour, int min, int s, int ns, String expected) {
        String input = "a6mr*&^&*%*&ylj|'[lbufszudtjdt42nh5by";
    }
}
