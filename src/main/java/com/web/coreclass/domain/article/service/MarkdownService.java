package com.web.coreclass.domain.article.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {
    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();
    // (보안 정책: 기본 포맷 + 링크 허용)
    private static final PolicyFactory policy = Sanitizers.FORMATTING
            .and(Sanitizers.LINKS)
            .and(Sanitizers.BLOCKS);

    /**
     * 마크다운 원본을 XSS가 제거된 안전한 HTML로 변환합니다.
     */
    public String markdownToSafeHtml(String markdownContent) {
        if (markdownContent == null || markdownContent.isEmpty()) {
            return "";
        }
        // 1. Markdown -> HTML
        Node document = parser.parse(markdownContent);
        String html = renderer.render(document);

        // 2. HTML -> Safe HTML (XSS 방지)
        String safeHtml = policy.sanitize(html);

        return safeHtml;
    }
}
