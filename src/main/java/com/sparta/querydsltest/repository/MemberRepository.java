package com.sparta.querydsltest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.sparta.querydsltest.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom,
	QuerydslPredicateExecutor<Member> {
	// QuerydslPredicateExecutor의 한계:
	//		조인이 안된다. (묵시적 조인은 가능하지만 left join이 불가능하다.)
	//		클라이언트가 Querydsl에 의존해야 한다. 서비스 클래스가 Querydsl이라는 구현 기술에 의존해야 한다.
	// 		복잡한 실무환경에서 사용하기에는 한계가 명확하다.


	List<Member> findByUsername(String username);
}
