package app.school.administration.common.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Service registry backing cancellable async tasks.
 * Maps unique X-Request-Id UUID headers to running Future<?> task handles.
 */
@Slf4j
@Service
public class CancellableTaskRegistry {

    private final ConcurrentHashMap<String, Future<?>> taskMap = new ConcurrentHashMap<>();

    /**
     * Registers a running task Future under the specified requestId.
     *
     * @param requestId Unique UUID from X-Request-Id header
     * @param future    Task Future handle
     */
    public void register(String requestId, Future<?> future) {
        if (requestId != null && !requestId.isBlank() && future != null) {
            taskMap.put(requestId, future);
            log.info("Registered cancellable task for requestId: {}", requestId);
        }
    }

    /**
     * Cancels a running task by issuing a thread interrupt (future.cancel(true)).
     * Safe no-op if the request is not registered or already completed.
     *
     * @param requestId Unique UUID from X-Request-Id header
     * @return true if task was found and interrupt sent; false otherwise
     */
    public boolean cancel(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return false;
        }
        Future<?> future = taskMap.remove(requestId);
        if (future != null) {
            if (!future.isDone() && !future.isCancelled()) {
                boolean cancelled = future.cancel(true);
                log.info("Cancelled running task for requestId: {} (interrupt sent: {})", requestId, cancelled);
                return cancelled;
            } else {
                log.info("Task for requestId: {} was already completed or cancelled.", requestId);
                return false;
            }
        }
        log.warn("Attempted to cancel task for requestId: {}, but no active task was registered.", requestId);
        return false;
    }

    /**
     * Removes the task entry from the registry without cancelling (called on normal completion or error).
     *
     * @param requestId Unique UUID from X-Request-Id header
     */
    public void remove(String requestId) {
        if (requestId != null) {
            taskMap.remove(requestId);
            log.debug("Removed task entry for requestId: {}", requestId);
        }
    }

    /**
     * Checks if a task is currently registered.
     *
     * @param requestId Unique UUID
     * @return true if registered
     */
    public boolean isRegistered(String requestId) {
        return requestId != null && taskMap.containsKey(requestId);
    }

    /**
     * Gets the total count of currently registered active tasks.
     *
     * @return active task count
     */
    public int getActiveTaskCount() {
        return taskMap.size();
    }
}
