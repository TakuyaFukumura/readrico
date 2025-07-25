-- 読書記録
CREATE TABLE IF NOT EXISTS reading_record (
    id BIGINT AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    reading_status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    current_page INTEGER DEFAULT 0,
    total_pages INTEGER,
    summary TEXT,
    thoughts TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
