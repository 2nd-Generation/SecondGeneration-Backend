package com.web.coreclass.domain.article.entity;

import com.web.coreclass.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "article")
@ToString
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleCategory category; // News, Event, Recruit

    @Column(nullable = false)
    private String title; // SGEA 아카데미 오픈 특별 이벤트

    @Column(name = "sub_title", nullable = false)
    private String subTitle; // 아카데미 런칭 기념 무료 체험 강의 및 장기 등록 할인 혜택

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 이벤트 자세히 보기

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "posted_at")
    private LocalDate postedAt;

    @Column(name = "start_date")
    private LocalDate startDate; // 이벤트 시작 날짜 (nullable)

    @Column(name = "end_date")
    private LocalDate endDate; // 이벤트 종료 날짜 (nullable)

}
