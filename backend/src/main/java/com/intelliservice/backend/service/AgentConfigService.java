package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.AgentConfigMapper;
import com.intelliservice.backend.model.entity.AgentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentConfigService {

    private final AgentConfigMapper agentConfigMapper;

    public List<AgentConfig> listAll() {
        return agentConfigMapper.selectAllOrdered();
    }

    public List<AgentConfig> listEnabled() {
        return agentConfigMapper.selectEnabled();
    }

    public AgentConfig getByName(String name) {
        AgentConfig config = agentConfigMapper.selectByName(name);
        if (config == null) {
            throw new RuntimeException("Agent 配置不存在: " + name);
        }
        return config;
    }

    public AgentConfig create(AgentConfig config) {
        if (agentConfigMapper.selectByName(config.getName()) != null) {
            throw new RuntimeException("Agent 名称已存在: " + config.getName());
        }
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        if (config.getEnabled() == null) config.setEnabled(1);
        if (config.getSortOrder() == null) config.setSortOrder(0);
        if (config.getModel() == null) config.setModel("deepseek-chat");
        if (config.getTemperature() == null) config.setTemperature(0.7);
        agentConfigMapper.insert(config);
        return config;
    }

    public AgentConfig update(Long id, AgentConfig config) {
        AgentConfig existing = agentConfigMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("Agent 配置不存在");
        }
        // 如果 name 变更，检查新 name 是否已被占用
        if (config.getName() != null && !config.getName().equals(existing.getName())) {
            if (agentConfigMapper.selectByName(config.getName()) != null) {
                throw new RuntimeException("Agent 名称已存在: " + config.getName());
            }
        }
        config.setId(id);
        config.setUpdatedAt(LocalDateTime.now());
        agentConfigMapper.updateById(config);
        return agentConfigMapper.selectById(id);
    }

    public void toggleEnabled(Long id, boolean enabled) {
        AgentConfig existing = agentConfigMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("Agent 配置不存在");
        }
        AgentConfig update = new AgentConfig();
        update.setId(id);
        update.setEnabled(enabled ? 1 : 0);
        update.setUpdatedAt(LocalDateTime.now());
        agentConfigMapper.updateById(update);
    }

    public void delete(Long id) {
        if (agentConfigMapper.selectById(id) == null) {
            throw new RuntimeException("Agent 配置不存在");
        }
        agentConfigMapper.deleteById(id);
    }
}
