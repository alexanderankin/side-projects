package info.ankin.projects.tfe4j.client.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@Slf4j
class TerraformClientResponseExceptionTest {

    @Test
    void test() {
        JsonApiErrors jsonApiErrors = new JsonApiErrors().setErrors(List.of(new JsonApiErrors.Error().setStatus("422").setTitle("invalid attribute").setSource(new JsonApiErrors.Error.ErrorSource().setPointer("/data/attributes/username")), new JsonApiErrors.Error().setStatus("422").setTitle("invalid attribute").setSource(new JsonApiErrors.Error.ErrorSource().setPointer("/data/attributes/username"))));


        var e = new TerraformClientResponseException(jsonApiErrors);
        assertThat(e.asPassThrough(), is(instanceOf(ResponseStatusException.class)));
        assertThat(e.asPassThrough().getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
        assertThat(e.asPassThrough().getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
    }

}
