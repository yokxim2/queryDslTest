package com.sparta.querydsltest.repository;

import static com.sparta.querydsltest.entity.QMember.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.querydsltest.dto.MemberSearchCondition;
import com.sparta.querydsltest.dto.MemberTeamDto;
import com.sparta.querydsltest.entity.Member;
import com.sparta.querydsltest.entity.QMember;
import com.sparta.querydsltest.entity.Team;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
	@Autowired
	EntityManager em;

	@Autowired
	MemberRepository memberRepository;

	@Test
	public void basicTest() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);

		Member findMember = memberRepository.findById(member.getId()).get();
		assertThat(findMember).isEqualTo(member);

		List<Member> result1 = memberRepository.findAll();
		assertThat(result1).containsExactly(member);

		List<Member> result2 = memberRepository.findByUsername("member1");
		assertThat(result2).containsExactly(member);
	}

	@Test
	public void querydslPredicateExecutorTest() {
		Iterable<Member> member1 = memberRepository.findAll(
			member.age.between(20, 40).and(member.username.eq("member1")));
	}
}