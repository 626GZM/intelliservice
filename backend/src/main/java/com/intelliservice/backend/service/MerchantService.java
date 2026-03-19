package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.MerchantMapper;
import com.intelliservice.backend.model.entity.Merchant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;

    public Merchant getById(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new RuntimeException("商户不存在");
        }
        return merchant;
    }

    public List<Merchant> listAll() {
        return merchantMapper.selectList(null);
    }

    /** 执行判罚：扣分 + 累加罚款 + 更新状态 */
    public void applyPenalty(Long merchantId, int deductPoints, double fineAmount) {
        if (merchantMapper.selectById(merchantId) == null) {
            throw new RuntimeException("商户不存在");
        }
        merchantMapper.applyPenalty(merchantId, deductPoints, fineAmount);
    }
}
