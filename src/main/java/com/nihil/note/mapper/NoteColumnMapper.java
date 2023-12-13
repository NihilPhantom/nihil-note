package com.nihil.note.mapper;

import com.nihil.note.entity.NoteArticle;
import com.nihil.note.entity.NoteArticleColumn;
import com.nihil.note.entity.NoteColumn;
import com.nihil.note.pojo.ArticleExchangePARAM;
import com.nihil.note.pojo.ColumnGetPARM;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoteColumnMapper {

    List<NoteColumn> getColumn(ColumnGetPARM columnGetPARM);

    Integer deleteNoteColumn(Long columnId);

    int changeArticleNum(String columnId, Integer num);

    String getLastArticle(String columnId);

    int updateLastNext(NoteArticleColumn columnArticle);

    int addArticleToColumn(NoteArticleColumn noteArticleColumn);

    List<NoteArticle> getArticleList(String id, boolean isAuthor);

    Integer updateAfterAricleId(String columnId, String articleId, String aftArticleId);

    Integer updateBeforeArticleId(String columnId, String articleId, String preArticleId);

    Integer getOneArticleInColumnNum(String articleId);

    NoteArticleColumn getNoteArticleColumn(String columnId, String articleId);

    Integer deleteNoteArticleColumn(String columnId, String articleId);

    Integer addColumn(NoteColumn param);

    ArticleExchangePARAM getPreAftArticleId(String articleId, String columnId);

    Integer updatePreAftArticleId(String columnId, String articleId, String preArticleId, String aftArticleId);

    Long getColumnByAuthorIdAndColumnName(String authorId, String noteRootName);

    List<NoteColumn> getColumnListByPid(Long pid);
    Integer decreaseNumByArticleId(Long articleId);

    Integer increaseNum(Long id);

    Integer decreaseNumByColumnId(Long columnId);

    Long getParentId(Long id);

    Integer decreaseNum(Long id);

    NoteColumn getById(Long id);

    NoteColumn getColumnByParentIdAndName(Long pid, String name);
}
