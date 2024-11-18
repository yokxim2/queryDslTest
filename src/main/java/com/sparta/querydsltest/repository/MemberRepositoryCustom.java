package com.sparta.querydsltest.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sparta.querydsltest.dto.MemberSearchCondition;
import com.sparta.querydsltest.dto.MemberTeamDto;

public interface MemberRepositoryCustom {
	List<MemberTeamDto> search(MemberSearchCondition condition);
	Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable	);
	Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
