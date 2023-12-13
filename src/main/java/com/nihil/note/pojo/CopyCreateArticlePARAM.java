package com.nihil.note.pojo;

import lombok.Data;

@Data
public class CopyCreateArticlePARAM {
    String articleId;          // 复制创建的新的文章的ID
    String fromArticleId;
    String toBeforeArticleId;
    String toColumnId;
}