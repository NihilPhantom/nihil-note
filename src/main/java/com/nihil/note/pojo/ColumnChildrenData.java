package com.nihil.note.pojo;

import com.nihil.note.entity.NoteArticleWithBLOBs;
import com.nihil.note.entity.NoteColumn;
import lombok.Data;

import java.util.List;

@Data
public class ColumnChildrenData {
    Long rootId;
    String rootName;
    String des;
    List<NoteColumn> columns;
    List<NoteArticleWithBLOBs> articles;
}