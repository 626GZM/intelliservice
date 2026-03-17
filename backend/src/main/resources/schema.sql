-- 用户表
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role ENUM('user', 'admin') DEFAULT 'user',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 会话表
CREATE TABLE IF NOT EXISTS sessions (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        user_id BIGINT NOT NULL,
                                        title VARCHAR(200) DEFAULT '新对话',
    status ENUM('active', 'closed') DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        session_id BIGINT NOT NULL,
                                        role ENUM('user', 'assistant', 'system') NOT NULL,
    content TEXT NOT NULL,
    token_count INT DEFAULT 0,
    response_time_ms INT DEFAULT 0,
    agent_name VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 工单表
CREATE TABLE IF NOT EXISTS tickets (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       session_id BIGINT,
                                       title VARCHAR(200) NOT NULL,
    description TEXT,
    priority ENUM('low', 'medium', 'high') DEFAULT 'medium',
    status ENUM('open', 'processing', 'resolved', 'closed') DEFAULT 'open',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES sessions(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Agent调用日志表
CREATE TABLE IF NOT EXISTS agent_logs (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          session_id BIGINT,
                                          message_id BIGINT,
                                          agent_name VARCHAR(50),
    tool_name VARCHAR(100),
    tool_input JSON,
    tool_output JSON,
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    response_time_ms INT DEFAULT 0,
    status ENUM('success', 'error', 'timeout') DEFAULT 'success',
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(id),
    FOREIGN KEY (message_id) REFERENCES messages(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
