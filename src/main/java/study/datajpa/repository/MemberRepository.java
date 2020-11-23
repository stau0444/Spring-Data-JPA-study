package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

//스프링 데이터 JPA 리파지토리 생성방법
//인터페이스로 생성해 JpaRepository<엔티티 타입 , 맵핑 PK 데이터 타입>를 상속받는다
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username =:username and m.age = :age")
    List<Member> findUser(@Param("username") String username , @Param("age") int age);

    //유저이름만 가져오는 메서드
    @Query("select m.username from Member m ")
    List<String> findUsernameList();

    //DTO 조회
    @Query("select new study.datajpa.dto.MemberDto(m.id , m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    //컬랙션 파라미터 바인딩
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    List<Member> findListByUsername(String username);//컬랙션
    Member findMemberByUsername(String username);//단 건
    Optional<Member> findOptionalByUsername(String username);//옵셔널
    //Spring- Data -  JPA 페이징
    //쿼리에 조인이 많아지거나 복잡해질 경우 카운트 쿼리를 분리하여 카운트 쿼리는 기준이 되는
    ///테이블의 로우수만 세어지게 하는게 좋다.
//    @Query(value = "select m from Member m left join m.team t",
//            countQuery = "select count(m.username) from Member m")
//    Page<Member> findByAge(int age , Pageable pageable);
    //페이징 소팅 부분을 SPRING DATA JPA 에서 맡아 주기 떄문에
    //핵심 비지니스에 집중할 수 있다.
    //소팅같은 경우에도 복잡한 상황에서는 pageRequest에서 해결이 안되기 떄문에
    //@Query에 작성하는 JPQL에 작성해 주는 것이 좋다
    @Query(value = "select m from Member m left join m.team t")
    Page<Member> findByAge(int age , Pageable pageable);
    //Slice<Member> findByAge(int age , Pageable pageable);
    
    //spring data jpa 에서 벌크성 수정쿼리
    //@Modifying이 있어야 update 문이 나가고 없으면 일반 List를 반환한다.
    //벌크성 쿼리는 영속성 컨텍스트의 더티체킹을 무시하고 업데이트를 때려버리기 때문에
    //영속성 컨텍스트를 한번 리셋 시켜 줘야한다.
    //spring - data - jpa 에서는 modifying에 clearAutomatically =true 옵션으로
    //flush() / clear()를 대신할 수 있다
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age +1 where m.age>=:age")
    int bulkAgePlus(@Param("age") int age);

    //순수 JPA에서  페치조인을 통한 연관데이터 불러오기
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();


    //spring data jpa 에서의 @EntityGraph
    //이미 인터페이스에 쿼리 메서드로 내장 되있는 findAll이라는 메서드를 오버라이드하고
    //아래와 같이 @EntityGraph(attributePaths = {"team"}) 으로 주면
    //따로 페치조인 jpql 작성필요없이(내부적으로 페치조인 쿼리가 자동 생성됨)
    //연관관계인 데이터를 불러올 수 있다.
    //간단할떄는 EntityGraph를 사용하고 쿼리가 복잡해진다면 JPQL에 fetch 조인을 사용하는 것이 좋다.
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    //이런식으로 작성한 JPQL에 EntityGraph를 사용하여 페치조인 할 수도 있다.
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);
}
