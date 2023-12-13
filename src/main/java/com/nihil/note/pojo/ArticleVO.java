package com.nihil.note.pojo;

import com.nihil.note.entity.NoteLabel;
import lombok.Data;

import java.util.List;

@Data
public class ArticleVO {
    private Long id;
    private String authorId;            // 作者 ID
    private String title;               // 文章标题
    private String des;                 // 文章描述
    private String imgHref;             // 图片链接：用于前端展示
    private List<NoteLabel> labelList;  // 标签列表
    private Long columnId;              // 专栏 Id
    private String markdown;            // markdown 内容
    private String content;             // html     内容
    private boolean published;          // 是否公开
}
