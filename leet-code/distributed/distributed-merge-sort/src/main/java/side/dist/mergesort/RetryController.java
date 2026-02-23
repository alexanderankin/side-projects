package side.dist.mergesort;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/retries")
@RequiredArgsConstructor
class RetryController {
    private final QueueService queueService;

    // Retry single failed job
    @PostMapping("/{id}")
    public QueueService.QueueItem retryOne(@PathVariable int id) {
        return queueService.retryFailed(id);
    }

    // Retry all failed jobs
    @PostMapping
    public int retryAll() {
        return queueService.retryAllFailed();
    }

    // Cursor list failed
    @GetMapping("/failed")
    public Slice<QueueService.QueueItem> listFailed(
            @RequestParam(required = false) Integer last,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return queueService.listFailed(Pageable.ofSize(limit), last);
    }
}
