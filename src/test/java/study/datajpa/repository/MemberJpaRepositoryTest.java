package study.datajpa.repository;



import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//테스트코드에선 스프링이 기본적으로 테스트가 끝날때 롤백시킨다.
//@Rollback(value = false)
@Transactional
@Rollback(value = false)
class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member save = memberJpaRepository.save(member);

        Member member1 = memberJpaRepository.find(save.getId());

        assertThat(member1.getId().equals(member.getId()));
        //assertThat(member1.getUserName().equals(member.getUserName()));
        assertThat(member1).isEqualTo(member);
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        
        //단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        //변경감지 테스트
        //findMember1.setUsername("Member!!!");
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberJpaRepository.Count();
        assertThat(count).isEqualTo(2);

        //삭제검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deleteCount = memberJpaRepository.Count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA",20);
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);

        List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThen("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA",20);
        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);

        List<Member> result = memberJpaRepository.findByUsername("AAA");

        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void paging(){
        //given
        memberJpaRepository.save(new Member("Member1",10));
        memberJpaRepository.save(new Member("Member2",10));
        memberJpaRepository.save(new Member("Member3",10));
        memberJpaRepository.save(new Member("Member4",10));
        memberJpaRepository.save(new Member("Member5",10));
        int age = 10;
        int offset = 0;
        int limit = 3;

        //when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(10);

        //then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
    }
    @Test
    public void bulkUpdate(){
        memberJpaRepository.save(new Member("member1",10));
        memberJpaRepository.save(new Member("member2",19));
        memberJpaRepository.save(new Member("member3",20));
        memberJpaRepository.save(new Member("member4",21));
        memberJpaRepository.save(new Member("member5",40));

        //when
        //20 이상인 사람 다 1살 플러스시킨다.
        int resultCount = memberJpaRepository.bulkAgePlus(20);
        assertThat(resultCount).isEqualTo(3);
    }
}