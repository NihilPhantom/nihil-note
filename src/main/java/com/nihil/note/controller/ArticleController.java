package com.nihil.note.controller;

import com.nihil.common.response.Result;
import com.nihil.note.entity.NoteArticleWithBLOBs;
import com.nihil.note.pojo.ArticleVO;
import com.nihil.note.service.serviceimpl.NoteArticleServiceImpl;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Delete;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    NoteArticleServiceImpl noteArticleService;

    // Add Article
    @PostMapping("/article")
    public Result<Long> addArticle(@RequestBody ArticleVO articleVO){
        articleVO.setAuthorId("1");
        Long articleId = noteArticleService.addArticle(articleVO);
        return Result.success(articleId);
    }

    // 更新文章
    @PutMapping()
    public Result<Boolean> updateArticle(@RequestBody ArticleVO articleVO){
        String owner = "1";
        noteArticleService.updateArticle(articleVO, owner);
        return Result.success(true);
    }

//    // 复制一个文件
//    @PostMapping("/copyCreate")
//    public Result<Boolean> copyCreateArticle(@RequestBody CopyCreateArticlePARAM param){
//        noteArticleService.copyCreateArticle(param);
//        return Result.success(true);
//    }

    // 获取文章详情
    @GetMapping("/detail")
    public Result<NoteArticleWithBLOBs> getArticleDetail(@RequestParam Long id){
        return Result.success(noteArticleService.getArticleDetail(id));
    }

    @DeleteMapping()
    public Result<Boolean> delArticle(@RequestParam Long id){
        return Result.success(noteArticleService.deleteArticleById(id));
    }

}
