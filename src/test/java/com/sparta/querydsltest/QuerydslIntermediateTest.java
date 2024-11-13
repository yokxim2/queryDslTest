package com.sparta.querydsltest;

import static com.sparta.querydsltest.entity.QMember.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.querydsltest.dto.MemberDto;
import com.sparta.querydsltest.dto.UserDto;
import com.sparta.querydsltest.entity.Member;
import com.sparta.querydsltest.entity.QMember;
import com.sparta.querydsltest.entity.Team;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class QuerydslIntermediateTest {

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
	public void simpleProject() {
		List<String> result = queryFactory
			.select(member.username)
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	@Test
	public void tupleProjection() {
		List<Tuple> result = queryFactory
			.select(member.username, member.age)
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			Integer age = tuple.get(member.age);
			System.out.println("username = " + username);
			System.out.println("age = " + age);
		}
	}

	@Test
	public void findDtoByJPQL() {
		List<MemberDto> result = em.createQuery(
				"select new com.sparta.querydsltest.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
			.getResultList();

		for (MemberDto dto : result) {
			System.out.println("memberDto = " + dto);
		}
	}

	@Test
	public void findDtoBySetter() {
		List<MemberDto> result = queryFactory
			.select(Projections.bean(MemberDto.class,
				member.username,
				member.age))
			.from(member)
			.fetch();

		for (MemberDto dto : result) {
			System.out.println("memberDto = " + dto);
		}
	}

	@Test
	public void findDtoByField() {
		List<MemberDto> result = queryFactory
			.select(Projections.fields(MemberDto.class,
				member.username,
				member.age))
			.from(member)
			.fetch();

		for (MemberDto dto : result) {
			System.out.println("memberDto = " + dto);
		}
	}

	@Test
	public void findDtoByConstructor() {
		List<MemberDto> result = queryFactory
			.select(Projections.constructor(MemberDto.class,
				member.username,
				member.age))
			.from(member)
			.fetch();

		for (MemberDto dto : result) {
			System.out.println("memberDto = " + dto);
		}
	}

	@Test
	public void findUserDto() {
		List<UserDto> result = queryFactory
			.select(Projections.fields(UserDto.class,
				member.username.as("name"),
				member.age))
			.from(member)
			.fetch();

		for (UserDto dto : result) {
			System.out.println("userDto = " + dto);
		}
	}

	/**
	 * 나이가 가장 많은 사람의 나이 값으로 모든 User 정보 수정
	 */
	@Test
	public void findUserDtoUsingExpression() {
		QMember memberSub = new QMember("memberSub");

		List<UserDto> result = queryFactory
			.select(Projections.fields(UserDto.class,
				member.username.as("name"),

				ExpressionUtils.as(JPAExpressions
					.select(memberSub.age.max())
					.from(memberSub), "age")
			))
			.from(member)
			.fetch();

		for (UserDto dto : result) {
			System.out.println("userDto = " + dto);
		}
	}
}
