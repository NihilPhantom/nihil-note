package com.nihil.note.entity;

import lombok.Data;

import java.util.Date;

@Data
public class NoteColumn {
    private Long id;
    private String pid;
    private String authorId;
    private String name;
    private String des;
    private Integer num;
    private Boolean published;
    private Date createTime;
    private Date updateTime;
    private Long parentId;
}