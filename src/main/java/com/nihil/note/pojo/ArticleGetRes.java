package com.nihil.note.pojo;

import com.nihil.note.entity.NoteLabel;
import lombok.Data;

import java.util.List;

@Data
public class    ArticleGetRes {
    private String id;
    private String authorId;            // 作者 ID
    private String authorName;          // 作者 ID
    private String title;               // 文章标题
    private String des;                 // 文章描述
    private Integer columnId;           // 专栏ID
    private Integer labelId;            // 标签列表
}
