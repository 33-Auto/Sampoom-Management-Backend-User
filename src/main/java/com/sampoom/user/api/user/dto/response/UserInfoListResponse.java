package com.sampoom.user.api.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoListResponse {
    private List<UserInfoResponse> users;
    private PageMeta meta;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PageMeta {
        private int currentPage;     // 현재 페이지 번호
        private int totalPages;      // 전체 페이지 수
        private long totalElements;  // 전체 데이터 개수
        private int size;            // 페이지당 데이터 수
        private boolean hasNext;     // 다음 페이지 존재 여부
        private boolean hasPrevious; // 이전 페이지 존재 여부
    }

    public static <T> UserInfoListResponse of(Page<UserInfoResponse> page) {
        return UserInfoListResponse.builder()
                .users(page.getContent())
                .meta(PageMeta.builder()
                        .currentPage(page.getNumber())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .size(page.getSize())
                        .hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious())
                        .build())
                .build();
    }
}
