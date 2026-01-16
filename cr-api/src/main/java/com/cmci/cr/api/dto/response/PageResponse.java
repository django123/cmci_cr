package com.cmci.cr.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO Response générique pour la pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;
}
