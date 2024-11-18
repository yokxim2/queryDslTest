package com.sparta.querydsltest.repository;

import static com.sparta.querydsltest.entity.QMember.*;
import static com.sparta.querydsltest.entity.QTeam.*;
import static org.springframework.util.StringUtils.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.querydsltest.dto.MemberSearchCondition;
import com.sparta.querydsltest.dto.MemberTeamDto;
import com.sparta.querydsltest.entity.Member;

import jakarta.persistence.EntityManager;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

	private final JPAQueryFactory queryFactory;

	public MemberRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
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

	@Override
	public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
		QueryResults<MemberTeamDto> results = queryFactory
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
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetchResults();

		List<MemberTeamDto> content = results.getResults();
		long total = results.getTotal();
		return new PageImpl<>(content, pageable, total);
	}

	@Override
	public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
		Pageable pageable) {
		List<MemberTeamDto> content = queryFactory
			.select(Projections.fields(MemberTeamDto.class,
				member.id,
				member.username,
				member.age,
				team.id,
				team.name))
			.from(member)
			.leftJoin(member.team, team)
			.where(usernameEq(condition.getUsername()),
				teamNameEq(condition.getTeamName()),
				ageGoe(condition.getAgeGoe()),
				ageLoe(condition.getAgeLoe()))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Member> countQuery = queryFactory
			.select(member)
			.from(member)
			.leftJoin(member.team, team)
			.where(usernameEq(condition.getUsername()),
				teamNameEq(condition.getTeamName()),
				ageGoe(condition.getAgeGoe()),
				ageLoe(condition.getAgeLoe()));

		// Count 쿼리가 생략 가능한 경우 생략해서 처리
		// 	1. 페이지 시작이면서 컨텐츠 사이즈가 페이즈 사이즈보다 작을 때
		// 	2. 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함, 더 정확히는 마지막 페이지이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때)

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
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
}
