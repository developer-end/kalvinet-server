package app.school.administration.modules.featureconfig.application.service;

import app.school.administration.auth.application.constant.OrgAccessPolicy;
import app.school.administration.auth.application.constant.RoleAssignmentPolicy;
import app.school.administration.auth.domain.model.CustomUserDetails;
import app.school.administration.common.application.custom.exception.NoDataFoundException;
import app.school.administration.modules.featureconfig.api.request.UpsertFeatureConfigRequestDTO;
import app.school.administration.modules.featureconfig.api.response.FeatureConfigDTO;
import app.school.administration.modules.featureconfig.persistance.entity.FeatureConfigurationEntity;
import app.school.administration.modules.featureconfig.persistance.entity.FeatureDefinitionEntity;
import app.school.administration.modules.featureconfig.persistance.entity.FeatureRoleGrantEntity;
import app.school.administration.modules.featureconfig.persistance.repository.FeatureConfigurationRepository;
import app.school.administration.modules.featureconfig.persistance.repository.FeatureDefinitionRepository;
import app.school.administration.modules.featureconfig.persistance.repository.FeatureRoleGrantRepository;
import app.school.administration.modules.institution.persistance.entity.InstitutionEntity;
import app.school.administration.modules.institution.persistance.repository.InstitutionRepository;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import app.school.administration.modules.school.persistance.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeatureAccessService {

    public static final String SCOPE_INSTITUTION = "INSTITUTION";
    public static final String SCOPE_SCHOOL = "SCHOOL";

    private final FeatureDefinitionRepository featureDefinitionRepository;
    private final FeatureConfigurationRepository featureConfigurationRepository;
    private final FeatureRoleGrantRepository featureRoleGrantRepository;
    private final InstitutionRepository institutionRepository;
    private final SchoolRepository schoolRepository;

    public CustomUserDetails requireActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new AccessDeniedException("Authenticated user required");
        }
        return details;
    }

    public Set<String> callerRoles() {
        return requireActor().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public InstitutionEntity requireSingletonInstitution() {
        return institutionRepository.findBySingletonKey((short) 1)
                .orElseThrow(() -> new NoDataFoundException("Institution not found"));
    }

    /**
     * Baseline role check, then if a configuration exists for the feature/scope,
     * only granted roles may proceed (and only when enabled).
     * <p>
     * {@code ROLE_OWNER} and {@code ROLE_IT} always pass for org features (institution/school
     * settings, events, schools list, and feature configuration) — platform admins are not
     * locked out by feature grants.
     */
    public void assertFeatureAccess(String featureCode, String scope, UUID schoolId) {
        Set<String> roles = callerRoles();
        if (isPlatformOrgAdmin(roles) && isOrgFeature(featureCode)) {
            return;
        }

        Set<String> baseline = OrgAccessPolicy.baselineRolesFor(featureCode);
        OrgAccessPolicy.assertAllows(roles, baseline, "use " + featureCode);

        InstitutionEntity institution = requireSingletonInstitution();
        Optional<FeatureConfigurationEntity> config = findConfig(featureCode, scope, institution.getId(), schoolId);
        if (config.isEmpty()) {
            return;
        }

        FeatureConfigurationEntity cfg = config.get();
        if (!cfg.isEnabled()) {
            throw new AccessDeniedException("Feature " + featureCode + " is disabled for this scope");
        }

        List<FeatureRoleGrantEntity> grants = featureRoleGrantRepository.findByConfigIdAndActiveTrue(cfg.getId());
        if (grants.isEmpty()) {
            throw new AccessDeniedException("Feature " + featureCode + " has no role grants configured");
        }

        Set<String> granted = grants.stream()
                .map(g -> RoleAssignmentPolicy.normalize(g.getRoleCode()))
                .collect(Collectors.toSet());
        if (!OrgAccessPolicy.allows(roles, granted)) {
            throw new AccessDeniedException("Your role is not granted for " + featureCode);
        }
    }

    private static boolean isPlatformOrgAdmin(Set<String> roles) {
        return OrgAccessPolicy.allows(
                roles,
                Set.of(RoleAssignmentPolicy.ROLE_OWNER, RoleAssignmentPolicy.ROLE_IT)
        );
    }

    private static boolean isOrgFeature(String featureCode) {
        if (featureCode == null) {
            return false;
        }
        String code = featureCode.toUpperCase(Locale.ROOT);
        return OrgAccessPolicy.FEATURE_INSTITUTION_SETTINGS.equals(code)
                || OrgAccessPolicy.FEATURE_INSTITUTION_EVENTS.equals(code)
                || OrgAccessPolicy.FEATURE_SCHOOLS_LIST.equals(code)
                || OrgAccessPolicy.FEATURE_SCHOOL_SETTINGS.equals(code)
                || OrgAccessPolicy.FEATURE_SCHOOL_EVENTS.equals(code)
                || OrgAccessPolicy.FEATURE_CONFIGURATION.equals(code);
    }

    @Transactional(readOnly = true)
    public List<FeatureConfigDTO> listForScope(String scope, UUID schoolId) {
        assertFeatureAccess(OrgAccessPolicy.FEATURE_CONFIGURATION, scopeForConfigManage(scope), schoolId);

        InstitutionEntity institution = requireSingletonInstitution();
        String normalizedScope = scope.toUpperCase(Locale.ROOT);
        List<FeatureDefinitionEntity> defs = featureDefinitionRepository.findByActiveTrueOrderByFeatureCodeAsc();
        List<FeatureConfigDTO> out = new ArrayList<>();

        for (FeatureDefinitionEntity def : defs) {
            Optional<FeatureConfigurationEntity> existing = findConfig(
                    def.getFeatureCode(), normalizedScope, institution.getId(), schoolId);
            if (existing.isPresent()) {
                FeatureConfigurationEntity cfg = existing.get();
                List<String> roleCodes = featureRoleGrantRepository.findByConfigIdAndActiveTrue(cfg.getId()).stream()
                        .map(FeatureRoleGrantEntity::getRoleCode)
                        .toList();
                out.add(new FeatureConfigDTO(
                        cfg.getId(),
                        def.getFeatureCode(),
                        def.getFeatureName(),
                        cfg.getScope(),
                        cfg.getInstitutionId(),
                        cfg.getSchoolId(),
                        cfg.isEnabled(),
                        roleCodes,
                        true
                ));
            } else {
                out.add(new FeatureConfigDTO(
                        null,
                        def.getFeatureCode(),
                        def.getFeatureName(),
                        normalizedScope,
                        institution.getId(),
                        schoolId,
                        def.isDefaultEnabled(),
                        List.copyOf(OrgAccessPolicy.baselineRolesFor(def.getFeatureCode())),
                        false
                ));
            }
        }
        return out;
    }

    @Transactional
    public FeatureConfigDTO upsert(UpsertFeatureConfigRequestDTO request) {
        String scope = request.scope().trim().toUpperCase(Locale.ROOT);
        UUID schoolId = request.schoolId();
        assertFeatureAccess(OrgAccessPolicy.FEATURE_CONFIGURATION, scopeForConfigManage(scope), schoolId);

        if (!SCOPE_INSTITUTION.equals(scope) && !SCOPE_SCHOOL.equals(scope)) {
            throw new IllegalArgumentException("scope must be INSTITUTION or SCHOOL");
        }
        if (SCOPE_SCHOOL.equals(scope) && schoolId == null) {
            throw new IllegalArgumentException("schoolId is required for SCHOOL scope");
        }
        if (SCOPE_INSTITUTION.equals(scope)) {
            schoolId = null;
        }

        InstitutionEntity institution = requireSingletonInstitution();
        if (schoolId != null) {
            SchoolEntity school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new NoDataFoundException("School not found"));
            if (!institution.getId().equals(school.getInstitutionId())) {
                throw new IllegalArgumentException("School does not belong to the institution");
            }
        }

        FeatureDefinitionEntity def = featureDefinitionRepository.findById(request.featureCode().trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new NoDataFoundException("Unknown feature: " + request.featureCode()));

        FeatureConfigurationEntity cfg = findConfig(def.getFeatureCode(), scope, institution.getId(), schoolId)
                .orElseGet(FeatureConfigurationEntity::new);
        cfg.setFeatureCode(def.getFeatureCode());
        cfg.setScope(scope);
        cfg.setInstitutionId(institution.getId());
        cfg.setSchoolId(schoolId);
        cfg.setEnabled(Boolean.TRUE.equals(request.enabled()));
        cfg = featureConfigurationRepository.save(cfg);

        featureRoleGrantRepository.deleteByConfigId(cfg.getId());
        List<String> normalizedRoles = RoleAssignmentPolicy.normalizeAll(
                request.roleCodes() == null ? List.of() : request.roleCodes()
        ).stream().toList();
        for (String role : normalizedRoles) {
            featureRoleGrantRepository.save(new FeatureRoleGrantEntity(cfg.getId(), role));
        }

        return new FeatureConfigDTO(
                cfg.getId(),
                def.getFeatureCode(),
                def.getFeatureName(),
                cfg.getScope(),
                cfg.getInstitutionId(),
                cfg.getSchoolId(),
                cfg.isEnabled(),
                normalizedRoles,
                true
        );
    }

    private String scopeForConfigManage(String scope) {
        return SCOPE_SCHOOL.equalsIgnoreCase(scope) ? SCOPE_SCHOOL : SCOPE_INSTITUTION;
    }

    private Optional<FeatureConfigurationEntity> findConfig(
            String featureCode, String scope, UUID institutionId, UUID schoolId) {
        if (SCOPE_SCHOOL.equalsIgnoreCase(scope) && schoolId != null) {
            return featureConfigurationRepository
                    .findByFeatureCodeAndScopeAndSchoolIdAndActiveTrue(featureCode, SCOPE_SCHOOL, schoolId);
        }
        return featureConfigurationRepository
                .findByFeatureCodeAndScopeAndInstitutionIdAndSchoolIdIsNullAndActiveTrue(
                        featureCode, SCOPE_INSTITUTION, institutionId);
    }
}
