package study.querydsl.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

    /**
     * 전체 카운트를 한번에 조회
     */
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    /**
     * 데이터 내용과 전체 카운트를 별도로 조회
     */
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
