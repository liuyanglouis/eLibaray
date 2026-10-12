package com.elibrary.service.impl;

import com.elibrary.common.exception.BusinessException;
import com.elibrary.common.exception.ErrorCode;
import com.elibrary.common.response.PageResult;
import com.elibrary.mapper.BookMapper;
import com.elibrary.model.dto.BookDTO;
import com.elibrary.model.dto.BookSearchDTO;
import com.elibrary.model.dto.CreateBookDTO;
import com.elibrary.model.entity.Book;
import com.elibrary.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public BookDTO getBookById(Long bookId) {
        log.info("获取书籍详情, bookId: {}", bookId);

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        return convertToDTO(book);
    }

    @Override
    @Transactional
    public BookDTO createBook(CreateBookDTO createBookDTO) {
        log.info("创建新书籍, title: {}", createBookDTO.getTitle());

        if (createBookDTO.getIsbn() != null && !createBookDTO.getIsbn().isEmpty()) {
            if (bookMapper.existsByIsbnSimple(createBookDTO.getIsbn())) {
                throw new BusinessException(ErrorCode.DATA_DUPLICATE, "ISBN已存在");
            }
        }

        Book book = convertToEntity(createBookDTO);
        book.setAvailableCopies(book.getTotalCopies());
        book.setIsActive(true);
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());

        bookMapper.insert(book);
        log.info("书籍创建成功, bookId: {}, isbn: {}", book.getId(), book.getIsbn());

        return convertToDTO(book);
    }

    @Override
    @Transactional
    public BookDTO updateBook(Long bookId, CreateBookDTO updateBookDTO) {
        log.info("更新书籍信息, bookId: {}", bookId);

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        if (updateBookDTO.getIsbn() != null && !updateBookDTO.getIsbn().isEmpty()) {
            if (bookMapper.existsByIsbn(updateBookDTO.getIsbn(), bookId)) {
                throw new BusinessException(ErrorCode.DATA_DUPLICATE, "ISBN已存在");
            }
        }

        book.setTitle(updateBookDTO.getTitle());
        book.setAuthor(updateBookDTO.getAuthor());
        book.setIsbn(updateBookDTO.getIsbn());
        book.setPublisher(updateBookDTO.getPublisher());
        book.setPublicationYear(updateBookDTO.getPublicationYear());
        book.setDescription(updateBookDTO.getDescription());
        book.setCategory(updateBookDTO.getCategory());

        if (updateBookDTO.getTotalCopies() != null && !updateBookDTO.getTotalCopies().equals(book.getTotalCopies())) {
            int oldTotalCopies = book.getTotalCopies();
            int newTotalCopies = updateBookDTO.getTotalCopies();
            book.setTotalCopies(newTotalCopies);

            int borrowedCopies = oldTotalCopies - book.getAvailableCopies();
            int newAvailableCopies = Math.max(0, newTotalCopies - borrowedCopies);
            book.setAvailableCopies(newAvailableCopies);
        }

        book.setPageCount(updateBookDTO.getPageCount());
        book.setLanguage(updateBookDTO.getLanguage());
        book.setFormat(updateBookDTO.getFormat());
        book.setFileSize(updateBookDTO.getFileSize());
        book.setFilePath(updateBookDTO.getFilePath());
        book.setCoverImagePath(updateBookDTO.getCoverImagePath());
        book.setPrice(updateBookDTO.getPrice());
        book.setUpdatedAt(LocalDateTime.now());

        bookMapper.update(book);
        log.info("书籍更新成功, bookId: {}", book.getId());

        return convertToDTO(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        log.info("删除书籍, bookId: {}", bookId);

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        bookMapper.softDelete(bookId);
        log.info("书籍软删除成功, bookId: {}", bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<BookDTO> searchBooks(BookSearchDTO searchDTO, int page, int size) {
        log.info("搜索书籍, keyword: {}", searchDTO.getTitle());

        String keyword = searchDTO.getTitle() != null ? searchDTO.getTitle() : "";
        int offset = page * size;

        List<Book> books = bookMapper.searchBooksPaged(keyword, offset, size);
        int total = bookMapper.countSearchBooks(keyword);

        List<BookDTO> dtoList = books.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResult<>(dtoList, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<BookDTO> getAvailableBooks(int page, int size) {
        log.info("获取可借阅书籍列表");

        int offset = page * size;
        List<Book> books = bookMapper.selectAvailableBooksPaged(offset, size);
        int total = bookMapper.countAvailableBooksTotal();

        List<BookDTO> dtoList = books.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResult<>(dtoList, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<BookDTO> getBooksByCategory(Book.Category category, int page, int size) {
        log.info("按分类获取书籍, category: {}", category);

        int offset = page * size;
        List<Book> books = bookMapper.selectByCategoryPaged(category, offset, size);
        int total = bookMapper.countByCategory(category);

        List<BookDTO> dtoList = books.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResult<>(dtoList, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getPopularBooks(int limit) {
        log.info("获取热门书籍, limit: {}", limit);

        return bookMapper.selectPopularBooks(limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> getNewestBooks(int limit) {
        log.info("获取最新书籍, limit: {}", limit);

        return bookMapper.selectNewestBooks(limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isIsbnAvailable(String isbn, Long excludeBookId) {
        if (isbn == null || isbn.isEmpty()) {
            return true;
        }

        if (excludeBookId == null) {
            return !bookMapper.existsByIsbnSimple(isbn);
        } else {
            return !bookMapper.existsByIsbn(isbn, excludeBookId);
        }
    }

    @Override
    @Transactional
    public boolean borrowCopy(Long bookId) {
        log.info("借阅书籍副本, bookId: {}", bookId);

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        if (!book.isAvailable()) {
            throw new BusinessException(ErrorCode.BOOK_UNAVAILABLE);
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException(ErrorCode.BOOK_UNAVAILABLE, "没有可用副本");
        }

        book.borrowCopy();
        bookMapper.updateAvailableCopies(bookId, book.getAvailableCopies());

        log.info("书籍借阅成功, bookId: {}, 剩余可用副本: {}", bookId, book.getAvailableCopies());
        return true;
    }

    @Override
    @Transactional
    public boolean returnCopy(Long bookId) {
        log.info("归还书籍副本, bookId: {}", bookId);

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        book.returnCopy();
        bookMapper.updateAvailableCopies(bookId, book.getAvailableCopies());

        log.info("书籍归还成功, bookId: {}, 当前可用副本: {}", bookId, book.getAvailableCopies());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableCopies(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        return book.getAvailableCopies();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookAvailable(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        return book != null && book.isAvailable();
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setDescription(book.getDescription());
        dto.setCategory(book.getCategory());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setPageCount(book.getPageCount());
        dto.setLanguage(book.getLanguage());
        dto.setFormat(book.getFormat());
        dto.setFileSize(book.getFileSize());
        dto.setFilePath(book.getFilePath());
        dto.setCoverImagePath(book.getCoverImagePath());
        dto.setPrice(book.getPrice());
        dto.setRating(book.getRating());
        dto.setReviewCount(book.getReviewCount());
        dto.setIsActive(book.getIsActive());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        return dto;
    }

    private Book convertToEntity(CreateBookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setPublicationYear(dto.getPublicationYear());
        book.setDescription(dto.getDescription());
        book.setCategory(dto.getCategory());
        book.setTotalCopies(dto.getTotalCopies());
        book.setPageCount(dto.getPageCount());
        book.setLanguage(dto.getLanguage());
        book.setFormat(dto.getFormat());
        book.setFileSize(dto.getFileSize());
        book.setFilePath(dto.getFilePath());
        book.setCoverImagePath(dto.getCoverImagePath());
        book.setPrice(dto.getPrice());
        return book;
    }
}