package com.chatapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic wrapper for paginated responses.
 * Used for endpoints that return lists with pagination support.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

    @Schema(
            description = "List of items in the current page"
    )
    private List<T> content;

    @Schema(
            description = "Current page number (0-indexed)",
            example = "0"
    )
    private int pageNumber;

    @Schema(
            description = "Number of items per page",
            example = "20"
    )
    private int pageSize;

    @Schema(
            description = "Total number of items across all pages",
            example = "100"
    )
    private long totalElements;

    @Schema(
            description = "Total number of pages",
            example = "5"
    )
    private int totalPages;

    @Schema(
            description = "Whether this is the last page",
            example = "false"
    )
    private boolean last;

    @Schema(
            description = "Whether this is the first page",
            example = "true"
    )
    private boolean first;

    @Schema(
            description = "Whether the page is empty",
            example = "false"
    )
    private boolean empty;
}
