package app.school.administration.dashboard.api.response;

public record DashboardConfigDTO(
        String role,
        String tenantId,
        double attendancePercentage,
        double pendingFeesAmount,
        long activeStudentsCount,
        long totalFacultyCount,
        double mrrAmount,
        String message
) {
}
