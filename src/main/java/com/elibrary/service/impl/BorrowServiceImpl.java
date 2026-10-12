package com.elibrary.service.impl;

import com.elibrary.common.exception.BusinessException;
import com.elibrary.common.exception.ErrorCode;
import com.elibrary.common.response.PageResult;
import com.elibrary.mapper.BookMapper;
import com.elibrary.mapper.BorrowRecordMapper;
import com.elibrary.mapper.UserMapper;
import com.elibrary.model.dto.BorrowRecordDTO;
import com.elibrary.model.entity.Book;
import com.elibrary.model.entity.BorrowRecord;
import com.elibrary.model.entity.User;
import com.elibrary.service.BorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    private static final int MAX_BORROW_COUNT = 5;
    private static final int MAX_RENEW_COUNT = 2;
    private static final int BORROW_PERIOD_DAYS = 30;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.50");

    @Override
    @Transactional
    public BorrowRecordDTO borrowBook(Long userId, Long bookId) {
        log.info("用户借阅书籍, userId: {}, bookId: {}", userId, bookId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        if (!book.isAvailable()) {
            throw new BusinessException(ErrorCode.BOOK_UNAVAILABLE);
        }

        if (hasReachedBorrowLimit(userId)) {
            throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED);
        }

        if (isBookBorrowedByUser(userId, bookId)) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_BORROWED);
        }

        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUser(user);
        borrowRecord.setBook(book);
        borrowRecord.setBorrowDate(LocalDateTime.now());
        borrowRecord.setDueDate(LocalDateTime.now().plusDays(BORROW_PERIOD_DAYS));
        borrowRecord.setStatus(BorrowRecord.Status.BORROWED);
        borrowRecord.setMaxRenewCount(MAX_RENEW_COUNT);
        borrowRecord.setCreatedAt(LocalDateTime.now());
        borrowRecord.setUpdatedAt(LocalDateTime.now());

        book.borrowCopy();

        borrowRecordMapper.insert(borrowRecord);
        bookMapper.updateAvailableCopies(bookId, book.getAvailableCopies());

        log.info("书籍借阅成功, userId: {}, bookId: {}, recordId: {}",
                userId, bookId, borrowRecord.getId());

        return convertToDTO(borrowRecord);
    }

    @Override
    @Transactional
    public BorrowRecordDTO returnBook(Long userId, Long bookId) {
        log.info("用户归还书籍, userId: {}, bookId: {}", userId, bookId);

        BorrowRecord borrowRecord = borrowRecordMapper.selectNotReturnedRecord(userId, bookId);
        if (borrowRecord == null) {
            throw new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND);
        }

        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }

        if (borrowRecord.isOverdue()) {
            borrowRecord.calculateOverdue();
            BigDecimal fine = FINE_PER_DAY.multiply(new BigDecimal(borrowRecord.getOverdueDays()));
            borrowRecord.setFineAmount(fine);
            log.info("计算逾期罚款, userId: {}, bookId: {}, 逾期天数: {}, 罚款金额: {}",
                    userId, bookId, borrowRecord.getOverdueDays(), fine);
        }

        borrowRecord.setReturnDate(LocalDateTime.now());
        borrowRecord.setStatus(BorrowRecord.Status.RETURNED);
        borrowRecord.setUpdatedAt(LocalDateTime.now());

        book.returnCopy();

        borrowRecordMapper.update(borrowRecord);
        bookMapper.updateAvailableCopies(bookId, book.getAvailableCopies());

        log.info("书籍归还成功, userId: {}, bookId: {}, recordId: {}",
                userId, bookId, borrowRecord.getId());

        return convertToDTO(borrowRecord);
    }

    @Override
    @Transactional
    public BorrowRecordDTO renewBook(Long borrowRecordId, Long userId) {
        log.info("用户续借书籍, userId: {}, recordId: {}", userId, borrowRecordId);

        BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowRecordId);
        if (borrowRecord == null) {
            throw new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND);
        }

        if (!borrowRecord.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        if (!borrowRecord.canRenew()) {
            throw new BusinessException(ErrorCode.CANNOT_RENEW_BORROW,
                    "无法续借，可能原因：已达最大续借次数、书籍已逾期、已归还");
        }

        borrowRecord.setDueDate(borrowRecord.getDueDate().plusDays(BORROW_PERIOD_DAYS));
        borrowRecord.setRenewCount(borrowRecord.getRenewCount() + 1);
        borrowRecord.setUpdatedAt(LocalDateTime.now());

        borrowRecordMapper.update(borrowRecord);

        log.info("书籍续借成功, userId: {}, recordId: {}, 新的到期时间: {}",
                userId, borrowRecordId, borrowRecord.getDueDate());

        return convertToDTO(borrowRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<BorrowRecordDTO> getUserBorrowRecords(Long userId, int page, int size) {
        log.info("获取用户借阅记录, userId: {}", userId);

        int offset = page * size;
        List<BorrowRecord> records = borrowRecordMapper.selectByUserIdPaged(userId, offset, size);
        int total = borrowRecordMapper.countByUserId(userId);

        List<BorrowRecordDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResult<>(dtoList, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDTO> getCurrentBorrows(Long userId) {
        log.info("获取用户当前借阅记录, userId: {}", userId);

        return borrowRecordMapper.selectByUserIdAndStatus(userId, "BORROWED").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDTO> getOverdueBorrows(Long userId) {
        log.info("获取用户逾期记录, userId: {}", userId);

        return borrowRecordMapper.selectByUserIdAndStatus(userId, "BORROWED").stream()
                .filter(BorrowRecord::isOverdue)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<BorrowRecordDTO> getBookBorrowRecords(Long bookId, int page, int size) {
        log.info("获取书籍借阅记录, bookId: {}", bookId);

        int offset = page * size;
        List<BorrowRecord> records = borrowRecordMapper.selectActiveBorrowsByBookIdPaged(bookId, offset, size);
        int total = borrowRecordMapper.countActiveBorrowsByBookIdForPage(bookId);

        List<BorrowRecordDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageResult<>(dtoList, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReachedBorrowLimit(Long userId) {
        long activeBorrowCount = borrowRecordMapper.countActiveBorrowsByUserId(userId);
        return activeBorrowCount >= MAX_BORROW_COUNT;
    }

    @Override
    @Transactional
    public void calculateOverdueFines(Long userId) {
        log.info("计算用户逾期罚款, userId: {}", userId);

        List<BorrowRecord> overdueRecords = borrowRecordMapper.selectOverdueRecords(LocalDateTime.now())
                .stream()
                .filter(record -> record.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        for (BorrowRecord record : overdueRecords) {
            record.calculateOverdue();
            BigDecimal fine = FINE_PER_DAY.multiply(new BigDecimal(record.getOverdueDays()));
            record.setFineAmount(fine);
            record.setUpdatedAt(LocalDateTime.now());
            borrowRecordMapper.update(record);

            log.info("计算逾期罚款完成, recordId: {}, 逾期天数: {}, 罚款金额: {}",
                    record.getId(), record.getOverdueDays(), fine);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookBorrowedByUser(Long userId, Long bookId) {
        return borrowRecordMapper.selectNotReturnedRecord(userId, bookId) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowRecordDTO getBorrowRecordById(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND);
        }
        return convertToDTO(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDTO> getDueSoonRecords(Long userId, int daysBeforeDue) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(daysBeforeDue);

        return borrowRecordMapper.selectDueSoonRecords(startDate, endDate).stream()
                .filter(record -> record.getUser().getId().equals(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordDTO> getAllOverdueRecords() {
        return borrowRecordMapper.selectOverdueRecords(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reportLostBook(Long borrowRecordId, String remarks) {
        BorrowRecord record = borrowRecordMapper.selectById(borrowRecordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND);
        }

        record.setStatus(BorrowRecord.Status.LOST);
        record.setUpdatedAt(LocalDateTime.now());
        borrowRecordMapper.update(record);

        log.info("书籍报失成功, recordId: {}, 备注: {}", borrowRecordId, remarks);
    }

    private BorrowRecordDTO convertToDTO(BorrowRecord record) {
        BorrowRecordDTO dto = new BorrowRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUser().getId());
        dto.setUsername(record.getUser().getUsername());
        dto.setBookId(record.getBook().getId());
        dto.setBookTitle(record.getBook().getTitle());
        dto.setBookAuthor(record.getBook().getAuthor());
        dto.setBorrowDate(record.getBorrowDate());
        dto.setDueDate(record.getDueDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setStatus(record.getStatus());
        dto.setRenewCount(record.getRenewCount());
        dto.setMaxRenewCount(record.getMaxRenewCount());
        dto.setOverdueDays(record.getOverdueDays());
        dto.setFineAmount(record.getFineAmount());
        dto.setIsFinePaid(record.getIsFinePaid());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }
}