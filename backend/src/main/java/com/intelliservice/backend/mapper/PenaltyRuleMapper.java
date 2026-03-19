package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.PenaltyRule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PenaltyRuleMapper extends BaseMapper<PenaltyRule> {

    @Select("SELECT * FROM penalty_rules ORDER BY category, rule_code")
    List<PenaltyRule> selectAllOrdered();

    @Select("SELECT * FROM penalty_rules WHERE rule_code = #{ruleCode} LIMIT 1")
    PenaltyRule selectByRuleCode(String ruleCode);
}
