package com.elibrary.service.impl;

import com.elibrary.common.exception.BusinessException;
import com.elibrary.common.response.PageResult;
import com.elibrary.mapper.BookMapper;
import com.elibrary.model.dto.BookDTO;
import com.elibrary.model.dto.BookSearchDTO;
import com.elibrary.model.dto.CreateBookDTO;
import com.elibrary.model.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookServiceImpl 单元测试")
class BookServiceImplTest {

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Spring Boot实战");
        sampleBook.setAuthor("王松");
        sampleBook.setIsbn("9787121301234");
        sampleBook.setPublisher("电子工业出版社");
        sampleBook.setPublicationYear(2023);
        sampleBook.setDescription("Spring Boot 框架的实战指南");
        sampleBook.setCategory(Book.Category.TECHNOLOGY);
        sampleBook.setTotalCopies(10);
        sampleBook.setAvailableCopies(8);
        sampleBook.setPageCount(350);
        sampleBook.setLanguage("Chinese");
        sampleBook.setFormat("PDF");
        sampleBook.setFileSize(1024L);
        sampleBook.setFilePath("/books/springboot.pdf");
        sampleBook.setCoverImagePath("/covers/springboot.jpg");
        sampleBook.setPrice(new BigDecimal("79.90"));
        sampleBook.setRating(new BigDecimal("4.50"));
        sampleBook.setReviewCount(20);
        sampleBook.setIsActive(true);
        sampleBook.setCreatedAt(LocalDateTime.now().minusDays(30));
        sampleBook.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== getBookById ====================

    @Nested
    @DisplayName("getBookById - 获取书籍详情")
    class GetBookByIdTest {

        @Test
        @DisplayName("书籍存在 → 返回完整DTO")
        void success() {
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            BookDTO result = bookService.getBookById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Spring Boot实战", result.getTitle());
            assertEquals("王松", result.getAuthor());
            assertEquals("9787121301234", result.getIsbn());
            assertEquals(Book.Category.TECHNOLOGY, result.getCategory());
            assertEquals(10, result.getTotalCopies());
            assertEquals(8, result.getAvailableCopies());
            verify(bookMapper).selectById(1L);
        }

        @Test
        @DisplayName("书籍不存在 → 抛出 BOOK_NOT_FOUND")
        void throwException_whenNotFound() {
            when(bookMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookService.getBookById(999L));
            assertEquals(3000, ex.getCode());
        }
    }

    // ==================== createBook ====================

    @Nested
    @DisplayName("createBook - 创建书籍")
    class CreateBookTest {

        @Test
        @DisplayName("ISBN不重复 → 创建成功，availableCopies = totalCopies")
        void success() {
            CreateBookDTO dto = buildCreateBookDTO();
            when(bookMapper.existsByIsbnSimple("9787121301234")).thenReturn(false);
            when(bookMapper.insert(any(Book.class))).thenAnswer(invocation -> {
                Book b = invocation.getArgument(0);
                b.setId(1L);
                return 1;
            });

            BookDTO result = bookService.createBook(dto);

            assertNotNull(result);
            assertEquals("Spring Boot实战", result.getTitle());
            assertEquals(10, result.getAvailableCopies());
            assertTrue(result.getIsActive());
            verify(bookMapper).insert(any(Book.class));
        }

        @Test
        @DisplayName("ISBN重复 → 抛出 DATA_DUPLICATE")
        void throwException_whenIsbnDuplicate() {
            CreateBookDTO dto = buildCreateBookDTO();
            when(bookMapper.existsByIsbnSimple("9787121301234")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookService.createBook(dto));
            assertEquals(5000, ex.getCode());

            verify(bookMapper, never()).insert(any(Book.class));
        }

        @Test
        @DisplayName("ISBN为空 → 跳过ISBN检查")
        void success_whenIsbnEmpty() {
            CreateBookDTO dto = buildCreateBookDTO();
            dto.setIsbn("");
            when(bookMapper.insert(any(Book.class))).thenReturn(1);

            BookDTO result = bookService.createBook(dto);

            assertNotNull(result);
            verify(bookMapper, never()).existsByIsbnSimple(anyString());
        }

        @Test
        @DisplayName("ISBN为null → 跳过ISBN检查")
        void success_whenIsbnNull() {
            CreateBookDTO dto = buildCreateBookDTO();
            dto.setIsbn(null);
            when(bookMapper.insert(any(Book.class))).thenReturn(1);

            BookDTO result = bookService.createBook(dto);

            assertNotNull(result);
            verify(bookMapper, never()).existsByIsbnSimple(anyString());
        }
    }

    // ==================== updateBook ====================

    @Nested
    @DisplayName("updateBook - 更新书籍")
    class UpdateBookTest {

        @Test
        @DisplayName("正常更新 → 字段全部更新")
        void success() {
            CreateBookDTO dto = buildCreateBookDTO();
            dto.setTitle("Spring Boot实战 第2版");
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.existsByIsbn("9787121301234", 1L)).thenReturn(false);
            when(bookMapper.update(any(Book.class))).thenReturn(1);

            BookDTO result = bookService.updateBook(1L, dto);

            assertEquals("Spring Boot实战 第2版", result.getTitle());
            verify(bookMapper).update(any(Book.class));
        }

        @Test
        @DisplayName("书籍不存在 → 抛出 BOOK_NOT_FOUND")
        void throwException_whenNotFound() {
            when(bookMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> bookService.updateBook(999L, buildCreateBookDTO()));
        }

        @Test
        @DisplayName("ISBN被其他书占用 → 抛出 DATA_DUPLICATE")
        void throwException_whenIsbnOccupied() {
            CreateBookDTO dto = buildCreateBookDTO();
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.existsByIsbn("9787121301234", 1L)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookService.updateBook(1L, dto));
            assertEquals(5000, ex.getCode());

            verify(bookMapper, never()).update(any(Book.class));
        }

        @Test
        @DisplayName("总副本增加 → 可用副本等量增加")
        void adjustCopies_whenTotalIncreased() {
            CreateBookDTO dto = buildCreateBookDTO();
            dto.setTotalCopies(15);
            sampleBook.setTotalCopies(10);
            sampleBook.setAvailableCopies(8);

            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.existsByIsbn(anyString(), anyLong())).thenReturn(false);
            when(bookMapper.update(any(Book.class))).thenReturn(1);

            BookDTO result = bookService.updateBook(1L, dto);

            assertEquals(15, result.getTotalCopies());
            // 借出了2本(10-8=2)，新的可用 = 15 - 2 = 13
            assertEquals(13, result.getAvailableCopies());
        }

        @Test
        @DisplayName("总副本减少且低于已借出 → 可用副本为0")
        void adjustCopies_whenTotalBelowBorrowed() {
            CreateBookDTO dto = buildCreateBookDTO();
            dto.setTotalCopies(1);
            sampleBook.setTotalCopies(10);
            sampleBook.setAvailableCopies(2);

            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.existsByIsbn(anyString(), anyLong())).thenReturn(false);
            when(bookMapper.update(any(Book.class))).thenReturn(1);

            BookDTO result = bookService.updateBook(1L, dto);

            assertEquals(1, result.getTotalCopies());
            assertEquals(0, result.getAvailableCopies());
        }

        @Test
        @DisplayName("总副本不变 → 可用副本不变")
        void noChangeCopies_whenTotalSame() {
            CreateBookDTO dto = buildCreateBookDTO();
            dto.setTotalCopies(10);
            sampleBook.setTotalCopies(10);
            sampleBook.setAvailableCopies(8);

            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.existsByIsbn(anyString(), anyLong())).thenReturn(false);
            when(bookMapper.update(any(Book.class))).thenReturn(1);

            BookDTO result = bookService.updateBook(1L, dto);

            assertEquals(10, result.getTotalCopies());
            assertEquals(8, result.getAvailableCopies());
        }

        @Test
        @DisplayName("ISBN为空 → 跳过ISBN检查")
        void success_whenIsbnEmpty() {
            CreateBookDTO dto = buildCreateBookDTO();
            dto.setIsbn("");
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.update(any(Book.class))).thenReturn(1);

            bookService.updateBook(1L, dto);

            verify(bookMapper, never()).existsByIsbn(anyString(), anyLong());
        }
    }

    // ==================== deleteBook ====================

    @Nested
    @DisplayName("deleteBook - 删除书籍")
    class DeleteBookTest {

        @Test
        @DisplayName("书籍存在 → 软删除成功")
        void success() {
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.softDelete(1L)).thenReturn(1);

            assertDoesNotThrow(() -> bookService.deleteBook(1L));
            verify(bookMapper).softDelete(1L);
        }

        @Test
        @DisplayName("书籍不存在 → 抛出 BOOK_NOT_FOUND")
        void throwException_whenNotFound() {
            when(bookMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookService.deleteBook(999L));
            assertEquals(3000, ex.getCode());
        }
    }

    // ==================== searchBooks ====================

    @Nested
    @DisplayName("searchBooks - 搜索书籍")
    class SearchBooksTest {

        @Test
        @DisplayName("有关键词有结果 → 返回分页")
        void success() {
            BookSearchDTO searchDTO = new BookSearchDTO();
            searchDTO.setTitle("Spring");

            when(bookMapper.searchBooksPaged("Spring", 0, 10)).thenReturn(List.of(sampleBook));
            when(bookMapper.countSearchBooks("Spring")).thenReturn(1);

            PageResult<BookDTO> result = bookService.searchBooks(searchDTO, 0, 10);

            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getTotal());
            assertEquals(0, result.getPage());
            assertEquals(10, result.getSize());
        }

        @Test
        @DisplayName("无结果 → 返回空分页")
        void empty() {
            BookSearchDTO searchDTO = new BookSearchDTO();
            searchDTO.setTitle("不存在的书");

            when(bookMapper.searchBooksPaged("不存在的书", 0, 10)).thenReturn(Collections.emptyList());
            when(bookMapper.countSearchBooks("不存在的书")).thenReturn(0);

            PageResult<BookDTO> result = bookService.searchBooks(searchDTO, 0, 10);

            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotal());
        }

        @Test
        @DisplayName("title为null → 用空字符串搜索")
        void nullTitle_useEmptyKeyword() {
            BookSearchDTO searchDTO = new BookSearchDTO();
            searchDTO.setTitle(null);

            when(bookMapper.searchBooksPaged("", 0, 10)).thenReturn(List.of(sampleBook));
            when(bookMapper.countSearchBooks("")).thenReturn(100);

            PageResult<BookDTO> result = bookService.searchBooks(searchDTO, 0, 10);

            assertEquals(1, result.getContent().size());
            assertEquals(100, result.getTotal());
        }

        @Test
        @DisplayName("第2页 → offset计算正确")
        void correctOffsetForPage2() {
            BookSearchDTO searchDTO = new BookSearchDTO();
            searchDTO.setTitle("Java");

            when(bookMapper.searchBooksPaged("Java", 20, 10)).thenReturn(Collections.emptyList());
            when(bookMapper.countSearchBooks("Java")).thenReturn(25);

            PageResult<BookDTO> result = bookService.searchBooks(searchDTO, 2, 10);

            verify(bookMapper).searchBooksPaged("Java", 20, 10);
            assertEquals(2, result.getPage());
        }
    }

    // ==================== getAvailableBooks ====================

    @Nested
    @DisplayName("getAvailableBooks - 可借阅书籍")
    class GetAvailableBooksTest {

        @Test
        @DisplayName("有可借书籍 → 返回分页")
        void success() {
            when(bookMapper.selectAvailableBooksPaged(0, 10)).thenReturn(List.of(sampleBook));
            when(bookMapper.countAvailableBooksTotal()).thenReturn(1);

            PageResult<BookDTO> result = bookService.getAvailableBooks(0, 10);

            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("无可借书籍 → 返回空")
        void empty() {
            when(bookMapper.selectAvailableBooksPaged(0, 10)).thenReturn(Collections.emptyList());
            when(bookMapper.countAvailableBooksTotal()).thenReturn(0);

            PageResult<BookDTO> result = bookService.getAvailableBooks(0, 10);

            assertTrue(result.getContent().isEmpty());
        }
    }

    // ==================== getBooksByCategory ====================

    @Nested
    @DisplayName("getBooksByCategory - 按分类获取")
    class GetBooksByCategoryTest {

        @Test
        @DisplayName("TECHNOLOGY分类有书 → 返回分页")
        void success() {
            when(bookMapper.selectByCategoryPaged(Book.Category.TECHNOLOGY, 0, 10))
                    .thenReturn(List.of(sampleBook));
            when(bookMapper.countByCategory(Book.Category.TECHNOLOGY)).thenReturn(1);

            PageResult<BookDTO> result = bookService.getBooksByCategory(Book.Category.TECHNOLOGY, 0, 10);

            assertEquals(1, result.getContent().size());
            assertEquals(Book.Category.TECHNOLOGY, result.getContent().get(0).getCategory());
        }

        @Test
        @DisplayName("分类无书 → 返回空")
        void empty() {
            when(bookMapper.selectByCategoryPaged(Book.Category.ART, 0, 10))
                    .thenReturn(Collections.emptyList());
            when(bookMapper.countByCategory(Book.Category.ART)).thenReturn(0);

            PageResult<BookDTO> result = bookService.getBooksByCategory(Book.Category.ART, 0, 10);

            assertTrue(result.getContent().isEmpty());
        }
    }

    // ==================== getPopularBooks ====================

    @Nested
    @DisplayName("getPopularBooks - 热门书籍")
    class GetPopularBooksTest {

        @Test
        @DisplayName("返回按评分排序的列表")
        void success() {
            when(bookMapper.selectPopularBooks(5)).thenReturn(List.of(sampleBook));

            List<BookDTO> result = bookService.getPopularBooks(5);

            assertEquals(1, result.size());
            verify(bookMapper).selectPopularBooks(5);
        }

        @Test
        @DisplayName("无数据 → 返回空")
        void empty() {
            when(bookMapper.selectPopularBooks(10)).thenReturn(Collections.emptyList());

            List<BookDTO> result = bookService.getPopularBooks(10);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== getNewestBooks ====================

    @Nested
    @DisplayName("getNewestBooks - 最新书籍")
    class GetNewestBooksTest {

        @Test
        @DisplayName("返回最新书籍列表")
        void success() {
            when(bookMapper.selectNewestBooks(5)).thenReturn(List.of(sampleBook));

            List<BookDTO> result = bookService.getNewestBooks(5);

            assertEquals(1, result.size());
            verify(bookMapper).selectNewestBooks(5);
        }
    }

    // ==================== isIsbnAvailable ====================

    @Nested
    @DisplayName("isIsbnAvailable - ISBN可用性")
    class IsIsbnAvailableTest {

        @Test
        @DisplayName("ISBN为null → 可用")
        void available_whenNull() {
            assertTrue(bookService.isIsbnAvailable(null, null));
            verifyNoInteractions(bookMapper);
        }

        @Test
        @DisplayName("ISBN为空串 → 可用")
        void available_whenEmpty() {
            assertTrue(bookService.isIsbnAvailable("", null));
            verifyNoInteractions(bookMapper);
        }

        @Test
        @DisplayName("ISBN不存在 → 可用")
        void available_whenNotExists() {
            when(bookMapper.existsByIsbnSimple("123")).thenReturn(false);

            assertTrue(bookService.isIsbnAvailable("123", null));
        }

        @Test
        @DisplayName("ISBN已存在 → 不可用")
        void notAvailable_whenExists() {
            when(bookMapper.existsByIsbnSimple("123")).thenReturn(true);

            assertFalse(bookService.isIsbnAvailable("123", null));
        }

        @Test
        @DisplayName("有excludeId → 使用排除查询")
        void useExcludeQuery_whenExcludeIdProvided() {
            when(bookMapper.existsByIsbn("123", 1L)).thenReturn(false);

            assertTrue(bookService.isIsbnAvailable("123", 1L));
            verify(bookMapper).existsByIsbn("123", 1L);
            verify(bookMapper, never()).existsByIsbnSimple(anyString());
        }

        @Test
        @DisplayName("有excludeId但ISBN被占用 → 不可用")
        void notAvailable_whenOccupiedByOther() {
            when(bookMapper.existsByIsbn("123", 1L)).thenReturn(true);

            assertFalse(bookService.isIsbnAvailable("123", 1L));
        }
    }

    // ==================== borrowCopy ====================

    @Nested
    @DisplayName("borrowCopy - 借阅副本")
    class BorrowCopyTest {

        @Test
        @DisplayName("有可用副本 → 减1，返回true")
        void success() {
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.updateAvailableCopies(1L, 7)).thenReturn(1);

            assertTrue(bookService.borrowCopy(1L));
            verify(bookMapper).updateAvailableCopies(1L, 7);
        }

        @Test
        @DisplayName("书籍不存在 → 抛出 BOOK_NOT_FOUND")
        void throwException_whenNotFound() {
            when(bookMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookService.borrowCopy(999L));
            assertEquals(3000, ex.getCode());
        }

        @Test
        @DisplayName("副本为0 → 抛出 BOOK_UNAVAILABLE")
        void throwException_whenNoCopies() {
            sampleBook.setAvailableCopies(0);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> bookService.borrowCopy(1L));
            assertEquals(3001, ex.getCode());
        }

        @Test
        @DisplayName("书籍已下架 → 抛出 BOOK_UNAVAILABLE")
        void throwException_whenInactive() {
            sampleBook.setIsActive(false);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            assertThrows(BusinessException.class,
                    () -> bookService.borrowCopy(1L));
        }
    }

    // ==================== returnCopy ====================

    @Nested
    @DisplayName("returnCopy - 归还副本")
    class ReturnCopyTest {

        @Test
        @DisplayName("正常归还 → 副本加1，返回true")
        void success() {
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.updateAvailableCopies(1L, 9)).thenReturn(1);

            assertTrue(bookService.returnCopy(1L));
            verify(bookMapper).updateAvailableCopies(1L, 9);
        }

        @Test
        @DisplayName("书籍不存在 → 抛出 BOOK_NOT_FOUND")
        void throwException_whenNotFound() {
            when(bookMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> bookService.returnCopy(999L));
        }

        @Test
        @DisplayName("副本已满 → 不超过总副本数")
        void notExceedTotalCopies() {
            sampleBook.setAvailableCopies(10);
            sampleBook.setTotalCopies(10);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(bookMapper.updateAvailableCopies(1L, 10)).thenReturn(1);

            assertTrue(bookService.returnCopy(1L));
            verify(bookMapper).updateAvailableCopies(1L, 10);
        }
    }

    // ==================== getAvailableCopies ====================

    @Nested
    @DisplayName("getAvailableCopies - 获取可用副本数")
    class GetAvailableCopiesTest {

        @Test
        @DisplayName("书籍存在 → 返回副本数")
        void success() {
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            assertEquals(8, bookService.getAvailableCopies(1L));
        }

        @Test
        @DisplayName("书籍不存在 → 抛出异常")
        void throwException_whenNotFound() {
            when(bookMapper.selectById(999L)).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> bookService.getAvailableCopies(999L));
        }
    }

    // ==================== isBookAvailable ====================

    @Nested
    @DisplayName("isBookAvailable - 检查书籍可用性")
    class IsBookAvailableTest {

        @Test
        @DisplayName("活跃且有副本 → true")
        void true_whenAvailable() {
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            assertTrue(bookService.isBookAvailable(1L));
        }

        @Test
        @DisplayName("副本为0 → false")
        void false_whenNoCopies() {
            sampleBook.setAvailableCopies(0);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            assertFalse(bookService.isBookAvailable(1L));
        }

        @Test
        @DisplayName("已下架 → false")
        void false_whenInactive() {
            sampleBook.setIsActive(false);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            assertFalse(bookService.isBookAvailable(1L));
        }

        @Test
        @DisplayName("书籍不存在 → false")
        void false_whenNotFound() {
            when(bookMapper.selectById(999L)).thenReturn(null);

            assertFalse(bookService.isBookAvailable(999L));
        }
    }

    // ==================== 辅助方法 ====================

    private CreateBookDTO buildCreateBookDTO() {
        CreateBookDTO dto = new CreateBookDTO();
        dto.setTitle("Spring Boot实战");
        dto.setAuthor("王松");
        dto.setIsbn("9787121301234");
        dto.setPublisher("电子工业出版社");
        dto.setPublicationYear(2023);
        dto.setDescription("Spring Boot 框架的实战指南");
        dto.setCategory(Book.Category.TECHNOLOGY);
        dto.setTotalCopies(10);
        dto.setPageCount(350);
        dto.setLanguage("Chinese");
        dto.setFormat("PDF");
        dto.setFileSize(1024L);
        dto.setFilePath("/books/springboot.pdf");
        dto.setCoverImagePath("/covers/springboot.jpg");
        dto.setPrice(new BigDecimal("79.90"));
        return dto;
    }
}