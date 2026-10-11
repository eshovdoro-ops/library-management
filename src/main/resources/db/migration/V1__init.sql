CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'LIBRARIAN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE books (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    isbn             VARCHAR(20) UNIQUE,
    publication_year INTEGER,
    total_copies     INTEGER NOT NULL CHECK (total_copies >= 0),
    available_copies INTEGER NOT NULL CHECK (available_copies >= 0 AND available_copies <= total_copies)
);

CREATE TABLE readers (
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone         VARCHAR(30),
    registered_at DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE loans (
    id          BIGSERIAL PRIMARY KEY,
    book_id     BIGINT NOT NULL REFERENCES books(id),
    reader_id   BIGINT NOT NULL REFERENCES readers(id),
    issued_at   DATE NOT NULL,
    due_date    DATE NOT NULL,
    returned_at DATE,
    CONSTRAINT chk_loan_dates CHECK (due_date >= issued_at)
);

CREATE INDEX idx_books_title        ON books (lower(title));
CREATE INDEX idx_books_author       ON books (lower(author));
CREATE INDEX idx_loans_reader       ON loans (reader_id);
CREATE INDEX idx_loans_book         ON loans (book_id);
CREATE INDEX idx_loans_open_due     ON loans (due_date) WHERE returned_at IS NULL;
