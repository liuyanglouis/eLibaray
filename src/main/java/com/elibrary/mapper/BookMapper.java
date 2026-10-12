package com.elibrary.mapper;

import com.elibrary.model.entity.Book;
import com.elibrary.model.dto.BookSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {

    Book selectById(Long id);

    Book selectByIsbn(String isbn);

    List<Book> selectAll();

    List<Book> selectAvailableBooks();

    List<Book> selectByAuthor(String author);

    List<Book> selectByTitle(String title);

    List<Book> selectByCategory(@Param("category") Book.Category category);

    List<Book> searchBooks(@Param("keyword") String keyword);

    List<Book> selectByCategoryPaged(@Param("category") Book.Category category,
                                     @Param("offset") int offset, @Param("limit") int limit);

    int countByCategory(@Param("category") Book.Category category);

    List<Book> searchBooksPaged(@Param("keyword") String keyword,
                                @Param("offset") int offset, @Param("limit") int limit);

    int countSearchBooks(@Param("keyword") String keyword);

    List<Book> selectAvailableBooksPaged(@Param("offset") int offset, @Param("limit") int limit);

    int countAvailableBooksTotal();

    List<Book> selectPopularBooks(@Param("limit") int limit);

    List<Book> selectNewestBooks(@Param("limit") int limit);

    List<Book> selectWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    int countAvailableBooks();

    int countAll();

    int insert(Book book);

    int update(Book book);

    int updateAvailableCopies(@Param("id") Long id, @Param("availableCopies") Integer availableCopies);

    int delete(Long id);

    int softDelete(Long id);

    boolean existsByIsbn(@Param("isbn") String isbn, @Param("excludeId") Long excludeId);

    boolean existsByIsbnSimple(@Param("isbn") String isbn);

    List<Book> selectByCriteria(BookSearchDTO criteria);

    int countByCriteria(BookSearchDTO criteria);
}