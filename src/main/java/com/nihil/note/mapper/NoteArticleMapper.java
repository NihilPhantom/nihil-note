package com.nihil.note.mapper;

import com.nihil.note.entity.NoteArticle;
import com.nihil.note.entity.NoteArticleLabel;
import com.nihil.note.entity.NoteArticleWithBLOBs;
import com.nihil.note.entity.NoteLabel;
import com.nihil.note.pojo.ArticleGetPARM;
import com.nihil.note.pojo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteArticleMapper {

    Integer addArticle(ArticleVO articleVO);

    Integer copyCreateArticle(String fromArticleId, String newArticleId);

    Integer addArticleLabel(Long articleId, @Param("labelList") List<NoteLabel> labelList);

    List<NoteArticleLabel> getArticleLabelByArticleId(String articleId);

    List<NoteArticle> getArticle(ArticleGetPARM articleGetPARM);

    NoteArticle getArticleByColumnIdAndName(Long pid, String title);
    NoteArticleWithBLOBs getArticleById(Long id);

    Integer updateArticle(ArticleVO articleVo);

    Integer delArticleById(Long articleId);

    List<NoteArticle> getArticleByPid(Long pid);

    List<NoteArticleWithBLOBs> getArticleByPidWithBLOBs(Long pid);
}
