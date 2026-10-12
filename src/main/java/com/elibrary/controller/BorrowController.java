package com.elibrary.controller;

import com.elibrary.common.response.ApiResponse;
import com.elibrary.common.response.PageResult;
import com.elibrary.model.dto.*;
import com.elibrary.service.BorrowService;
import com.elibrary.common.security.UserContext;
import com.elibrary.common.security.LoginRequired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
@Tag(name = "借阅管理", description = "书籍借阅、归还、续借相关的接口")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    @Operation(summary = "借阅书籍", description = "用户借阅指定书籍")
    public ApiResponse<BorrowRecordDTO> borrowBook(
            @Valid @RequestBody BorrowRequestDTO request) {
        return ApiResponse.success(borrowService.borrowBook(request.getUserId(), request.getBookId()));
    }

    @PutMapping("/return")
    @Operation(summary = "归还书籍", description = "用户归还已借阅的书籍")
    public ApiResponse<BorrowRecordDTO> returnBook(
            @Valid @RequestBody BorrowRequestDTO request) {
        return ApiResponse.success(borrowService.returnBook(request.getUserId(), request.getBookId()));
    }

    @PutMapping("/renew")
    @Operation(summary = "续借书籍", description = "续借已借阅的书籍")
    public ApiResponse<BorrowRecordDTO> renewBook(
            @Valid @RequestBody RenewRequestDTO request) {
        return ApiResponse.success(borrowService.renewBook(request.getRecordId(), request.getUserId()));
    }

    @LoginRequired
    @PostMapping("/users/records")
    @Operation(summary = "获取用户借阅记录", description = "获取当前登录用户的借阅历史记录，支持分页")
    public ApiResponse<PageResult<BorrowRecordDTO>> getUserBorrowRecords(
            @RequestBody(required = false) PageRequestDTO pageRequest) {
        Long userId = UserContext.getCurrentUserId();
        PageRequestDTO page = pageRequest != null ? pageRequest : new PageRequestDTO();
        PageResult<BorrowRecordDTO> records = borrowService.getUserBorrowRecords(userId, page.getPage(), page.getSize());
        return ApiResponse.success(records);
    }

    @LoginRequired
    @GetMapping("/users/current")
    @Operation(summary = "获取当前借阅记录", description = "获取当前登录用户正在借阅的书籍列表")
    public ApiResponse<List<BorrowRecordDTO>> getCurrentBorrows() {
        Long userId = UserContext.getCurrentUserId();
        return ApiResponse.success(borrowService.getCurrentBorrows(userId));
    }

    @LoginRequired
    @GetMapping("/users/overdue")
    @Operation(summary = "获取逾期记录", description = "获取当前登录用户的逾期借阅记录")
    public ApiResponse<List<BorrowRecordDTO>> getOverdueBorrows() {
        Long userId = UserContext.getCurrentUserId();
        return ApiResponse.success(borrowService.getOverdueBorrows(userId));
    }

    @PostMapping("/books/records")
    @Operation(summary = "获取书籍借阅记录", description = "获取指定书籍的借阅记录")
    public ApiResponse<PageResult<BorrowRecordDTO>> getBookBorrowRecords(
            @RequestParam Long bookId,
            @RequestBody(required = false) PageRequestDTO pageRequest) {
        PageRequestDTO page = pageRequest != null ? pageRequest : new PageRequestDTO();
        PageResult<BorrowRecordDTO> records = borrowService.getBookBorrowRecords(bookId, page.getPage(), page.getSize());
        return ApiResponse.success(records);
    }

    @LoginRequired
    @GetMapping("/users/check-limit")
    @Operation(summary = "检查借阅限制", description = "检查当前登录用户是否已达到借阅上限")
    public ApiResponse<Boolean> checkBorrowLimit() {
        Long userId = UserContext.getCurrentUserId();
        return ApiResponse.success(borrowService.hasReachedBorrowLimit(userId));
    }

    @GetMapping("/records/{recordId}")
    @Operation(summary = "获取借阅记录详情", description = "根据记录ID获取借阅详情")
    public ApiResponse<BorrowRecordDTO> getBorrowRecordById(
            @PathVariable Long recordId) {
        return ApiResponse.success(borrowService.getBorrowRecordById(recordId));
    }

    @LoginRequired
    @PostMapping("/users/due-soon")
    @Operation(summary = "获取即将到期记录", description = "获取当前登录用户即将到期的借阅记录")
    public ApiResponse<List<BorrowRecordDTO>> getDueSoonRecords(
            @RequestBody DueSoonRequestDTO request) {
        Long userId = UserContext.getCurrentUserId();
        return ApiResponse.success(borrowService.getDueSoonRecords(userId, request.getDaysBeforeDue()));
    }

    @GetMapping("/overdue/all")
    @Operation(summary = "获取所有逾期记录", description = "获取系统中所有的逾期借阅记录")
    public ApiResponse<List<BorrowRecordDTO>> getAllOverdueRecords() {
        return ApiResponse.success(borrowService.getAllOverdueRecords());
    }

    @PostMapping("/report-lost")
    @Operation(summary = "报告书籍丢失", description = "报告借阅的书籍已丢失")
    public ApiResponse<Void> reportLostBook(
            @Valid @RequestBody ReportLostRequestDTO request) {
        borrowService.reportLostBook(request.getRecordId(), request.getRemarks());
        return ApiResponse.success();
    }
}