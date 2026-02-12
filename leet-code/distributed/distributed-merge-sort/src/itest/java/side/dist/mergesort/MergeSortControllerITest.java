package side.dist.mergesort;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class MergeSortControllerITest extends DistributedMergeSortITest {
    @Test
    void test() {
        var result = webTestClient.post().uri("/api/sorts")
                .body(Flux.range(0, 200).map(i -> i + "\n"), String.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MergeSortEntity.class)
                .returnResult();
        assertThat(result.getResponseBody(), is(notNullValue()));
        assertThat(result.getResponseBody().getId(), is(notNullValue()));
        System.out.println(result.getResponseBody().getId());

        String originalContents = webTestClient.get().uri("/api/sorts/{id}/input", result.getResponseBody().getId()).exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(notNullValue())
                .returnResult().getResponseBody();
        assertThat(originalContents, is(notNullValue()));
        assertThat(originalContents, startsWith("0\n1\n2\n3\n"));
        assertThat(originalContents, endsWith("197\n198\n199\n"));
    }
}
