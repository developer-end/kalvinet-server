package app.school.administration.common.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class CancellableTaskRegistryTest {

    private CancellableTaskRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CancellableTaskRegistry();
    }

    @Test
    @DisplayName("Pre-completion cancel should interrupt task thread and remove registry entry")
    void testCancelBeforeCompletionInterruptsTask() {
        String requestId = "req-uuid-101";
        CompletableFuture<String> future = new CompletableFuture<>();

        registry.register(requestId, future);
        assertTrue(registry.isRegistered(requestId), "Task should be registered");

        boolean cancelled = registry.cancel(requestId);
        assertTrue(cancelled, "Registry cancel should return true for active task");
        assertTrue(future.isCancelled(), "Future should be marked cancelled");
        assertFalse(registry.isRegistered(requestId), "Registry map should no longer contain requestId (no memory leaks)");
    }

    @Test
    @DisplayName("Post-completion cancel should be a safe no-op")
    void testCancelAfterCompletionIsSafeNoOp() {
        String requestId = "req-uuid-102";
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("SUCCESS_RESULT");

        registry.register(requestId, future);
        boolean cancelled = registry.cancel(requestId);

        assertFalse(cancelled, "Cancelling completed task should return false");
        assertFalse(future.isCancelled(), "Completed future should remain uncancelled");
        assertFalse(registry.isRegistered(requestId), "Registry map should be cleaned up");
    }

    @Test
    @DisplayName("Task registry should not leak entries on normal completion cleanup")
    void testRegistryCleanupOnNormalCompletion() {
        String requestId = "req-uuid-103";
        CompletableFuture<String> future = new CompletableFuture<>();

        registry.register(requestId, future);
        assertEquals(1, registry.getActiveTaskCount());

        registry.remove(requestId);
        assertEquals(0, registry.getActiveTaskCount(), "Registry task count should be 0 after remove");
    }
}
