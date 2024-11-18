package com.sparta.querydsltest.repository;

import java.util.List;

import com.sparta.querydsltest.dto.MemberSearchCondition;
import com.sparta.querydsltest.dto.MemberTeamDto;

public interface MemberRepositoryCustom {
	List<MemberTeamDto> search(MemberSearchCondition condition);
}
