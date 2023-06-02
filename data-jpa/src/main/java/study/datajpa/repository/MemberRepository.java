package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * NamedQuery
     * Entity 의 @NamedQuery 를 설정하고, Repository 에서 @Query(name = "Member.findByUsername") 지정
     */
//    @Query(name = "Member.findByUsername") // 생략 가능
    List<Member> findByUsername(@Param("username") String username);

    /**
     * Repository 에서 @Query 를 사용하여 직접 쿼리를 작성할 수 있다.
     * Application 실행 시점에 오류를 발견할 수 있다.
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findMember(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    /**
     * DTO 로 조회
     */
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * 컬렉션 파라미터 바인딩
     */
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username); // 컬렉션 반환

    Member findMemberByUsername(String username); // 단건 반환

    Optional<Member> findOptionalByUsername(String username); // 단건 - Optional 반환

    /**
     * 페이징 쿼리
     * count 쿼리를 분리하여 성능을 최적화할 수 있다. (분리를 하지 않으면 불필요한 조인이 발생)
     */
    @Query(value = "select m from Member m left join m.team t", countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    List<Member> findListByAge(int age, Pageable pageable);

    /**
     * 벌크성 수정 쿼리
     */
    @Modifying(clearAutomatically = true) // executeUpdate() 를 실행
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /**
     * EntityGraph 를 사용하여 연관된 엔티티를 한번에 조회 (fetch join)
     */
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    /**
     * QueryHint
     * Read Only!
     */
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    /**
     * Lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
}
