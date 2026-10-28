package com.tikit.seller.service;

import com.tikit.auth.entity.Member;
import com.tikit.auth.entity.MemberStatus;
import com.tikit.auth.entity.Role;
import com.tikit.auth.repository.AccountRepository;
import com.tikit.common.ErrorCode;
import com.tikit.seller.dto.SellerApplicationRequestDTO;
import com.tikit.seller.dto.SellerApplicationResponseDTO;
import com.tikit.seller.entity.SellerApplication;
import com.tikit.seller.entity.SellerApplicationStatus;
import com.tikit.seller.exception.SellerException;
import com.tikit.seller.repository.SellerApplicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class SellerApplicationServiceTest {

    @Mock
    private SellerApplicationRepository sellerApplicationRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private SellerApplicationService service;


    //요청dto
    private SellerApplicationRequestDTO request() {
        SellerApplicationRequestDTO dto = new SellerApplicationRequestDTO();
        ReflectionTestUtils.setField(dto, "businessName", "테스트회사");
        ReflectionTestUtils.setField(dto, "contactEmail", "test@test.com");
        ReflectionTestUtils.setField(dto, "description", "신청 사유입니다");
        ReflectionTestUtils.setField(dto, "evidenceUrls", List.of("https://example.com/1"));
        return dto;
    }

    //가짜회원
    private Member member(MemberStatus status, Role role) {
        Member member = mock(Member.class);
        lenient().when(member.getStatus()).thenReturn(status);
        lenient().when(member.getRole()).thenReturn(role);
        return member;
    }


    private void assertApplyFails(Long memberId, ErrorCode expected) {
        assertThatThrownBy(() -> service.apply(memberId, request()))
                .isInstanceOf(SellerException.class)
                .extracting("errorCode").isEqualTo(expected);
    }


    // apply(판매자 신청)
    @Test
    @DisplayName("일반 회원이 신청하면 PENDING 상태로 저장된다")
    void apply_success() {
        // given
        Member member = member(MemberStatus.ACTIVE, Role.MEMBER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(member));
        when(sellerApplicationRepository.hasStatus(1L, SellerApplicationStatus.PENDING))
                .thenReturn(false);
        when(sellerApplicationRepository.save(any(SellerApplication.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        service.apply(1L, request());

        // then
        ArgumentCaptor<SellerApplication> captor = ArgumentCaptor.forClass(SellerApplication.class);
        verify(sellerApplicationRepository).save(captor.capture());

        SellerApplication saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(SellerApplicationStatus.PENDING);
        assertThat(saved.getMember()).isSameAs(member);
        assertThat(saved.getBusinessName()).isEqualTo("테스트회사");
        assertThat(saved.getContactEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 RESOURCE_NOT_FOUND")
    void apply_memberNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertApplyFails(99L, ErrorCode.RESOURCE_NOT_FOUND);

        verify(sellerApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("제한된 계정은 신청할 수 없다 (ACCOUNT_RESTRICTED)")
    void apply_restrictedAccount() {
        Member member = member(MemberStatus.SUSPENDED, Role.MEMBER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(member));

        assertApplyFails(1L, ErrorCode.ACCOUNT_RESTRICTED);
        verify(sellerApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 판매자인 회원은 신청할 수 없다 (SELLER_APPLICATION_EXISTS)")
    void apply_alreadySeller() {
        Member member = member(MemberStatus.ACTIVE, Role.SELLER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(member));

        assertApplyFails(1L, ErrorCode.SELLER_APPLICATION_EXISTS);
        verify(sellerApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("일반 회원이 아니면 신청할 수 없다 (ACCESS_DENIED)")
    void apply_notMemberRole() {
        Member member = member(MemberStatus.ACTIVE, Role.ADMIN);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(member));

        assertApplyFails(1L, ErrorCode.ACCESS_DENIED);
        verify(sellerApplicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 PENDING 신청이 있으면 중복 신청으로 거부된다")
    void apply_duplicatePending() {
        Member member = member(MemberStatus.ACTIVE, Role.MEMBER);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(member));
        when(sellerApplicationRepository.hasStatus(1L, SellerApplicationStatus.PENDING))
                .thenReturn(true);

        assertApplyFails(1L, ErrorCode.SELLER_APPLICATION_EXISTS);
        verify(sellerApplicationRepository, never()).save(any());
    }



    // getMyApplication (신청 조회)

    @Test
    @DisplayName("신청 이력이 있으면 가장 최근 신청을 반환한다")
    void getMyApplication_success() {
        SellerApplication application = SellerApplication.create(
                member(MemberStatus.ACTIVE, Role.MEMBER),
                "테스트 기획사", "test@test.com", "신청 사유",
                List.of("https://example.com/1"));
        when(sellerApplicationRepository.findLatest(1L)).thenReturn(Optional.of(application));

        SellerApplicationResponseDTO result = service.getMyApplication(1L);

        assertThat(result).isNotNull();
        verify(sellerApplicationRepository).findLatest(1L);   // 로그인한 회원의 id로 찾았는지
    }

    @Test
    @DisplayName("신청 이력이 없으면 RESOURCE_NOT_FOUND (404)")
    void getMyApplication_notFound() {
        when(sellerApplicationRepository.findLatest(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyApplication(1L))
                .isInstanceOf(SellerException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
}