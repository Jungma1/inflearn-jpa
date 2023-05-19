package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
//    @Rollback(false) // @Transactional 은 기본적으로 롤백을 하기 때문에, 롤백을 하지 않도록 설정 (테스트 결과를 DB 에 반영하고 싶을 때 사용)
    @DisplayName("회원 가입이 된다.")
    void save_member() throws Exception {
        // given
        Member member = new Member();
        member.setName("member");

        // when
        Long savedId = memberService.join(member);

        // then
        assertThat(member).isEqualTo(memberRepository.findOne(savedId));
    }

    @Test
    @DisplayName("회원 이름이 중복될 경우 예외가 발생한다.")
    void duplicate_member_exception() throws Exception {
        // given
        Member memberA = new Member();
        memberA.setName("member");

        Member memberB = new Member();
        memberB.setName("member");

        // when
        memberService.join(memberA);

        // then
        assertThatThrownBy(() -> memberService.join(memberB))
                .isInstanceOf(IllegalStateException.class);
    }
}
