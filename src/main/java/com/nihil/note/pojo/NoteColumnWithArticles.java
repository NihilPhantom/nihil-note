package com.nihil.note.pojo;


import com.nihil.note.entity.NoteArticleWithBLOBs;
import com.nihil.note.entity.NoteColumn;

import java.util.ArrayList;
import java.util.List;

public class NoteColumnWithArticles extends NoteColumn {
    private List<NoteArticleWithBLOBs> articleList;

    public List<NoteArticleWithBLOBs> getArticleList() {
        return articleList;
    }

    public void setArticleList(List<NoteArticleWithBLOBs> articleList) {
        this.articleList = articleList;
    }
    NoteColumnWithArticles(){
        this.articleList = new ArrayList<>();
    }

    public NoteColumnWithArticles(NoteColumn p){
        this.id = p.getId();
        this.authorId = p.getAuthorId();
        this.name = p.getName();
        this.des = p.getDes();
        this.num = p.getNum();
        this.published = p.getPublished();
        this.createTime = p.getCreateTime();
        this.updateTime = p.getUpdateTime();
        this.parentId = p.getParentId();
        this.articleList = new ArrayList<>();
    }
}
