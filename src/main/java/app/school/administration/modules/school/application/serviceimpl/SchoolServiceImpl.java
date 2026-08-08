package app.school.administration.modules.school.application.serviceimpl;

import app.school.administration.auth.application.constant.OrgAccessPolicy;
import app.school.administration.common.application.custom.exception.NoDataFoundException;
import app.school.administration.common.application.serviceimpl.AppBaseService;
import app.school.administration.common.domain.repository.AppBaseRepository;
import app.school.administration.modules.featureconfig.application.service.FeatureAccessService;
import app.school.administration.modules.institution.persistance.entity.InstitutionEntity;
import app.school.administration.modules.school.api.request.UpsertSchoolRequestDTO;
import app.school.administration.modules.school.api.response.SchoolDTO;
import app.school.administration.modules.school.application.service.SchoolService;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import app.school.administration.modules.school.persistance.projection.SchoolProjectionDTO;
import app.school.administration.modules.school.persistance.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl extends AppBaseService<SchoolEntity, UUID> implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final FeatureAccessService featureAccessService;

    @Override
    protected AppBaseRepository<SchoolEntity, UUID> getJpaRepository() {
        return schoolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolDTO getById(UUID uuid) {
        SchoolEntity school = schoolRepository.findById(uuid)
                .orElseThrow(() -> new NoDataFoundException("School not found"));
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOL_SETTINGS,
                FeatureAccessService.SCOPE_SCHOOL,
                school.getId()
        );
        return toDto(school);
    }

    @Override
    @Transactional
    public SchoolDTO create(UpsertSchoolRequestDTO request) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOLS_LIST,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        OrgAccessPolicy.assertAllows(
                featureAccessService.callerRoles(),
                OrgAccessPolicy.SCHOOL_CREATE_ROLES,
                "create a school"
        );
        InstitutionEntity institution = featureAccessService.requireSingletonInstitution();
        SchoolEntity school = new SchoolEntity();
        apply(school, request);
        school.setInstitutionId(institution.getId());
        if (school.getStartedDate() == null) {
            school.setStartedDate(Instant.now());
        }
        return toDto(schoolRepository.save(school));
    }

    @Override
    @Transactional
    public SchoolDTO updateSettings(UUID uuid, UpsertSchoolRequestDTO request) {
        SchoolEntity school = schoolRepository.findById(uuid)
                .orElseThrow(() -> new NoDataFoundException("School not found"));
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOL_SETTINGS,
                FeatureAccessService.SCOPE_SCHOOL,
                school.getId()
        );
        apply(school, request);
        return toDto(schoolRepository.save(school));
    }

    @Override
    public SchoolEntity deActivate(UUID uuid) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOLS_LIST,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        return appDeActivate(uuid);
    }

    @Override
    public SchoolEntity activate(UUID uuid) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOLS_LIST,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        return appActivate(uuid);
    }

    @Override
    public SchoolEntity save(SchoolEntity schoolEntity) {
        InstitutionEntity institution = featureAccessService.requireSingletonInstitution();
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOLS_LIST,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        if (schoolEntity.getInstitutionId() == null) {
            schoolEntity.setInstitutionId(institution.getId());
        }
        return appSave(schoolEntity);
    }

    @Override
    public SchoolEntity update(SchoolProjectionDTO dto, UUID uuid) {
        SchoolEntity school = appFindByIdWithDirtyCheck(uuid);
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOL_SETTINGS,
                FeatureAccessService.SCOPE_SCHOOL,
                school.getId()
        );
        BeanUtils.copyProperties(dto, school);
        return school;
    }

    @Override
    public SchoolProjectionDTO findByIdProjection(UUID uuid) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOL_SETTINGS,
                FeatureAccessService.SCOPE_SCHOOL,
                uuid
        );
        return appFindByIdProjection(uuid, SchoolProjectionDTO.class);
    }

    private void apply(SchoolEntity school, UpsertSchoolRequestDTO request) {
        school.setSchoolName(request.schoolName().trim());
        school.setSchoolCode(blankToNull(request.schoolCode()));
        school.setEmail(blankToNull(request.email()));
        school.setPhone(blankToNull(request.phone()));
        school.setAddressLine1(blankToNull(request.addressLine1()));
        school.setAddressLine2(blankToNull(request.addressLine2()));
        school.setCity(blankToNull(request.city()));
        school.setState(blankToNull(request.state()));
        school.setCountry(blankToNull(request.country()));
        school.setPostalCode(blankToNull(request.postalCode()));
        school.setPrincipalName(blankToNull(request.principalName()));
        school.setBoardAffiliation(blankToNull(request.boardAffiliation()));
        if (request.startedDate() != null) {
            school.setStartedDate(request.startedDate());
        }
        school.setTimezone(blankToNull(request.timezone()));
        school.setDescription(blankToNull(request.description()));
    }

    private SchoolDTO toDto(SchoolEntity s) {
        return new SchoolDTO(
                s.getId(),
                s.getInstitutionId(),
                s.getSchoolName(),
                s.getSchoolCode(),
                s.getEmail(),
                s.getPhone(),
                s.getAddressLine1(),
                s.getAddressLine2(),
                s.getCity(),
                s.getState(),
                s.getCountry(),
                s.getPostalCode(),
                s.getPrincipalName(),
                s.getBoardAffiliation(),
                s.getStartedDate(),
                s.getTimezone(),
                s.getDescription(),
                s.isActive()
        );
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
