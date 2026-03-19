package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.PenaltyRuleMapper;
import com.intelliservice.backend.model.entity.PenaltyRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PenaltyRuleService {

    private final PenaltyRuleMapper penaltyRuleMapper;

    public List<PenaltyRule> listAll() {
        return penaltyRuleMapper.selectAllOrdered();
    }

    public PenaltyRule getByRuleCode(String ruleCode) {
        PenaltyRule rule = penaltyRuleMapper.selectByRuleCode(ruleCode);
        if (rule == null) {
            throw new RuntimeException("判罚规则不存在: " + ruleCode);
        }
        return rule;
    }
}
