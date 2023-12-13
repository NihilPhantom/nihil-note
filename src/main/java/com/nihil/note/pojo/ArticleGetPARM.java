package com.nihil.note.pojo;

import com.nihil.note.entity.NoteLabel;
import lombok.Data;

import java.util.List;

@Data
public class ArticleGetPARM {
    private String id;
    private String authorId;            // 作者 ID
    private String title;               // 文章标题
    private String des;                 // 文章描述
    private Integer labelId;            // 标签列表
    private String columnId;            // 专栏Id
    private String markdown;            // markdown 内容
    private String content;             // html     内容
    private Integer start;              // 查询开始条数
    private Integer length;             // 查询长度
    private boolean published;          // 是否已公开
}
