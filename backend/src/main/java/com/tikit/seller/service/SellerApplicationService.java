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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerApplicationService {

    private final SellerApplicationRepository sellerApplicationRepository;
    private final AccountRepository accountRepository;

    //판매자 등록 신청
    @Transactional
    public SellerApplicationResponseDTO apply(Long memberId, SellerApplicationRequestDTO dto){

        //로그인한 회원조회 ("요청한 리소스를 찾을 수 없거나 접근할 수 없습니다.")
        Member member = accountRepository.findById(memberId)
                .orElseThrow(()->new SellerException(ErrorCode.RESOURCE_NOT_FOUND));

        //"이용이 제한된 계정입니다."
        if(member.getStatus() != MemberStatus.ACTIVE){
            throw new SellerException(ErrorCode.ACCOUNT_RESTRICTED);
        }

        //일반회원만 신청가능("접근 권한이 없거나 현재 상태에서는 접근할 수 없습니다.")
        if(member.getRole() != Role.MEMBER){
            throw new SellerException(ErrorCode.ACCESS_DENIED);
        }

        //심사 대기 중 중복신청 불가능("진행 중인 판매자 신청이 있거나 이미 승인된 판매자입니다.")
        if(sellerApplicationRepository.hasStatus(memberId, SellerApplicationStatus.PENDING)){
            throw new SellerException(ErrorCode.SELLER_APPLICATION_EXISTS);
        }

        //신청서 작성, 저장
        SellerApplication application = SellerApplication.create(
                member,
                dto.getBusinessName(),
                dto.getContactEmail(),
                dto.getDescription(),
                dto.getEvidenceUrls()
        );
        SellerApplication saved = sellerApplicationRepository.save(application);
        return  SellerApplicationResponseDTO.from(saved);
    }

    //판매자 신청결과 조회(가장최근신청1건)
    @Transactional(readOnly=true)
    public SellerApplicationResponseDTO getMyApplication(Long memberId){
        SellerApplication application = sellerApplicationRepository
                .findLatest(memberId)
                .orElseThrow(()->new SellerException(ErrorCode.RESOURCE_NOT_FOUND));
        return  SellerApplicationResponseDTO.from(application);
    }

}
