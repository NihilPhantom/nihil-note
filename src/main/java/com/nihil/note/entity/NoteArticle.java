package com.nihil.note.entity;

import lombok.Data;

import java.util.Date;

@Data
public class NoteArticle {
    private Long id;

    private String authorId;

    private String title;

    private String des;

    private String imgHref;

    private Date createTime;

    private Integer readNum;

    private Integer commentNum;

    private Integer starNum;

    private Integer coinNum;

    private Boolean published;

    private Long parentId;
}