package com.daily.app.backend.service;

import com.daily.app.backend.dto.business.BusinessCreateRequest;
import com.daily.app.backend.dto.business.BusinessResponse;
import com.daily.app.backend.dto.business.BusinessUpdateRequest;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.BusinessType;
import com.daily.app.backend.entity.User;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.mapper.BusinessMapper;
import com.daily.app.backend.repository.BusinessRepository;
import com.daily.app.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessServiceTest {

    private static final long USER_ID = 7L;

    private final BusinessRepository businessRepository = mock(BusinessRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BusinessService service =
            new BusinessService(businessRepository, userRepository, new BusinessMapper());

    private static Business business(long id, String clientId, boolean archived) {
        return Business.builder()
                .id(id)
                .businessName("Shop " + id)
                .businessType(BusinessType.GROCERY)
                .clientId(clientId)
                .archived(archived)
                .build();
    }

    @Test
    void createSavesNewBusinessForUser() {
        when(userRepository.getReferenceById(USER_ID)).thenReturn(User.builder().id(USER_ID).build());
        when(businessRepository.findByUserIdAndClientId(USER_ID, "local-1")).thenReturn(Optional.empty());
        when(businessRepository.save(any(Business.class))).thenAnswer(inv -> {
            Business saved = inv.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        BusinessResponse response = service.create(USER_ID,
                new BusinessCreateRequest("Car Showroom", BusinessType.GARAGE, "local-1"));

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.businessName()).isEqualTo("Car Showroom");
        assertThat(response.clientId()).isEqualTo("local-1");
        assertThat(response.archived()).isFalse();
    }

    @Test
    void retriedCreateWithSameClientIdReturnsExistingBusiness() {
        when(businessRepository.findByUserIdAndClientId(USER_ID, "local-1"))
                .thenReturn(Optional.of(business(11L, "local-1", false)));

        BusinessResponse response = service.create(USER_ID,
                new BusinessCreateRequest("Car Showroom", BusinessType.GARAGE, "local-1"));

        assertThat(response.id()).isEqualTo(11L);
        verify(businessRepository, never()).save(any());
    }

    @Test
    void updateRenamesAndChangesType() {
        when(businessRepository.findById(11L)).thenReturn(Optional.of(business(11L, null, false)));

        BusinessResponse response = service.updateBusiness(USER_ID, 11L,
                new BusinessUpdateRequest("Medical Store", BusinessType.MEDICAL_STORE, null));

        assertThat(response.businessName()).isEqualTo("Medical Store");
        assertThat(response.businessType()).isEqualTo(BusinessType.MEDICAL_STORE);
    }

    @Test
    void legacyNameOnlyUpdateKeepsTypeAndArchiveState() {
        when(businessRepository.findById(11L)).thenReturn(Optional.of(business(11L, null, false)));

        BusinessResponse response = service.updateBusiness(USER_ID, 11L, new BusinessUpdateRequest("Renamed", null, null));

        assertThat(response.businessName()).isEqualTo("Renamed");
        assertThat(response.businessType()).isEqualTo(BusinessType.GROCERY);
        assertThat(response.archived()).isFalse();
    }

    @Test
    void archivingIsAllowedWhileAnotherBusinessStaysActive() {
        when(businessRepository.findById(11L)).thenReturn(Optional.of(business(11L, null, false)));
        when(businessRepository.countByUserIdAndArchivedFalse(USER_ID)).thenReturn(2L);

        BusinessResponse response = service.updateBusiness(USER_ID, 11L, new BusinessUpdateRequest("Shop 11", null, true));

        assertThat(response.archived()).isTrue();
    }

    @Test
    void archivingTheLastActiveBusinessIsRejected() {
        when(businessRepository.findById(11L)).thenReturn(Optional.of(business(11L, null, false)));
        when(businessRepository.countByUserIdAndArchivedFalse(USER_ID)).thenReturn(1L);

        assertThatThrownBy(() -> service.updateBusiness(USER_ID, 11L, new BusinessUpdateRequest("Shop 11", null, true)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
