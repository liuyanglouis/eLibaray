package com.elibrary.service;

import com.elibrary.common.response.PageResult;
import com.elibrary.model.dto.BookDTO;
import com.elibrary.model.dto.BookSearchDTO;
import com.elibrary.model.dto.CreateBookDTO;
import com.elibrary.model.entity.Book;

import java.util.List;

public interface BookService {

    BookDTO getBookById(Long bookId);

    BookDTO createBook(CreateBookDTO createBookDTO);

    BookDTO updateBook(Long bookId, CreateBookDTO updateBookDTO);

    void deleteBook(Long bookId);

    PageResult<BookDTO> searchBooks(BookSearchDTO searchDTO, int page, int size);

    PageResult<BookDTO> getAvailableBooks(int page, int size);

    PageResult<BookDTO> getBooksByCategory(Book.Category category, int page, int size);

    List<BookDTO> getPopularBooks(int limit);

    List<BookDTO> getNewestBooks(int limit);

    boolean isIsbnAvailable(String isbn, Long excludeBookId);

    boolean borrowCopy(Long bookId);

    boolean returnCopy(Long bookId);

    int getAvailableCopies(Long bookId);

    boolean isBookAvailable(Long bookId);
}