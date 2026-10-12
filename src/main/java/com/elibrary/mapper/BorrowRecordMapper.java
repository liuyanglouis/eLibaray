package com.elibrary.mapper;

import com.elibrary.model.dto.BorrowRecordDTO;
import com.elibrary.model.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BorrowRecordMapper {

    BorrowRecord selectById(Long id);

    List<BorrowRecord> selectByUserId(Long userId);

    List<BorrowRecord> selectByBookId(Long bookId);

    List<BorrowRecord> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    BorrowRecord selectActiveBorrow(@Param("userId") Long userId, @Param("bookId") Long bookId);

    List<BorrowRecord> selectOverdueRecords(@Param("now") LocalDateTime now);

    List<BorrowRecord> selectActiveBorrows();

    List<BorrowRecord> selectByUserIdAndBorrowDateBetween(@Param("userId") Long userId,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);

    List<BorrowRecord> selectActiveBorrowsByBookIdPaged(@Param("bookId") Long bookId,
                                                        @Param("offset") int offset, @Param("limit") int limit);

    int countActiveBorrowsByBookIdForPage(@Param("bookId") Long bookId);

    List<BorrowRecord> selectByUserIdPaged(@Param("userId") Long userId,
                                           @Param("offset") int offset, @Param("limit") int limit);

    int countByUserId(@Param("userId") Long userId);

    List<BorrowRecord> selectDueSoonRecords(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    int insert(BorrowRecord borrowRecord);

    int update(BorrowRecord borrowRecord);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateReturnDate(@Param("id") Long id);

    int updateDueDate(@Param("id") Long id, @Param("days") int days);

    int delete(Long id);

    Long countActiveBorrowsByUserId(Long userId);

    Long countActiveBorrowsByBookId(Long bookId);

    BorrowRecord selectNotReturnedRecord(@Param("userId") Long userId, @Param("bookId") Long bookId);
}