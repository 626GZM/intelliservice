package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.ToolConfigMapper;
import com.intelliservice.backend.model.entity.ToolConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolConfigService {

    private final ToolConfigMapper toolConfigMapper;

    public List<ToolConfig> listAll() {
        return toolConfigMapper.selectAllOrdered();
    }

    public List<ToolConfig> listEnabled() {
        return toolConfigMapper.selectEnabled();
    }

    public ToolConfig create(ToolConfig config) {
        if (toolConfigMapper.selectByName(config.getName()) != null) {
            throw new RuntimeException("工具名称已存在: " + config.getName());
        }
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        if (config.getEnabled() == null) config.setEnabled(1);
        toolConfigMapper.insert(config);
        return config;
    }

    public ToolConfig update(Long id, ToolConfig config) {
        ToolConfig existing = toolConfigMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("工具配置不存在");
        }
        if (config.getName() != null && !config.getName().equals(existing.getName())) {
            if (toolConfigMapper.selectByName(config.getName()) != null) {
                throw new RuntimeException("工具名称已存在: " + config.getName());
            }
        }
        config.setId(id);
        config.setUpdatedAt(LocalDateTime.now());
        toolConfigMapper.updateById(config);
        return toolConfigMapper.selectById(id);
    }

    public void toggleEnabled(Long id, boolean enabled) {
        if (toolConfigMapper.selectById(id) == null) {
            throw new RuntimeException("工具配置不存在");
        }
        ToolConfig update = new ToolConfig();
        update.setId(id);
        update.setEnabled(enabled ? 1 : 0);
        update.setUpdatedAt(LocalDateTime.now());
        toolConfigMapper.updateById(update);
    }

    public void delete(Long id) {
        if (toolConfigMapper.selectById(id) == null) {
            throw new RuntimeException("工具配置不存在");
        }
        toolConfigMapper.deleteById(id);
    }
}
