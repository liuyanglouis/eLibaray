-- 电子图书馆数据库初始化脚本
-- 版本：1.0.0

-- 创建数据库
-- CREATE DATABASE elibrary;
-- \c elibrary;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    max_borrow_count INTEGER DEFAULT 5,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建书籍表
CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(200) NOT NULL,
    isbn VARCHAR(50),
    publisher VARCHAR(100),
    publication_year INTEGER,
    description TEXT,
    category VARCHAR(50),
    total_copies INTEGER DEFAULT 1,
    available_copies INTEGER DEFAULT 1,
    page_count INTEGER,
    language VARCHAR(20) DEFAULT 'Chinese',
    format VARCHAR(20) DEFAULT 'PDF',
    file_size BIGINT,
    file_path VARCHAR(500),
    cover_image_path VARCHAR(500),
    price DECIMAL(10, 2),
    rating DECIMAL(3, 2),
    review_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建借阅记录表
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'BORROWED',
    renew_count INTEGER DEFAULT 0,
    max_renew_count INTEGER DEFAULT 2,
    overdue_days INTEGER DEFAULT 0,
    fine_amount DECIMAL(10, 2) DEFAULT 0,
    is_fine_paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);

CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_category ON books(category);
CREATE INDEX idx_books_active ON books(is_active);
CREATE INDEX idx_books_available ON books(is_active, available_copies);

CREATE INDEX idx_borrow_records_user ON borrow_records(user_id);
CREATE INDEX idx_borrow_records_book ON borrow_records(book_id);
CREATE INDEX idx_borrow_records_status ON borrow_records(status);
CREATE INDEX idx_borrow_records_due_date ON borrow_records(due_date);
CREATE INDEX idx_borrow_records_user_book_status ON borrow_records(user_id, book_id, status);

-- 创建函数和触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为所有表创建触发器
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_books_updated_at BEFORE UPDATE ON books
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_borrow_records_updated_at BEFORE UPDATE ON borrow_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 插入测试数据
-- 插入测试用户（密码均为 123456，BCrypt 加密）
INSERT INTO users (username, password, email, full_name, role) VALUES
('admin', '$2a$10$IDo1dyMZ64afuPgngoHV7OfSvhKLCxVu8HYHqPhBi.v4.dcYuraoW', 'admin@elibrary.com', '系统管理员', 'ADMIN'),
('user1', '$2a$10$IDo1dyMZ64afuPgngoHV7OfSvhKLCxVu8HYHqPhBi.v4.dcYuraoW', 'user1@elibrary.com', '测试用户一', 'USER'),
('user2', '$2a$10$IDo1dyMZ64afuPgngoHV7OfSvhKLCxVu8HYHqPhBi.v4.dcYuraoW', 'user2@elibrary.com', '测试用户二', 'USER')
;

-- 插入测试书籍
INSERT INTO books (title, author, isbn, publisher, publication_year, description, category, total_copies, available_copies, page_count, language, price, rating) VALUES
('Spring Boot实战', '王松', '9787121301234', '电子工业出版社', 2023, 'Spring Boot 框架的实战指南', 'TECHNOLOGY', 10, 10, 350, 'Chinese', 79.90, 4.5),
('Java编程思想', 'Bruce Eckel', '9787111213826', '机械工业出版社', 2022, 'Java编程经典书籍', 'TECHNOLOGY', 5, 5, 800, 'Chinese', 89.90, 4.8),
('百年孤独', '加西亚·马尔克斯', '9787544253994', '南海出版公司', 2017, '魔幻现实主义文学代表作', 'FICTION', 8, 8, 360, 'Chinese', 39.90, 4.7),
('人类简史', '尤瓦尔·赫拉利', '9787508672069', '中信出版社', 2021, '从石器时代到二十一世纪的人类发展史', 'HISTORY', 6, 6, 450, 'Chinese', 68.00, 4.6),
('Python编程：从入门到实践', 'Eric Matthes', '9787115428028', '人民邮电出版社', 2023, 'Python编程入门书籍', 'TECHNOLOGY', 12, 12, 500, 'Chinese', 89.00, 4.4)
;

-- 插入测试借阅记录
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) VALUES
(2, 1, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '20 days', 'BORROWED'),
(2, 2, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP + INTERVAL '15 days', 'BORROWED'),
(3, 3, CURRENT_TIMESTAMP - INTERVAL '40 days', CURRENT_TIMESTAMP - INTERVAL '10 days', 'BORROWED'),
(3, 4, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '25 days', 'BORROWED')
;

-- 创建视图
CREATE VIEW available_books_view AS
SELECT
    b.id,
    b.title,
    b.author,
    b.isbn,
    b.category,
    b.total_copies,
    b.available_copies,
    b.rating,
    b.created_at
FROM books b
WHERE b.is_active = TRUE
AND b.available_copies > 0;

CREATE VIEW user_borrow_summary_view AS
SELECT
    u.id AS user_id,
    u.username,
    u.full_name,
    u.email,
    COUNT(CASE WHEN br.status = 'BORROWED' THEN 1 END) AS active_borrows,
    COUNT(CASE WHEN br.status = 'OVERDUE' THEN 1 END) AS overdue_borrows,
    COUNT(CASE WHEN br.status = 'RETURNED' THEN 1 END) AS returned_borrows
FROM users u
LEFT JOIN borrow_records br ON u.id = br.user_id
WHERE u.is_active = TRUE
GROUP BY u.id, u.username, u.full_name, u.email;

-- 创建存储过程：检查逾期借阅
CREATE OR REPLACE PROCEDURE check_overdue_borrows()
LANGUAGE plpgsql
AS $$
BEGIN
    -- 更新已逾期的借阅记录状态
    UPDATE borrow_records
    SET
        status = 'OVERDUE',
        updated_at = CURRENT_TIMESTAMP
    WHERE status = 'BORROWED'
    AND due_date < CURRENT_TIMESTAMP
    AND return_date IS NULL;

    -- 记录日志
    RAISE NOTICE '逾期借阅检查完成于 %', CURRENT_TIMESTAMP;
END;
$$;

COMMENT ON TABLE users IS '用户信息表';
COMMENT ON TABLE books IS '书籍信息表';
COMMENT ON TABLE borrow_records IS '借阅记录表';
COMMENT ON VIEW available_books_view IS '可用书籍视图';
COMMENT ON VIEW user_borrow_summary_view IS '用户借阅汇总视图';
COMMENT ON PROCEDURE check_overdue_borrows() IS '检查逾期借阅的存储过程';