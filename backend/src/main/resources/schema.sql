-- ============================================================
-- IntelliService 商城AI客服 — 完整建表脚本（幂等，可重复执行）
-- ============================================================

-- 初始化管理员账号（密码 admin123，BCrypt 加密）
-- INSERT IGNORE INTO users (username, password, email, role)
-- VALUES ('admin', '$2a$10$ZXCo6uDV/Y8a2rKnv2/WnO1xFkHvjszPkn9m4rAfY4l.WJhxzDXR2', 'admin@intelliservice.com', 'admin');

-- ——————————————————————————————————————————————————
-- 1. 用户表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100),
    role        ENUM('user', 'admin', 'agent') DEFAULT 'user',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ——————————————————————————————————————————————————
-- 2. 商户表（被判罚的对象）
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS merchants (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    score        INT DEFAULT 100 COMMENT '商户评分，满分100',
    total_fines  DECIMAL(10,2) DEFAULT 0 COMMENT '累计罚款',
    status       ENUM('normal', 'warning', 'suspended') DEFAULT 'normal',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO merchants (id, name, score) VALUES
(1, '智选官方旗舰店', 100),
(2, '数码配件专营店', 98),
(3, '手机壳工厂直销', 95);

-- ——————————————————————————————————————————————————
-- 3. 商品表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS products (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(200) NOT NULL,
    category       VARCHAR(100),
    price          DECIMAL(10,2),
    original_price DECIMAL(10,2) COMMENT '划线原价',
    description    TEXT,
    image_url      VARCHAR(500),
    sales_count    INT DEFAULT 0 COMMENT '销量',
    stock          INT DEFAULT 0,
    merchant_id    BIGINT,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO products (id, name, category, price, original_price, description, image_url, sales_count, merchant_id, stock) VALUES
(1, 'iPhone 15 Pro 磨砂钛合金手机壳', '手机壳', 89.00, 129.00, '采用钛合金材质，完美贴合iPhone 15 Pro，四角加固防摔', 'https://picsum.photos/seed/case1/400/400', 3421, 3, 500),
(2, 'Samsung S24 透明硬壳', '手机壳', 49.00, 79.00, '超薄透明设计，展现手机原色，精准开孔', 'https://picsum.photos/seed/case2/400/400', 1892, 3, 300),
(3, '华为 Mate60 素皮手机壳', '手机壳', 99.00, 149.00, '环保素皮材质，防指纹防油污，精致缝线工艺', 'https://picsum.photos/seed/case3/400/400', 765, 3, 200),
(4, '60W 氮化镓快充充电器（单口）', '充电器', 69.00, 99.00, '氮化镓技术，体积缩小50%，支持PD3.0快充', 'https://picsum.photos/seed/charger1/400/400', 5678, 2, 150),
(5, '100W 三口充电器（2C1A）', '充电器', 129.00, 179.00, '三口同充，智能分配功率，适配多设备', 'https://picsum.photos/seed/charger2/400/400', 2341, 2, 100),
(6, 'MagSafe 磁吸无线充电板', '充电器', 159.00, 219.00, '支持15W MagSafe快速无线充电，精准磁吸定位', 'https://picsum.photos/seed/charger3/400/400', 987, 2, 80),
(7, 'Type-C to Lightning 编织数据线 1m', '配件', 39.00, 59.00, '尼龙编织材质，1米长度，支持快充数据传输', 'https://picsum.photos/seed/cable1/400/400', 8765, 1, 1000),
(8, '钢化膜 iPhone 15 系列（2片装）', '配件', 35.00, 55.00, '9H硬度，防指纹涂层，一键贴膜神器套装', 'https://picsum.photos/seed/glass1/400/400', 6543, 1, 500),
(9, '蓝牙耳机收纳包', '配件', 29.00, 49.00, '防水耐磨材质，适配多款蓝牙耳机盒', 'https://picsum.photos/seed/bag1/400/400', 4321, 1, 300);

-- ——————————————————————————————————————————————————
-- 4. 会话表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS sessions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    title           VARCHAR(200) DEFAULT '新对话',
    status          ENUM('active', 'closed') DEFAULT 'active',
    context_type    ENUM('general', 'product', 'order') DEFAULT 'general' COMMENT '对话上下文类型',
    context_id      VARCHAR(50) COMMENT '关联的productId或orderNo',
    transfer_status ENUM('ai', 'waiting', 'serving', 'closed') DEFAULT 'ai' COMMENT 'ai=AI处理中',
    agent_id        BIGINT COMMENT '接手的客服ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ——————————————————————————————————————————————————
-- 5. 消息表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS messages (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id       BIGINT NOT NULL,
    role             ENUM('user', 'assistant', 'system') NOT NULL,
    content          TEXT NOT NULL,
    token_count      INT DEFAULT 0,
    response_time_ms INT DEFAULT 0,
    agent_name       VARCHAR(50),
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ——————————————————————————————————————————————————
-- 6. 订单表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no         VARCHAR(50) NOT NULL UNIQUE,
    user_id          BIGINT NOT NULL,
    product_id       BIGINT NOT NULL,
    product_name     VARCHAR(200) COMMENT '商品名称快照',
    merchant_id      BIGINT NOT NULL DEFAULT 1,
    quantity         INT DEFAULT 1,
    total_amount     DECIMAL(10,2),
    shipping_address VARCHAR(500),
    status           ENUM('pending', 'paid', 'shipped', 'delivered', 'cancelled', 'refunding', 'refunded', 'completed') DEFAULT 'paid',
    courier          VARCHAR(50),
    courier_no       VARCHAR(100),
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ——————————————————————————————————————————————————
-- 7. 工单表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS tickets (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    session_id     BIGINT,
    order_no       VARCHAR(50) COMMENT '关联订单号',
    merchant_id    BIGINT COMMENT '涉诉商户',
    title          VARCHAR(200) NOT NULL,
    description    TEXT,
    priority       ENUM('low', 'medium', 'high') DEFAULT 'medium',
    status         ENUM('open', 'processing', 'resolved', 'closed') DEFAULT 'open',
    ai_suggestion  JSON COMMENT 'AI建议的处罚方案',
    final_penalty  JSON COMMENT '最终执行的处罚',
    review_status  ENUM('pending', 'ai_suggested', 'reviewed', 'executed', 'rejected') DEFAULT 'pending',
    reviewed_by    BIGINT COMMENT '审阅客服ID',
    reviewed_at    DATETIME,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at    DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES sessions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ——————————————————————————————————————————————————
-- 8. Agent调用日志表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS agent_logs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id        BIGINT,
    message_id        BIGINT,
    agent_name        VARCHAR(50),
    tool_name         VARCHAR(100),
    tool_input        JSON,
    tool_output       JSON,
    prompt_tokens     INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    response_time_ms  INT DEFAULT 0,
    status            ENUM('success', 'error', 'timeout') DEFAULT 'success',
    error_message     TEXT,
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(id),
    FOREIGN KEY (message_id) REFERENCES messages(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ——————————————————————————————————————————————————
-- 9. 判罚规则表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS penalty_rules (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_code     VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码如QUALITY_001',
    category      VARCHAR(50) NOT NULL COMMENT '分类：quality/delivery/service/fraud',
    description   VARCHAR(500) NOT NULL COMMENT '规则描述',
    deduct_points INT DEFAULT 0 COMMENT '扣分',
    fine_amount   DECIMAL(10,2) DEFAULT 0 COMMENT '罚款金额',
    severity      ENUM('minor', 'moderate', 'severe') DEFAULT 'moderate',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO penalty_rules (rule_code, category, description, deduct_points, fine_amount, severity) VALUES
('QUALITY_001', 'quality',  '商品与描述严重不符',                  12, 500.00, 'severe'),
('QUALITY_002', 'quality',  '商品存在质量缺陷（非人为损坏）',      6,  200.00, 'moderate'),
('QUALITY_003', 'quality',  '商品包装破损但不影响使用',            3,  0.00,   'minor'),
('DELIVERY_001','delivery', '超时发货（超过承诺时间48小时以上）',  4,  100.00, 'moderate'),
('DELIVERY_002','delivery', '发错商品',                            8,  300.00, 'severe'),
('DELIVERY_003','delivery', '快递丢件',                            10, 500.00, 'severe'),
('SERVICE_001', 'service',  '客服态度恶劣',                        6,  200.00, 'moderate'),
('SERVICE_002', 'service',  '拒绝履行七天无理由退换',              10, 500.00, 'severe'),
('SERVICE_003', 'service',  '未在24小时内响应售后请求',            3,  100.00, 'minor'),
('FRAUD_001',   'fraud',    '虚假发货（上传假物流单号）',           24, 2000.00,'severe'),
('FRAUD_002',   'fraud',    '售卖假冒伪劣商品',                    48, 5000.00,'severe');

-- ——————————————————————————————————————————————————
-- 10. 满意度评价表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS ratings (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    rating     ENUM('good', 'neutral', 'bad') DEFAULT 'good',
    comment    VARCHAR(500),
    auto_rated TINYINT(1) DEFAULT 0 COMMENT '是否超时自动好评',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ——————————————————————————————————————————————————
-- 11. Agent配置表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS agent_configs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Agent标识',
    display_name VARCHAR(100) COMMENT '显示名称',
    description  VARCHAR(500) COMMENT 'Agent描述',
    system_prompt TEXT NOT NULL COMMENT 'System Prompt',
    agent_type   ENUM('knowledge', 'order', 'ticket', 'custom') NOT NULL,
    tools        JSON COMMENT '可用工具列表',
    model        VARCHAR(50) DEFAULT 'deepseek-chat',
    temperature  DOUBLE DEFAULT 0.7,
    enabled      TINYINT(1) DEFAULT 1,
    sort_order   INT DEFAULT 0,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO agent_configs (name, display_name, description, system_prompt, agent_type, tools, enabled, sort_order) VALUES
('knowledge_agent', '知识问答助手', '处理产品咨询、退换货政策等问题',
 '你是产品知识专家。使用search_knowledge工具查询信息后回答用户。只基于工具返回的内容回答，不要编造信息。如果信息不足请说明。',
 'knowledge', '["search_knowledge","query_product","search_products","recommend_similar"]', 1, 1),
('order_agent', '订单查询助手', '处理订单查询、物流跟踪等',
 '你是订单助手。使用query_order工具查询订单信息后回答用户。如果查询不到，建议用户核实订单号。',
 'order', '["query_order","query_customer"]', 1, 2),
('ticket_agent', '工单处理助手', '处理投诉、建议，自动创建工单并建议判罚',
 '你是工单助手。用户投诉时先创建工单，再使用suggest_penalty工具分析投诉内容建议判罚方案。优先级：一般问题medium，紧急问题high。',
 'ticket', '["create_ticket","suggest_penalty","get_penalty_rules"]', 1, 3);

-- ——————————————————————————————————————————————————
-- 12. 工具配置表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS tool_configs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50)  NOT NULL UNIQUE COMMENT '工具标识',
    display_name VARCHAR(100) COMMENT '显示名称',
    description  VARCHAR(500) NOT NULL COMMENT '工具描述（LLM看到的）',
    tool_type    ENUM('mcp', 'http', 'rag') NOT NULL COMMENT '工具类型',
    config       JSON NOT NULL COMMENT '工具配置',
    enabled      TINYINT(1) DEFAULT 1,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO tool_configs (name, display_name, description, tool_type, config) VALUES
('search_knowledge',  '知识库搜索',   '从企业知识库搜索产品相关信息，包括退换货政策、会员权益、产品价格等', 'rag', '{"collection":"default"}'),
('query_order',       '订单查询',     '根据订单号查询订单状态和物流信息',                                'mcp', '{"mcp_tool_name":"query_order"}'),
('query_customer',    '客户查询',     '查询客户信息和会员等级',                                          'mcp', '{"mcp_tool_name":"query_customer"}'),
('create_ticket',     '创建工单',     '创建客服工单，需要user_id、title、description和priority参数',     'mcp', '{"mcp_tool_name":"create_ticket"}'),
('query_product',     '商品查询',     '根据商品ID查询商品详情，包括名称、价格、库存',                    'mcp', '{"mcp_tool_name":"query_product"}'),
('search_products',   '商品搜索',     '根据关键词搜索商品，返回匹配的商品列表',                          'mcp', '{"mcp_tool_name":"search_products"}'),
('recommend_similar', '相似商品推荐', '根据商品ID推荐同类商品，最多返回5个',                             'mcp', '{"mcp_tool_name":"recommend_similar"}'),
('get_penalty_rules', '获取判罚规则', '获取所有判罚规则列表，用于分析投诉时参考',                        'mcp', '{"mcp_tool_name":"get_penalty_rules"}'),
('suggest_penalty',   'AI判罚建议',   '根据ticket_id和投诉描述，AI分析并生成处罚建议，保存到工单',       'mcp', '{"mcp_tool_name":"suggest_penalty"}');

-- ——————————————————————————————————————————————————
-- 13. 知识库文档表
-- ——————————————————————————————————————————————————
CREATE TABLE IF NOT EXISTS knowledge_documents (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    filename    VARCHAR(200) NOT NULL,
    file_path   VARCHAR(500) NOT NULL,
    file_size   BIGINT DEFAULT 0,
    status      ENUM('processing', 'ready', 'failed') DEFAULT 'processing',
    chunk_count INT DEFAULT 0,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 以下 ALTER TABLE 仅针对已存在旧结构的数据库，全新部署可忽略
-- ============================================================
-- ALTER TABLE users MODIFY COLUMN role ENUM('user', 'admin', 'agent') DEFAULT 'user';
-- ALTER TABLE sessions ADD COLUMN context_type ENUM('general','product','order') DEFAULT 'general' COMMENT '对话上下文类型';
-- ALTER TABLE sessions ADD COLUMN context_id VARCHAR(50) COMMENT '关联productId或orderNo';
-- ALTER TABLE sessions ADD COLUMN transfer_status ENUM('ai','waiting','serving','closed') DEFAULT 'ai';
-- ALTER TABLE sessions ADD COLUMN agent_id BIGINT COMMENT '接手的客服ID';
-- ALTER TABLE tickets ADD COLUMN order_no VARCHAR(50) AFTER session_id;
-- ALTER TABLE tickets ADD COLUMN merchant_id BIGINT AFTER order_no;
-- ALTER TABLE tickets ADD COLUMN ai_suggestion JSON COMMENT 'AI建议的处罚方案';
-- ALTER TABLE tickets ADD COLUMN final_penalty JSON COMMENT '最终执行的处罚';
-- ALTER TABLE tickets ADD COLUMN review_status ENUM('pending','ai_suggested','reviewed','executed','rejected') DEFAULT 'pending';
-- ALTER TABLE tickets ADD COLUMN reviewed_by BIGINT COMMENT '审阅客服ID';
-- ALTER TABLE tickets ADD COLUMN reviewed_at DATETIME;
-- ALTER TABLE orders ADD COLUMN merchant_id BIGINT DEFAULT 1 AFTER product_id;
-- ALTER TABLE orders ADD COLUMN product_name VARCHAR(200) COMMENT '商品名称快照' AFTER product_id;
-- ALTER TABLE products ADD COLUMN original_price DECIMAL(10,2) COMMENT '划线原价' AFTER price;
-- ALTER TABLE products ADD COLUMN sales_count INT DEFAULT 0 COMMENT '销量' AFTER image_url;
