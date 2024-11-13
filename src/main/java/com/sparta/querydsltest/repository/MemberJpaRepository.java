package com.sparta.querydsltest.repository;

import static com.sparta.querydsltest.entity.QMember.*;
import static com.sparta.querydsltest.entity.QTeam.*;
import static org.springframework.util.StringUtils.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.querydsltest.dto.MemberSearchCondition;
import com.sparta.querydsltest.dto.MemberTeamDto;
import com.sparta.querydsltest.entity.Member;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	public void save(Member member) {
		em.persist(member);
	}

	public Optional<Member> findById(Long id) {
		Member findMember = em.find(Member.class, id);
		return Optional.ofNullable(findMember);
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class).getResultList();
	}

	public List<Member> findAll_Querydsl() {
		return queryFactory
			.selectFrom(member)
			.fetch();
	}

	public List<Member> findByUsername(String username) {
		return em.createQuery("select m from Member m where m.username = :username", Member.class)
			.setParameter("username", username)
			.getResultList();
	}

	public List<Member> findByUsername_Querydsl(String username) {
		return queryFactory
			.selectFrom(member)
			.where(member.username.eq(username))
			.fetch();
	}

	// Builder를 사용한 동적 쿼리 생성
	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
		BooleanBuilder builder = new BooleanBuilder();
		if (hasText(condition.getUsername())) {
			builder.and(member.username.eq(condition.getUsername()));
		}
		if (hasText(condition.getTeamName())) {
			builder.and(team.name.eq(condition.getTeamName()));
		}
		if (condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}
		if (condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}

		return queryFactory
			.select(Projections.fields(MemberTeamDto.class,
				member.id,
				member.username,
				member.age,
				team.id.as("teamId"),
				team.name.as("teamName")))
			.from(member)
			.leftJoin(member.team, team)
			.where(builder)
			.fetch();
	}

	// where절 파라미터를 사용한 동적 쿼리 생성
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return queryFactory
			.select(Projections.fields(MemberTeamDto.class,
				member.id.as("memberId"),
				member.username,
				member.age,
				team.id.as("teamId"),
				team.name.as("teamName")))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				// 동적 쿼리들
				usernameEq(condition.getUsername()),
				teamNameEq(condition.getTeamName()),
				ageGoe(condition.getAgeGoe()),
				ageLoe(condition.getAgeLoe())
			)
			.fetch();
	}

	// Predicate보다 BooleanExpression을 사용하면 조건절 조립이 가능하다.
	private BooleanExpression usernameEq(String username) {
		return hasText(username) ? member.username.eq(username) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return hasText(teamName) ? team.name.eq(teamName) : null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}

	public List<Member> searchMember(MemberSearchCondition condition) {
		return queryFactory
			.selectFrom(member)
			.leftJoin(member.team, team)
			.where(
				usernameEq(condition.getUsername()),
				teamNameEq(condition.getTeamName()),
				ageBetween(condition.getAgeGoe(), condition.getAgeLoe())
			)
			.fetch();
	}

	private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
		return ageLoe(ageLoe).and(ageGoe(ageGoe));
	}
}
