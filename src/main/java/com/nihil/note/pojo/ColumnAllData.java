package com.nihil.note.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ColumnAllData {
    Long rootId;
    String rootName;
    String des;
    List<NoteColumnWithArticles> columnWithArticles;
}