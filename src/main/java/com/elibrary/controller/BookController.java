package com.elibrary.controller;

import com.elibrary.common.response.ApiResponse;
import com.elibrary.common.response.PageResult;
import com.elibrary.model.dto.BookDTO;
import com.elibrary.model.dto.BookSearchDTO;
import com.elibrary.model.dto.CreateBookDTO;
import com.elibrary.model.dto.PageRequestDTO;
import com.elibrary.model.entity.Book;
import com.elibrary.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "书籍管理", description = "书籍相关的接口，包括浏览、搜索、创建、更新和删除")
public class BookController {

    private final BookService bookService;

    @GetMapping("/{id}")
    @Operation(summary = "获取书籍详情", description = "根据书籍ID获取详细信息")
    public ApiResponse<BookDTO> getBookById(
            @Parameter(description = "书籍ID", required = true)
            @PathVariable Long id) {
        BookDTO bookDTO = bookService.getBookById(id);
        return ApiResponse.success(bookDTO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建书籍", description = "创建新的书籍记录")
    public ApiResponse<BookDTO> createBook(
            @Parameter(description = "书籍信息", required = true)
            @Valid @RequestBody CreateBookDTO createBookDTO) {
        BookDTO bookDTO = bookService.createBook(createBookDTO);
        return ApiResponse.success(bookDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新书籍", description = "更新书籍信息")
    public ApiResponse<BookDTO> updateBook(
            @Parameter(description = "书籍ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新后的书籍信息", required = true)
            @Valid @RequestBody CreateBookDTO updateBookDTO) {
        BookDTO bookDTO = bookService.updateBook(id, updateBookDTO);
        return ApiResponse.success(bookDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除书籍", description = "软删除书籍（标记为不可用）")
    public ApiResponse<Void> deleteBook(
            @Parameter(description = "书籍ID", required = true)
            @PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success();
    }

    @GetMapping("/search")
    @Operation(summary = "搜索书籍", description = "根据条件搜索书籍，支持分页")
    public ApiResponse<PageResult<BookDTO>> searchBooks(
            @Parameter(description = "搜索关键字")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "分类")
            @RequestParam(required = false) Book.Category category,
            @Parameter(description = "页码（从0开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        BookSearchDTO searchDTO = new BookSearchDTO();
        searchDTO.setTitle(keyword);
        searchDTO.setAuthor(keyword);
        searchDTO.setCategory(category);

        PageResult<BookDTO> bookPage = bookService.searchBooks(searchDTO, page, size);
        return ApiResponse.success(bookPage);
    }

    @GetMapping("/available")
    @Operation(summary = "获取可借阅书籍", description = "获取所有可借阅的书籍列表，支持分页")
    public ApiResponse<PageResult<BookDTO>> getAvailableBooks(@RequestBody PageRequestDTO requestDTO) {

        PageResult<BookDTO> bookPage = bookService.getAvailableBooks(requestDTO.getPage(), requestDTO.getSize());
        return ApiResponse.success(bookPage);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取书籍", description = "根据分类获取书籍列表，支持分页")
    public ApiResponse<PageResult<BookDTO>> getBooksByCategory(
            @Parameter(description = "书籍分类", required = true)
            @PathVariable Book.Category category,
            @Parameter(description = "页码（从0开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        PageResult<BookDTO> bookPage = bookService.getBooksByCategory(category, page, size);
        return ApiResponse.success(bookPage);
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门书籍", description = "获取评分最高的热门书籍")
    public ApiResponse<List<BookDTO>> getPopularBooks(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(bookService.getPopularBooks(limit));
    }

    @GetMapping("/newest")
    @Operation(summary = "获取最新书籍", description = "获取最新添加的书籍")
    public ApiResponse<List<BookDTO>> getNewestBooks(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(bookService.getNewestBooks(limit));
    }

    @GetMapping("/{id}/available-copies")
    @Operation(summary = "获取可用副本数量", description = "获取书籍的可用副本数量")
    public ApiResponse<Integer> getAvailableCopies(
            @Parameter(description = "书籍ID", required = true)
            @PathVariable Long id) {
        return ApiResponse.success(bookService.getAvailableCopies(id));
    }

    @GetMapping("/isbn-available")
    @Operation(summary = "检查ISBN是否可用", description = "检查ISBN是否已被其他书籍使用")
    public ApiResponse<Boolean> checkIsbnAvailable(
            @Parameter(description = "ISBN号码", required = true)
            @RequestParam String isbn,
            @Parameter(description = "排除的书籍ID（可选）")
            @RequestParam(required = false) Long excludeId) {
        return ApiResponse.success(bookService.isIsbnAvailable(isbn, excludeId));
    }
}