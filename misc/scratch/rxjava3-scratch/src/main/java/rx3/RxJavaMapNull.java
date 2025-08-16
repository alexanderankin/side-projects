package rx3;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;

import java.util.Objects;
import java.util.Optional;

public class RxJavaMapNull {
    @NonNull
    <T> Observable<T> test(Optional<T> optional) {
        return Observable.just(optional)
                .map(o -> o.orElse(null))
                .filter(Objects::nonNull);
    }
}
