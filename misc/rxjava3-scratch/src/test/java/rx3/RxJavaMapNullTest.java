package rx3;

import io.reactivex.rxjava3.core.Maybe;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RxJavaMapNullTest {

    @Test
    void test() {
        assertThrows(CompletionException.class, () -> {
            Object o = Maybe.fromObservable(new RxJavaMapNull().test(Optional.empty()))
                    .toCompletionStage()
                    .toCompletableFuture().join();
    
            System.out.println(o);
        });
    }
}
