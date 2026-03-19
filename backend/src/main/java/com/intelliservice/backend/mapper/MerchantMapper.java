package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.Merchant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface MerchantMapper extends BaseMapper<Merchant> {

    @Update("UPDATE merchants SET score = GREATEST(0, score - #{deductPoints}), " +
            "total_fines = total_fines + #{fineAmount}, " +
            "status = CASE WHEN score - #{deductPoints} <= 60 THEN 'suspended' " +
            "             WHEN score - #{deductPoints} <= 80 THEN 'warning' " +
            "             ELSE 'normal' END " +
            "WHERE id = #{merchantId}")
    int applyPenalty(@Param("merchantId") Long merchantId,
                     @Param("deductPoints") int deductPoints,
                     @Param("fineAmount") double fineAmount);
}
