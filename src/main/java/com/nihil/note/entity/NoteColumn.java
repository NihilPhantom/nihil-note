package com.nihil.note.entity;

import lombok.Data;

import java.util.Date;

@Data
public class NoteColumn {
    protected Long id;
    protected String authorId;
    protected String name;
    protected String des;
    protected Integer num;
    protected Boolean published;
    protected Date createTime;
    protected Date updateTime;
    protected Long parentId;
}