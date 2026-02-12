package side.dist.mergesort;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sorts")
class MergeSortController {
    private final MergeSortService mergeSortService;

    @PostMapping(consumes = {"text/plain","application/*+json"})
    @ResponseStatus(HttpStatus.CREATED)
    MergeSortEntity create(@NonNull InputStream body) {
        return mergeSortService.createMergeSort(body);
    }

    @GetMapping("/{id}/input")
    StreamingResponseBody getInput(@PathVariable int id) {
        return mergeSortService.getMergeSortInput(id);
    }
}
