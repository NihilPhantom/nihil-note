package com.nihil.note.pojo;

import com.nihil.note.entity.NoteArticle;
import com.nihil.note.entity.NoteColumn;
import lombok.Data;

import java.util.List;

@Data
public class ColumnAllData {
    Long rootId;
    List<NoteColumn> columnList;
    List<NoteArticle> articles;
}