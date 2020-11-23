package study.datajpa.repository;



import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {
    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public  void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        //변경감지 테스트
        //findMember1.setUsername("Member!!!");
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }
    @Test
    public void findByUsernameAndAgeGreaterThen(){
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA",15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();
        assertThat(result.get(0)).isEqualTo(m1.getUsername());
    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member ("aaa", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto =" + dto);
        }
    }

    @Test
    public void findByNames(){

        Member m1 = new Member ("aaa", 10);
        Member m2 = new Member ("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<String> names = new ArrayList<>();
        names.add("aaa");
        names.add("bbb");
        List<Member> byNames = memberRepository.findByNames(names);
        for (Member byName : byNames) {
            System.out.println("member =" + byName);
        }
    }
    @Test
    public void returnTypeTest(){
        List<Member> members = memberRepository.findListByUsername("aaa");
        Member member= memberRepository.findMemberByUsername("aaa");
        Optional<Member> Optional = memberRepository.findOptionalByUsername("ccc");
    }
    @Test
    public void paging(){
        //given
        memberRepository.save(new Member("Member1",10));
        memberRepository.save(new Member("Member2",10));
        memberRepository.save(new Member("Member3",10));
        memberRepository.save(new Member("Member4",10));
        memberRepository.save(new Member("Member5",10));

        int age = 10 ;

        //spring data jpa는 페이지를 0부터 시작한다
        //pagerequest를 만들어 넘겨줘야한다 PageRequest.of(0페이지부터 3개씩 username을 desc로 해서 받아온다 )
        //반환타입을 Page로 받으면 토탈카운트 쿼리까지 같이 날려주기 때문에 토탈카운트 가져오는 코드가 필요없다
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        //반환 타입을 Page로해서 페이징 정보를 가져온다
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        
        //Entity를 바로 노출하는 것은 좋은 방법이 아니기 떄문에 
        //DTO로 변환 하여 노출하는 것이 좋다.
        //Dto로 간단하게 변환하는 방법
        Page<MemberDto> map = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        //실제 데이터를 꺼내온다.
        List<Member> content = page.getContent();
        //TotalCount를 가져온다.
        long totalElements = page.getTotalElements();

        //then
        //가져온 리스트의 크기
        assertThat(content.size()).isEqualTo(3);
        //totalCount
        assertThat(page.getTotalElements()).isEqualTo(5);
        //현재 페이지
        assertThat(page.getNumber()).isEqualTo(0);
        //총 페이지 수
        assertThat(page.getTotalPages()).isEqualTo(2);
        //첫번째 페이지인지
        assertThat(page.isFirst()).isTrue();
        //다음페이지가 있는지
        assertThat(page.hasNext()).isTrue();

        //토탈 카운트 토탈페이지가 필요없는 더보기 형식의 페이징 같은경우에는 slice를 쓴다.
        //Slice 로 받으면 pageRequest에 정해놓은 limits 보다 1개씩 더가져와서
        //1개더가 있다면 다음페이지가 있다고 간주하고 더보기를 띄우고 아니면
        //더보기를 띄우지 않는 식으로 API설계가 많이 이루어진다.
//        Slice<Member> slicePage = memberRepository.findByAge(age, pageRequest);
//        List<Member> contents = slicePage.getContent();
//        //현재 페이지
//        assertThat(slicePage.getNumber()).isEqualTo(0);
//
//        //첫번째 페이지인지
//        assertThat(slicePage.isFirst()).isTrue();
//        //다음페이지가 있는지
//        assertThat(slicePage.hasNext()).isTrue();
    }
    @Test
    public void bulkUpdate(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));

        int resultCount = memberRepository.bulkAgePlus(20);
       //Spring data jpa의  @Modifying(clearAutomatically = true)이
        //아래의 flush clear를 대신해 주었다
       
        //쿼리를 인위적으로 날려주는 메서드.
       // em.flush();
        //영속성 컨텍스트를 초기화시켜주는 메서드
       // em.clear();

        //~~~~~~~~~~~~~~~아래부터는 영속성컨택스트가 리셋된 상태이기 때문에
        //아래 리스트 조회에서는 위의 벌크성 업데이트가 반영된 데이터가 출력된다.
        //코드에 JPQL이 있으면 JPQL이 아닌닌 것들이 먼저 flush 되고 jpql이 flush 된다.

        //벌크성 수정쿼리는 영속성 컨텍스트모르게 바로 update 시키기때문에
        //쿼리가 날라가 DB는 업데이트 되었으나 영속성 컨택스트의 엔티티는 수정이 되지않은 상태이기 때문에
        //영속성 컨택스트를 한번 날려줘야한다.
        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5= " +member5.getAge());
        //when
        //20 이상인 사람 다 1살 플러스시킨다.
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy(){
        //given
        //member 1 -> team A
        //member 2 -> team B 를 참조

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
        //멤버만 가져오고 멤버안의 팀은 레이지로딩으로 인해 프록싱객체가 가져와진다
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            //멤버의 정보이기 때문에 문제없고
            System.out.println("member= " + member.getUsername());
            //멤버의 팀을 가져올때 실제 디비에 쿼리를 날린다.(프록시초기화) * n+1 문제가 발생한다
            //페치조인으로 해결해야한다.
            System.out.println("member.team=" +member.getTeam().getName());
        }
    }

    @Test
    public void queryHint(){
        //given
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");
    }
}