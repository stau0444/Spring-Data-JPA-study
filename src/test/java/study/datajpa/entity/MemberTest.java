package study.datajpa.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    @Rollback(value = false)
    public void testEntity(){
        //팀 엔티티 생성
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        //팀 엔티티 저장
        em.persist(teamA);
        em.persist(teamB);
        
        //멤버 엔티티 생성
        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",10,teamA);
        Member member3 = new Member("member3",10,teamB);
        Member member4 = new Member("member4",10,teamB);
        
        //멤버 엔티티 저장
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        
        //플러쉬 강제 호출 강제로 인서트 쿼리 날림
        em.flush();
        //영속성 컨텍스트 초기화
        em.clear();
        
        //확인
        //멤버를 모두 가져옴
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.team = "+ member.getTeam());
        }

    }
}