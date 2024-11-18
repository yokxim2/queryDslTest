package com.sparta.querydsltest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.querydsltest.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	List<Member> findByUsername(String username);
}
