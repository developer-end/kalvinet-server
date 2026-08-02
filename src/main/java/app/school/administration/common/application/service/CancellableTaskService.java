package app.school.administration.common.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service demonstrating long-running, interrupt-aware async tasks registered for cancellation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancellableTaskService {

    private final CancellableTaskRegistry cancellableTaskRegistry;

    /**
     * Simulates a long-running batch report generation that checks for thread interrupts at loop boundaries.
     *
     * @param requestId Unique UUID from X-Request-Id header
     * @param taskName  Name of task (e.g., "Full Attendance Analytics Export")
     * @return CompletableFuture with result message
     */
    @Async
    public CompletableFuture<String> executeLongRunningReport(String requestId, String taskName) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Register future in task registry
        cancellableTaskRegistry.register(requestId, future);

        try {
            log.info("Started long-running task [{}] for requestId: {}", taskName, requestId);
            
            // Loop boundaries with interrupt checks
            for (int i = 1; i <= 10; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Task [{}] interrupted at step {} for requestId: {}", taskName, i, requestId);
                    future.cancel(true);
                    return future;
                }

                // Simulate batch chunk processing
                Thread.sleep(1000);
                log.debug("Processed step {}/10 for task [{}] (requestId: {})", i, taskName, requestId);
            }

            String result = "Report [" + taskName + "] generated successfully for request " + requestId;
            future.complete(result);
            return future;
        } catch (InterruptedException e) {
            log.warn("InterruptedException caught during task [{}] execution for requestId: {}. Propagating cancellation.", taskName, requestId);
            Thread.currentThread().interrupt(); // Restore interrupt status
            future.cancel(true);
            return future;
        } finally {
            // Clean up map entry to prevent memory leaks
            cancellableTaskRegistry.remove(requestId);
        }
    }
}
