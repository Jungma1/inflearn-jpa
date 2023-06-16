package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Test
    void test_member() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); // JPA 영속성 컨텍스트 동일성 보장
    }

    @Test
    void basic_crud() {
        Member memberA = new Member("memberA");
        Member memberB = new Member("memberB");
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // 단건 조회 검증
        Member findMemberA = memberRepository.findById(memberA.getId()).get();
        Member findMemberB = memberRepository.findById(memberB.getId()).get();
        assertThat(findMemberA).isEqualTo(memberA);
        assertThat(findMemberB).isEqualTo(memberB);

        // 리스트 조회 검증
        List<Member> members = memberRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(memberA);
        memberRepository.delete(memberB);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThan() {
        Member memberA = new Member("member", 10);
        Member memberB = new Member("member", 20);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("member", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("member");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void testNamedQuery() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> members = memberRepository.findByUsername("memberA");
        Member member = members.get(0);

        assertThat(member).isEqualTo(memberA);
    }

    @Test
    void testQuery() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> members = memberRepository.findMember("memberA", 10);
        Member member = members.get(0);

        assertThat(member).isEqualTo(memberA);
    }

    @Test
    void findUsernameList() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<String> usernames = memberRepository.findUsernameList();

        for (String username : usernames) {
            System.out.println("username = " + username);
        }
    }

    @Test
    void findMemberDto() {
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member memberA = new Member("memberA", 10);
        memberA.setTeam(teamA);
        memberRepository.save(memberA);

        List<MemberDto> members = memberRepository.findMemberDto();

        for (MemberDto member : members) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void findByNames() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> members = memberRepository.findByNames(Arrays.asList("memberA", "memberB"));

        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void returnType() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> members = memberRepository.findListByUsername("memberA");
        Member findMemberA = memberRepository.findMemberByUsername("memberA");
        Optional<Member> findOptionalMember = memberRepository.findOptionalByUsername("memberA");

        // 컬렉션 조회 시 결과가 없으면 빈 컬렉션 반환 (null 이 아님)
        List<Member> result = memberRepository.findListByUsername("none");
        System.out.println("result = " + result);

        // 단건 조회 시 결과가 없으면 null 반환
        Member result2 = memberRepository.findMemberByUsername("none");
        System.out.println("result2 = " + result2);

        // 결론적으로 컬렉션 조회 시 결과가 없으면 빈 컬렉션 반환, 단건 조회 시 결과가 없으면 null 반환
    }

    @Test
    @DisplayName("Spring Data JPA - 페이징 쿼리 테스트")
    void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by("username").descending());

        // when
        // Page 는 count 쿼리를 실행함
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // Slice 는 count 쿼리를 실행하지 않음
        // size + 1 을 조회해서 다음 페이지가 있는지 확인할 수 있음 (모바일에서 더보기 버튼)
        Slice<Member> pageSlice = memberRepository.findSliceByAge(age, pageRequest);

        // List 는 count 쿼리를 실행하지 않음
        List<Member> pageList = memberRepository.findListByAge(age, pageRequest);

        // *DTO 로 변환
        Page<MemberDto> pageDto = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        // then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(3); // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 번호 (5/3 = 1.66.. -> 2)
        assertThat(page.isFirst()).isTrue(); // 첫번째 페이지인가?
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는가?
    }

    @Test
    @DisplayName("Spring Data JPA - 벌크성 수정 쿼리 테스트")
    void bulkUpdate() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        // when
        int resultCount = memberRepository.bulkAgePlus(20);

        // 벌크성 수정 쿼리는 영속성 컨텍스트를 무시하고 DB 에 바로 쿼리를 날림
        // 따라서, 벌크성 수정 쿼리를 실행하고 나면 영속성 컨텍스트와 DB 의 데이터가 달라짐
        // Spring Data JPA 에서 지원하는 벌크성 수정 쿼리는 @Modifying(clearAutomatically = true) 옵션으로 자동화

//        em.flush(); // 영속성 컨텍스트의 변경 내용을 DB 에 반영 (벌크 연산은 엔티티를 직접 변경하지 않아 flush 를 할때 DB 에 반영하지 않음)
//        em.clear(); // 영속성 컨텍스트를 초기화

        Member findMember = memberRepository.findMemberByUsername("member5"); // DB 에서 조회 (영속성 컨텍스트 X)
        System.out.println("findMember = " + findMember); // 41

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    void findMemberLazy() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 10, teamB);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    void queryHint() {
        // given
        memberRepository.save(new Member("memberA", 10));
        em.flush();
        em.clear();

        // when
        Member findMember = memberRepository.findReadOnlyByUsername("memberA");
        findMember.setUsername("memberB"); // @QueryHint 에 의해 읽기 전용으로 조회되어 변경 감지가 되지 않음
        em.flush();
    }

    @Test
    void lock() {
        // given
        memberRepository.save(new Member("memberA", 10));
        em.flush();
        em.clear();

        // when
        List<Member> findMember = memberRepository.findLockByUsername("memberA");
    }

    @Test
    @DisplayName("사용자 정의 리포지토리 테스트")
    void call_custom() {
        List<Member> members = memberRepository.findMemberCustom();
    }

    @Test
    @DisplayName("Specification 테스트 (실무 사용 X)")
    void spec_basic() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> members = memberRepository.findAll(spec);

        // then
        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Query By Example 테스트 (실무 사용 X)")
    void query_by_example() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");

        Example<Member> example = Example.of(member, matcher);

        List<Member> members = memberRepository.findAll(example);

        // then
        assertThat(members.get(0).getUsername()).isEqualTo("m1");
    }

    @Test
    @DisplayName("Projections 테스트")
    void projections() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");
        for (UsernameOnly usernameOnly : result) {
            // 스프링이 만든 구현체
            // usernameOnly = org.springframework.data.jpa.repository.query.AbstractJpaQuery$TupleConverter$TupleBackedMap@1582dfec
            System.out.println("usernameOnly = " + usernameOnly);
        }

        List<UsernameOnlyDto> result2 = memberRepository.findProjectionsDtoByUsername("m1");
        for (UsernameOnlyDto usernameOnlyDto : result2) {
            // usernameOnlyDto = study.datajpa.repository.UsernameOnlyDto@2407f1a8
            System.out.println("usernameOnlyDto = " + usernameOnlyDto);
        }

        List<UsernameOnlyDto> result3 = memberRepository.findProjectionsByUsername("m1", UsernameOnlyDto.class);
        for (UsernameOnlyDto usernameOnlyDto : result3) {
            // usernameOnlyDto = study.datajpa.repository.UsernameOnlyDto@18bec5eb
            System.out.println("usernameOnlyDto = " + usernameOnlyDto);
        }

        List<NestedClosedProjections> result4 = memberRepository.findProjectionsByUsername("m1",
                NestedClosedProjections.class);
        for (NestedClosedProjections nestedClosedProjections : result4) {
            String username = nestedClosedProjections.getUsername();
            System.out.println("username = " + username);
            String teamName = nestedClosedProjections.getTeam().getName();
            System.out.println("teamName = " + teamName);
        }
    }

    @Test
    @DisplayName("네이티브 쿼리 테스트")
    void native_query() {
        // given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Member result = memberRepository.findNativeQuery("m1");
        System.out.println("result = " + result);

        Page<MemberProjection> result2 = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result2.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection = " + memberProjection.getUsername());
            System.out.println("memberProjection = " + memberProjection.getTeamName());
        }
    }
}
