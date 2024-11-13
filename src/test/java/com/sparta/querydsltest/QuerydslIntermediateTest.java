package com.sparta.querydsltest;

import static com.sparta.querydsltest.entity.QMember.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
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

	@Test
	public void dynamicQuery_BooleanBuilder() {
		String usernameParam = "member1";
		Integer ageParam = null;

		List<Member> result = searchMember1(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember1(String usernameCond, Integer ageCond) {
		BooleanBuilder builder = new BooleanBuilder();
		if (usernameCond != null) {
			builder.and(member.username.eq(usernameCond));
		}

		if (ageCond != null) {
			builder.and(member.age.eq(ageCond));
		}

		return queryFactory
			.selectFrom(member)
			.where(builder)
			.fetch();
	}

	@Test
	public void dynamicQuery_WhereParam() {
		String usernameParam = "member1";
		Integer ageParam = null;

		List<Member> result = searchMember2(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember2(String usernameCond, Integer ageCond) {
		return queryFactory
			.selectFrom(member)
			.where(allEq(usernameCond, ageCond))
			.fetch();
	}

	private BooleanExpression usernameEq(String usernameCond) {
		return usernameCond != null ? member.username.eq(usernameCond) : null;
	}

	private BooleanExpression ageEq(Integer ageCond) {
		return ageCond != null ? member.age.eq(ageCond) : null;
	}

	// 조립 가능
	private BooleanExpression allEq(String usernameCond, Integer ageCond) {
		return usernameEq(usernameCond).and(ageEq(ageCond));
	}

	/**
	 * 벌크 연산 시 주의점
	 * update를 수행한 후에는 항상 em.flush(), em.clear()를 습관화하자. JPQL 배치와 마찬가지로,
	 * 영속성 컨텍스트에 있는 엔티티를 무시하고 실행되기 때문에 배치 쿼리를 실행하고 나면 영속성
	 * 컨텍스트를 초기화 하는 것이 안전하다.
	 */
	@Test
	public void bulkUpdate() {

		// member1 = 10 -> 비회원
		// member2 = 20 -> 비회원
		// member3 = 30 -> 유지
		// member4 = 40 -> 유지

		long count = queryFactory
			.update(member)
			.set(member.username, "비회원")
			.where(member.age.lt(28))
			.execute();

		// em.flush();
		// em.clear();

		List<Member> result = queryFactory
			.selectFrom(member)
			.fetch();

		for (Member member1 : result) {
			System.out.println("member1 = " + member1);
		}
	}

	@Test
	public void bulkAdd() {
		long count = queryFactory
			.update(member)
			.set(member.age, member.age.add(1))
			.execute();
	}

	@Test
	public void bulkDelete() {
		long count = queryFactory
			.delete(member)
			.where(member.age.gt(18))
			.execute();
	}

	@Test
	public void sqlFunction() {
		List<String> result = queryFactory
			.select(Expressions.stringTemplate(
				"function('replace', {0}, {1}, {2})",
				member.username, "member", "M"
			))
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	@Test
	public void sqlFunction2() {
		List<String> result = queryFactory
			.select(member.username)
			.from(member)
			// .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
			.where(member.username.eq(member.username.lower()))
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}
}
