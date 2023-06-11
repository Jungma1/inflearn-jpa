package study.datajpa.repository;

import java.util.List;
import study.datajpa.entity.Member;

/**
 * 사용자 정의 리포지토리 인터페이스
 */
public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();
}
