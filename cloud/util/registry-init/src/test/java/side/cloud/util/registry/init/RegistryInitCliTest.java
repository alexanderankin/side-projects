package side.cloud.util.registry.init;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class RegistryInitCliTest {

    @Test
    void bcrypt() {
        String bcryptHash = new HtpasswdCrud(new SecureRandom()).bcrypt("password");
        assertThat(bcryptHash.charAt(0), is(equalTo('$')));
        assertThat(bcryptHash.charAt(3), is(equalTo('$')));
        assertThat(bcryptHash.charAt(6), is(equalTo('$')));
        assertThat(bcryptHash.substring(4, 6), is(equalTo("10")));
        assertThat(bcryptHash.length(), is(equalTo(60)));
    }
}
