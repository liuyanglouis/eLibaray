package com.elibrary.service;

import com.elibrary.common.response.PageResult;
import com.elibrary.model.dto.BorrowRecordDTO;

import java.util.List;

public interface BorrowService {

    BorrowRecordDTO borrowBook(Long userId, Long bookId);

    BorrowRecordDTO returnBook(Long userId, Long bookId);

    BorrowRecordDTO renewBook(Long borrowRecordId, Long userId);

    PageResult<BorrowRecordDTO> getUserBorrowRecords(Long userId, int page, int size);

    List<BorrowRecordDTO> getCurrentBorrows(Long userId);

    List<BorrowRecordDTO> getOverdueBorrows(Long userId);

    PageResult<BorrowRecordDTO> getBookBorrowRecords(Long bookId, int page, int size);

    boolean hasReachedBorrowLimit(Long userId);

    void calculateOverdueFines(Long userId);

    boolean isBookBorrowedByUser(Long userId, Long bookId);

    BorrowRecordDTO getBorrowRecordById(Long recordId);

    List<BorrowRecordDTO> getDueSoonRecords(Long userId, int daysBeforeDue);

    List<BorrowRecordDTO> getAllOverdueRecords();

    void reportLostBook(Long borrowRecordId, String remarks);
}