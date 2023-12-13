package com.nihil.note.service;

import com.nihil.common.file.FileNodeDO;
import com.nihil.common.file.FolderCreateParam;
import com.nihil.common.file.FolderInfoVO;
import com.nihil.note.entity.NoteColumn;
import com.nihil.note.pojo.ArticleExchangePARAM;
import com.nihil.note.pojo.ColumnAllData;
import com.nihil.note.pojo.ColumnChildrenData;
import com.nihil.note.pojo.ColumnGetPARM;

import java.util.List;

public interface NoteColumnService {
    FolderInfoVO getColumn(ColumnGetPARM columnGetPARM);

    List<FileNodeDO> getArticleList(Long pid, String authorId);

    Boolean deleteArticle(Long articleId);

    Long addColumn(FolderCreateParam param, String ownId);

    Integer exchangeArticle(ArticleExchangePARAM param);

    Boolean delColumn(Long id);

    NoteColumn getColumnByParentIdAndName(long newId, String oldNodeName);

    NoteColumn getColumnDetailById(Long id);

    ColumnChildrenData getChildrenByPid(Long pid);
}
