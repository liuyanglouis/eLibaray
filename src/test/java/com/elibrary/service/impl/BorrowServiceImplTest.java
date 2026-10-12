package com.elibrary.service.impl;

import com.elibrary.common.exception.BusinessException;
import com.elibrary.common.response.PageResult;
import com.elibrary.mapper.BookMapper;
import com.elibrary.mapper.BorrowRecordMapper;
import com.elibrary.mapper.UserMapper;
import com.elibrary.model.dto.BorrowRecordDTO;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.BorrowRecord;
import com.elibrary.model.entity.User;
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
@DisplayName("BorrowServiceImpl 单元测试")
class BorrowServiceImplTest {

    @Mock
    private BorrowRecordMapper borrowRecordMapper;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private User sampleUser;
    private Book sampleBook;
    private BorrowRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@elibrary.com");
        sampleUser.setFullName("测试用户");
        sampleUser.setRole(User.Role.USER);
        sampleUser.setIsActive(true);

        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Spring Boot实战");
        sampleBook.setAuthor("王松");
        sampleBook.setIsbn("9787121301234");
        sampleBook.setTotalCopies(10);
        sampleBook.setAvailableCopies(8);
        sampleBook.setIsActive(true);

        sampleRecord = new BorrowRecord();
        sampleRecord.setId(1L);
        sampleRecord.setUser(sampleUser);
        sampleRecord.setBook(sampleBook);
        sampleRecord.setBorrowDate(LocalDateTime.now().minusDays(5));
        sampleRecord.setDueDate(LocalDateTime.now().plusDays(25));
        sampleRecord.setStatus(BorrowRecord.Status.BORROWED);
        sampleRecord.setRenewCount(0);
        sampleRecord.setMaxRenewCount(2);
        sampleRecord.setOverdueDays(0);
        sampleRecord.setFineAmount(BigDecimal.ZERO);
        sampleRecord.setIsFinePaid(false);
        sampleRecord.setCreatedAt(LocalDateTime.now().minusDays(5));
        sampleRecord.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== borrowBook ====================

    @Nested
    @DisplayName("borrowBook - 借阅书籍")
    class BorrowBookTest {

        @Test
        @DisplayName("正常借阅 → 返回DTO，副本数减1")
        void success() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(borrowRecordMapper.countActiveBorrowsByUserId(1L)).thenReturn(0L);
            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(null);
            when(borrowRecordMapper.insert(any(BorrowRecord.class))).thenReturn(1);
            when(bookMapper.updateAvailableCopies(eq(1L), eq(7))).thenReturn(1);

            BorrowRecordDTO result = borrowService.borrowBook(1L, 1L);

            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("testuser", result.getUsername());
            assertEquals(1L, result.getBookId());
            assertEquals("Spring Boot实战", result.getBookTitle());
            assertEquals(BorrowRecord.Status.BORROWED, result.getStatus());

            verify(borrowRecordMapper).insert(any(BorrowRecord.class));
            verify(bookMapper).updateAvailableCopies(1L, 7);
        }

        @Test
        @DisplayName("用户不存在 → 抛出 USER_NOT_FOUND")
        void throwException_whenUserNotFound() {
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.borrowBook(999L, 1L));
            assertEquals(3002, ex.getCode());

            verify(borrowRecordMapper, never()).insert(any());
        }

        @Test
        @DisplayName("书籍不存在 → 抛出 BOOK_NOT_FOUND")
        void throwException_whenBookNotFound() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);
            when(bookMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.borrowBook(1L, 999L));
            assertEquals(3000, ex.getCode());
        }

        @Test
        @DisplayName("书籍不可用（副本为0） → 抛出 BOOK_UNAVAILABLE")
        void throwException_whenBookUnavailable() {
            sampleBook.setAvailableCopies(0);
            when(userMapper.selectById(1L)).thenReturn(sampleUser);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.borrowBook(1L, 1L));
            assertEquals(3001, ex.getCode());
        }

        @Test
        @DisplayName("已达借阅上限 → 抛出 BORROW_LIMIT_EXCEEDED")
        void throwException_whenBorrowLimitReached() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(borrowRecordMapper.countActiveBorrowsByUserId(1L)).thenReturn(5L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.borrowBook(1L, 1L));
            assertEquals(3004, ex.getCode());
        }

        @Test
        @DisplayName("已借阅此书 → 抛出 BOOK_ALREADY_BORROWED")
        void throwException_whenAlreadyBorrowed() {
            when(userMapper.selectById(1L)).thenReturn(sampleUser);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(borrowRecordMapper.countActiveBorrowsByUserId(1L)).thenReturn(0L);
            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(sampleRecord);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.borrowBook(1L, 1L));
            assertEquals(3005, ex.getCode());
        }
    }

    // ==================== returnBook ====================

    @Nested
    @DisplayName("returnBook - 归还书籍")
    class ReturnBookTest {

        @Test
        @DisplayName("正常归还（未逾期） → 状态变为RETURNED")
        void success_whenNotOverdue() {
            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(sampleRecord);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(borrowRecordMapper.update(any(BorrowRecord.class))).thenReturn(1);
            when(bookMapper.updateAvailableCopies(eq(1L), eq(9))).thenReturn(1);

            BorrowRecordDTO result = borrowService.returnBook(1L, 1L);

            assertNotNull(result);
            assertEquals(BorrowRecord.Status.RETURNED, result.getStatus());
            assertNotNull(result.getReturnDate());
            assertEquals(0.0, result.getFineAmount().doubleValue());

            verify(borrowRecordMapper).update(any(BorrowRecord.class));
            verify(bookMapper).updateAvailableCopies(1L, 9);
        }

        @Test
        @DisplayName("逾期归还 → 计算罚款")
        void success_whenOverdue_calculateFine() {
            sampleRecord.setDueDate(LocalDateTime.now().minusDays(10));

            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(sampleRecord);
            when(bookMapper.selectById(1L)).thenReturn(sampleBook);
            when(borrowRecordMapper.update(any(BorrowRecord.class))).thenReturn(1);
            when(bookMapper.updateAvailableCopies(eq(1L), anyInt())).thenReturn(1);

            BorrowRecordDTO result = borrowService.returnBook(1L, 1L);

            assertNotNull(result);
            assertEquals(BorrowRecord.Status.RETURNED, result.getStatus());
            assertTrue(result.getFineAmount().doubleValue() > 0);
        }

        @Test
        @DisplayName("借阅记录不存在 → 抛出 BORROW_RECORD_NOT_FOUND")
        void throwException_whenRecordNotFound() {
            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.returnBook(1L, 1L));
            assertEquals(3003, ex.getCode());
        }

        @Test
        @DisplayName("书籍不存在 → 抛出 BOOK_NOT_FOUND")
        void throwException_whenBookNotFound() {
            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(sampleRecord);
            when(bookMapper.selectById(1L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.returnBook(1L, 1L));
            assertEquals(3000, ex.getCode());
        }
    }

    // ==================== renewBook ====================

    @Nested
    @DisplayName("renewBook - 续借书籍")
    class RenewBookTest {

        @Test
        @DisplayName("正常续借 → 到期日延长30天，续借次数+1")
        void success() {
            LocalDateTime originalDueDate = sampleRecord.getDueDate();
            when(borrowRecordMapper.selectById(1L)).thenReturn(sampleRecord);
            when(borrowRecordMapper.update(any(BorrowRecord.class))).thenReturn(1);

            BorrowRecordDTO result = borrowService.renewBook(1L, 1L);

            assertNotNull(result);
            assertEquals(1, result.getRenewCount());
            verify(borrowRecordMapper).update(any(BorrowRecord.class));
        }

        @Test
        @DisplayName("记录不存在 → 抛出 BORROW_RECORD_NOT_FOUND")
        void throwException_whenRecordNotFound() {
            when(borrowRecordMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.renewBook(999L, 1L));
            assertEquals(3003, ex.getCode());
        }

        @Test
        @DisplayName("非本人操作 → 抛出 INSUFFICIENT_PERMISSION")
        void throwException_whenNotOwner() {
            when(borrowRecordMapper.selectById(1L)).thenReturn(sampleRecord);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.renewBook(1L, 999L));
            assertEquals(3008, ex.getCode());
        }

        @Test
        @DisplayName("已达续借上限 → 抛出 CANNOT_RENEW_BORROW")
        void throwException_whenMaxRenewReached() {
            sampleRecord.setRenewCount(2);
            when(borrowRecordMapper.selectById(1L)).thenReturn(sampleRecord);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.renewBook(1L, 1L));
            assertEquals(3007, ex.getCode());
        }
    }

    // ==================== 查询类方法 ====================

    @Nested
    @DisplayName("getUserBorrowRecords - 用户借阅记录分页")
    class GetUserBorrowRecordsTest {

        @Test
        @DisplayName("有记录 → 返回分页结果")
        void success() {
            when(borrowRecordMapper.selectByUserIdPaged(eq(1L), eq(0), eq(10))).thenReturn(List.of(sampleRecord));
            when(borrowRecordMapper.countByUserId(1L)).thenReturn(1);

            PageResult<BorrowRecordDTO> result = borrowService.getUserBorrowRecords(1L, 0, 10);

            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getTotal());
            assertEquals(0, result.getPage());
            assertEquals(10, result.getSize());
        }

        @Test
        @DisplayName("无记录 → 返回空列表")
        void empty() {
            when(borrowRecordMapper.selectByUserIdPaged(eq(1L), eq(0), eq(10))).thenReturn(Collections.emptyList());
            when(borrowRecordMapper.countByUserId(1L)).thenReturn(0);

            PageResult<BorrowRecordDTO> result = borrowService.getUserBorrowRecords(1L, 0, 10);

            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotal());
        }
    }

    @Nested
    @DisplayName("getCurrentBorrows - 当前借阅列表")
    class GetCurrentBorrowsTest {

        @Test
        @DisplayName("有当前借阅 → 返回列表")
        void success() {
            when(borrowRecordMapper.selectByUserIdAndStatus(1L, "BORROWED")).thenReturn(List.of(sampleRecord));

            List<BorrowRecordDTO> result = borrowService.getCurrentBorrows(1L);

            assertEquals(1, result.size());
            assertEquals(BorrowRecord.Status.BORROWED, result.get(0).getStatus());
        }

        @Test
        @DisplayName("无借阅 → 返回空列表")
        void empty() {
            when(borrowRecordMapper.selectByUserIdAndStatus(1L, "BORROWED")).thenReturn(Collections.emptyList());

            List<BorrowRecordDTO> result = borrowService.getCurrentBorrows(1L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getOverdueBorrows - 逾期借阅列表")
    class GetOverdueBorrowsTest {

        @Test
        @DisplayName("有逾期记录 → 只返回逾期的")
        void success() {
            BorrowRecord overdueRecord = new BorrowRecord();
            overdueRecord.setId(2L);
            overdueRecord.setUser(sampleUser);
            overdueRecord.setBook(sampleBook);
            overdueRecord.setDueDate(LocalDateTime.now().minusDays(5));
            overdueRecord.setStatus(BorrowRecord.Status.BORROWED);

            when(borrowRecordMapper.selectByUserIdAndStatus(1L, "BORROWED"))
                    .thenReturn(List.of(sampleRecord, overdueRecord));

            List<BorrowRecordDTO> result = borrowService.getOverdueBorrows(1L);

            assertEquals(1, result.size());
            assertEquals(2L, result.get(0).getId());
        }
    }

    @Nested
    @DisplayName("getBookBorrowRecords - 书籍借阅记录分页")
    class GetBookBorrowRecordsTest {

        @Test
        @DisplayName("有记录 → 返回分页结果")
        void success() {
            when(borrowRecordMapper.selectActiveBorrowsByBookIdPaged(eq(1L), eq(0), eq(10)))
                    .thenReturn(List.of(sampleRecord));
            when(borrowRecordMapper.countActiveBorrowsByBookIdForPage(1L)).thenReturn(1);

            PageResult<BorrowRecordDTO> result = borrowService.getBookBorrowRecords(1L, 0, 10);

            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getTotal());
        }
    }

    // ==================== hasReachedBorrowLimit ====================

    @Nested
    @DisplayName("hasReachedBorrowLimit - 借阅上限检查")
    class HasReachedBorrowLimitTest {

        @Test
        @DisplayName("未达上限(3<5) → false")
        void false_whenUnderLimit() {
            when(borrowRecordMapper.countActiveBorrowsByUserId(1L)).thenReturn(3L);

            assertFalse(borrowService.hasReachedBorrowLimit(1L));
        }

        @Test
        @DisplayName("恰好达到上限(5>=5) → true")
        void true_whenAtLimit() {
            when(borrowRecordMapper.countActiveBorrowsByUserId(1L)).thenReturn(5L);

            assertTrue(borrowService.hasReachedBorrowLimit(1L));
        }

        @Test
        @DisplayName("超过上限(7>=5) → true")
        void true_whenOverLimit() {
            when(borrowRecordMapper.countActiveBorrowsByUserId(1L)).thenReturn(7L);

            assertTrue(borrowService.hasReachedBorrowLimit(1L));
        }
    }

    // ==================== calculateOverdueFines ====================

    @Nested
    @DisplayName("calculateOverdueFines - 计算逾期罚款")
    class CalculateOverdueFinesTest {

        @Test
        @DisplayName("有逾期记录 → 计算罚款并更新")
        void success() {
            sampleRecord.setDueDate(LocalDateTime.now().minusDays(10));

            when(borrowRecordMapper.selectOverdueRecords(any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleRecord));
            when(borrowRecordMapper.update(any(BorrowRecord.class))).thenReturn(1);

            borrowService.calculateOverdueFines(1L);

            verify(borrowRecordMapper).update(argThat(record ->
                    record.getFineAmount().compareTo(BigDecimal.ZERO) > 0));
        }

        @Test
        @DisplayName("无逾期记录 → 不调用update")
        void noUpdate_whenNoOverdue() {
            when(borrowRecordMapper.selectOverdueRecords(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            borrowService.calculateOverdueFines(1L);

            verify(borrowRecordMapper, never()).update(any(BorrowRecord.class));
        }

        @Test
        @DisplayName("逾期记录属于其他用户 → 不更新")
        void skip_whenRecordBelongsToOtherUser() {
            User otherUser = new User();
            otherUser.setId(999L);
            sampleRecord.setUser(otherUser);
            sampleRecord.setDueDate(LocalDateTime.now().minusDays(10));

            when(borrowRecordMapper.selectOverdueRecords(any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleRecord));

            borrowService.calculateOverdueFines(1L);

            verify(borrowRecordMapper, never()).update(any(BorrowRecord.class));
        }
    }

    // ==================== isBookBorrowedByUser ====================

    @Nested
    @DisplayName("isBookBorrowedByUser - 检查是否已借阅")
    class IsBookBorrowedByUserTest {

        @Test
        @DisplayName("已借阅 → true")
        void true_whenBorrowed() {
            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(sampleRecord);

            assertTrue(borrowService.isBookBorrowedByUser(1L, 1L));
        }

        @Test
        @DisplayName("未借阅 → false")
        void false_whenNotBorrowed() {
            when(borrowRecordMapper.selectNotReturnedRecord(1L, 1L)).thenReturn(null);

            assertFalse(borrowService.isBookBorrowedByUser(1L, 1L));
        }
    }

    // ==================== getBorrowRecordById ====================

    @Nested
    @DisplayName("getBorrowRecordById - 借阅记录详情")
    class GetBorrowRecordByIdTest {

        @Test
        @DisplayName("记录存在 → 返回DTO")
        void success() {
            when(borrowRecordMapper.selectById(1L)).thenReturn(sampleRecord);

            BorrowRecordDTO result = borrowService.getBorrowRecordById(1L);

            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
            assertEquals("Spring Boot实战", result.getBookTitle());
        }

        @Test
        @DisplayName("记录不存在 → 抛出 BORROW_RECORD_NOT_FOUND")
        void throwException_whenNotFound() {
            when(borrowRecordMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.getBorrowRecordById(999L));
            assertEquals(3003, ex.getCode());
        }
    }

    // ==================== getDueSoonRecords ====================

    @Nested
    @DisplayName("getDueSoonRecords - 即将到期记录")
    class GetDueSoonRecordsTest {

        @Test
        @DisplayName("有即将到期记录 → 返回列表")
        void success() {
            when(borrowRecordMapper.selectDueSoonRecords(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleRecord));

            List<BorrowRecordDTO> result = borrowService.getDueSoonRecords(1L, 3);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("属于其他用户的记录 → 被过滤")
        void filterOtherUsers() {
            User otherUser = new User();
            otherUser.setId(999L);
            sampleRecord.setUser(otherUser);

            when(borrowRecordMapper.selectDueSoonRecords(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleRecord));

            List<BorrowRecordDTO> result = borrowService.getDueSoonRecords(1L, 3);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== getAllOverdueRecords ====================

    @Nested
    @DisplayName("getAllOverdueRecords - 所有逾期记录")
    class GetAllOverdueRecordsTest {

        @Test
        @DisplayName("有逾期记录 → 返回列表")
        void success() {
            when(borrowRecordMapper.selectOverdueRecords(any(LocalDateTime.class)))
                    .thenReturn(List.of(sampleRecord));

            List<BorrowRecordDTO> result = borrowService.getAllOverdueRecords();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("无逾期记录 → 返回空列表")
        void empty() {
            when(borrowRecordMapper.selectOverdueRecords(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            List<BorrowRecordDTO> result = borrowService.getAllOverdueRecords();

            assertTrue(result.isEmpty());
        }
    }

    // ==================== reportLostBook ====================

    @Nested
    @DisplayName("reportLostBook - 报失")
    class ReportLostBookTest {

        @Test
        @DisplayName("记录存在 → 状态改为LOST")
        void success() {
            when(borrowRecordMapper.selectById(1L)).thenReturn(sampleRecord);
            when(borrowRecordMapper.update(any(BorrowRecord.class))).thenReturn(1);

            assertDoesNotThrow(() -> borrowService.reportLostBook(1L, "丢失了"));

            verify(borrowRecordMapper).update(argThat(record ->
                    record.getStatus() == BorrowRecord.Status.LOST));
        }

        @Test
        @DisplayName("记录不存在 → 抛出 BORROW_RECORD_NOT_FOUND")
        void throwException_whenNotFound() {
            when(borrowRecordMapper.selectById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> borrowService.reportLostBook(999L, "丢失了"));
            assertEquals(3003, ex.getCode());
        }
    }
}