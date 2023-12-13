package com.nihil.note.pojo;

import lombok.Data;

@Data
public class ArticleExchangePARAM {
    String articleId1;  // 前面的一篇文章
    String articleId2;  // 后面的一篇文章
    String columnId;
}
