package app.school.administration.modules.institution.application.service;

import app.school.administration.auth.application.constant.OrgAccessPolicy;
import app.school.administration.modules.institution.api.request.UpdateInstitutionRequestDTO;
import app.school.administration.modules.institution.api.response.InstitutionDTO;
import app.school.administration.modules.institution.persistance.entity.InstitutionEntity;
import app.school.administration.modules.institution.persistance.repository.InstitutionRepository;
import app.school.administration.modules.featureconfig.application.service.FeatureAccessService;
import app.school.administration.modules.school.api.response.SchoolListItemDTO;
import app.school.administration.modules.school.persistance.entity.SchoolEntity;
import app.school.administration.modules.school.persistance.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final SchoolRepository schoolRepository;
    private final FeatureAccessService featureAccessService;

    @Transactional(readOnly = true)
    public InstitutionDTO getSingleton() {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_INSTITUTION_SETTINGS,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        return toDto(featureAccessService.requireSingletonInstitution());
    }

    @Transactional
    public InstitutionDTO update(UpdateInstitutionRequestDTO request) {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_INSTITUTION_SETTINGS,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        InstitutionEntity entity = featureAccessService.requireSingletonInstitution();
        entity.setInstitutionName(request.institutionName().trim());
        entity.setLegalName(blankToNull(request.legalName()));
        entity.setRegistrationNumber(blankToNull(request.registrationNumber()));
        entity.setEmail(blankToNull(request.email()));
        entity.setPhone(blankToNull(request.phone()));
        entity.setAddressLine1(blankToNull(request.addressLine1()));
        entity.setAddressLine2(blankToNull(request.addressLine2()));
        entity.setCity(blankToNull(request.city()));
        entity.setState(blankToNull(request.state()));
        entity.setCountry(blankToNull(request.country()));
        entity.setPostalCode(blankToNull(request.postalCode()));
        entity.setWebsite(blankToNull(request.website()));
        entity.setLogoUrl(blankToNull(request.logoUrl()));
        entity.setDescription(blankToNull(request.description()));
        return toDto(institutionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SchoolListItemDTO> listSchools() {
        featureAccessService.assertFeatureAccess(
                OrgAccessPolicy.FEATURE_SCHOOLS_LIST,
                FeatureAccessService.SCOPE_INSTITUTION,
                null
        );
        InstitutionEntity institution = featureAccessService.requireSingletonInstitution();
        return schoolRepository.findByInstitutionIdAndActiveTrueOrderBySchoolNameAsc(institution.getId())
                .stream()
                .map(this::toSchoolItem)
                .toList();
    }

    private InstitutionDTO toDto(InstitutionEntity e) {
        return new InstitutionDTO(
                e.getId(),
                e.getInstitutionName(),
                e.getLegalName(),
                e.getRegistrationNumber(),
                e.getEmail(),
                e.getPhone(),
                e.getAddressLine1(),
                e.getAddressLine2(),
                e.getCity(),
                e.getState(),
                e.getCountry(),
                e.getPostalCode(),
                e.getWebsite(),
                e.getLogoUrl(),
                e.getDescription(),
                e.isActive()
        );
    }

    private SchoolListItemDTO toSchoolItem(SchoolEntity s) {
        return new SchoolListItemDTO(
                s.getId(),
                s.getInstitutionId(),
                s.getSchoolName(),
                s.getSchoolCode(),
                s.getEmail(),
                s.getPhone(),
                s.getCity(),
                s.getPrincipalName(),
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
