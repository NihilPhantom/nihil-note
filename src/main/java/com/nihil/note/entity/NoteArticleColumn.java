package com.nihil.note.entity;

import lombok.Data;

@Data
public class NoteArticleColumn {
    private String articleId;

    private String columnId;

    private String preArticleId;

    private String aftArticleId;

    public NoteArticleColumn(String articleId, String columnId, String preArticleId, String aftArticleId) {
        this.articleId = articleId;
        this.columnId = columnId;
        this.preArticleId = preArticleId;
        this.aftArticleId = aftArticleId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId == null ? null : articleId.trim();
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId == null ? null : columnId.trim();
    }

    public String getPreArticleId() {
        return preArticleId;
    }

    public void setPreArticleId(String preArticleId) {
        this.preArticleId = preArticleId == null ? null : preArticleId.trim();
    }

    public String getAftArticleId() {
        return aftArticleId;
    }

    public void setAftArticleId(String aftArticleId) {
        this.aftArticleId = aftArticleId == null ? null : aftArticleId.trim();
    }
}