package jpabook.jpashop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Rollback(false) // 테스트 케이스에 있으면, 테스트 케이스가 끝나도 롤백을 하지 않는다.
    @Transactional // 테스트 케이스에 있으면, 테스트 케이스가 끝나면 롤백을 해준다.
    @Test
    void testMember() throws Exception {
        // given
        Member member = new Member();
        member.setUsername("memberA");

        // when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);

        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

        // 영속성 컨텍스트에서 관리되는 엔티티는 같은 트랜잭션 안에서는 같은 인스턴스를 보장한다.
        System.out.println("findMember == member: " + (findMember == member));
    }
}
