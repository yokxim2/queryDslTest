package com.sparta.querydsltest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.querydsltest.dto.MemberSearchCondition;
import com.sparta.querydsltest.dto.MemberTeamDto;
import com.sparta.querydsltest.repository.MemberJpaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberController {

	private final MemberJpaRepository memberJpaRepository;

	@GetMapping("/v1/members")
	public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
		return memberJpaRepository.search(condition);
	}
}
