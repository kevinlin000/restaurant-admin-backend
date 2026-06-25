CREATE TABLE IF NOT EXISTS faq_search_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    query_text VARCHAR(255) NOT NULL,
    normalized_query VARCHAR(255) NOT NULL,
    matched BOOLEAN NOT NULL DEFAULT FALSE,
    result_count INT NOT NULL DEFAULT 0,
    top_faq_id BIGINT NULL,
    top_question VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_faq_search_log_created_at (created_at),
    INDEX idx_faq_search_log_matched (matched),
    INDEX idx_faq_search_log_normalized_query (normalized_query),
    INDEX idx_faq_search_log_top_faq (top_faq_id)
);
