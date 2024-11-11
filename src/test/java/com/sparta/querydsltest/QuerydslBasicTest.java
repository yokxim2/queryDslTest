package com.sparta.querydsltest;

import static com.sparta.querydsltest.entity.QMember.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.List;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.querydsltest.entity.Member;
import com.sparta.querydsltest.entity.QMember;
import com.sparta.querydsltest.entity.Team;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@Test
	@BeforeEach
	public void before() {
		queryFactory = new JPAQueryFactory(em);
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);

		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
	}

	@Test
	public void startJPQL() {
		// member1을 찾아라.
		String qlString =
			"select m from Member m " +
			"where m.username = :username";

		Member findMember = em.createQuery(qlString, Member.class)
			.setParameter("username", "member1")
			.getSingleResult();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	// QueryDsl을 사용하면 오류를 컴파일 시점에 파악할 수 있고,
	// PreparedStatement를 사용해서 parameter binding을 자동으로 해준다.
	@Test
	public void startQuerydsl() {
		Member findMember = queryFactory
			.select(member)
			.from(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void search() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1")
				.and(member.age.eq(10)))
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void searchAndParam() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				member.username.eq("member1"),
				member.age.eq(10)
			)
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void resultFetch() {
		// // List
		// List<Member> fetch = queryFactory
		// 	.selectFrom(member)
		// 	.fetch();
		//
		// // 단 건
		// Member fetchOne = queryFactory
		// 	.selectFrom(member)
		// 	.fetchOne();
		//
		// // 처음 한 건 조회
		// Member fetchFirst = queryFactory
		// 	.selectFrom(member)
		// 	.fetchFirst();

		// 페이징에서 사용
		QueryResults<Member> results = queryFactory
			.selectFrom(member)
			.fetchResults();
		results.getTotal();
		List<Member> content = results.getResults();

		// count 쿼리로 변경
		long total = queryFactory
			.selectFrom(member)
			.fetchCount();
	}
}
