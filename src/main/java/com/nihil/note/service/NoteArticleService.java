package com.nihil.note.service;

import com.nihil.note.entity.NoteArticle;
import com.nihil.note.pojo.ArticleGetPARM;
import com.nihil.note.pojo.ArticleVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public interface NoteArticleService {

    Long addArticle(ArticleVO articleVo);

    Boolean deleteArticleById(Long articleID);

    Integer updateArticle(ArticleVO articleVo, String owner);

    List<NoteArticle> getArticleById(String articleID);

    List<NoteArticle> getArticleByLabelId(Integer labelId);

    List<NoteArticle> getArticle(ArticleGetPARM parm);

    NoteArticle getArticleDetail(Long id);
}
