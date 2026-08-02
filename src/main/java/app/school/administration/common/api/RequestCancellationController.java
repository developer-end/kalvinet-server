package app.school.administration.common.api;

import app.school.administration.common.application.service.CancellableTaskRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller exposing DELETE /api/requests/{requestId}/cancel endpoint for dual-layer request cancellation.
 */
@Slf4j
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestCancellationController {

    private final CancellableTaskRegistry cancellableTaskRegistry;

    /**
     * Cancels a running backend task correlated by the unique request ID.
     *
     * @param requestId Unique UUID from X-Request-Id header
     * @return ResponseEntity with cancellation status
     */
    @DeleteMapping("/{requestId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelRequest(@PathVariable("requestId") String requestId) {
        log.info("Received cancellation request for requestId: {}", requestId);
        boolean cancelled = cancellableTaskRegistry.cancel(requestId);

        return ResponseEntity.ok(Map.of(
                "requestId", requestId,
                "cancelled", cancelled,
                "message", cancelled ? "Server-side task interrupted successfully." : "Task already completed, cancelled, or not found."
        ));
    }
}
