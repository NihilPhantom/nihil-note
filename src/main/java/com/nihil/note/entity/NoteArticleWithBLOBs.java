package com.nihil.note.entity;

public class NoteArticleWithBLOBs extends NoteArticle {
    private String markdown;

    private String content;

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown == null ? null : markdown.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }
}