package com.nihil.note.pojo;

import lombok.Data;

@Data
public class ColumnArticle {
    private String articleId;            // 作者id
    private String columnId;           // 是否发布

    public ColumnArticle() {
    }

    public ColumnArticle(String articleId, String columnId) {
        this.articleId = articleId;
        this.columnId = columnId;
    }
}
