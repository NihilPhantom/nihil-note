package com.nihil.note.controller;

import com.nihil.common.file.FileNodeDO;
import com.nihil.common.file.FolderCreateParam;
import com.nihil.common.file.FolderInfoVO;
import com.nihil.common.response.Result;
import com.nihil.note.pojo.ColumnAllData;
import com.nihil.note.pojo.ColumnGetPARM;
import com.nihil.note.service.NoteColumnService;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/column")
public class ColumnController{
    @Resource
    NoteColumnService noteColumnService;

    // 从用户根路径获取所有的专栏
    @GetMapping("/column")
    public Result<FolderInfoVO> getColumn(@Validated ColumnGetPARM param){
        param.setAuthorId("1");
        return Result.success(noteColumnService.getColumn(param));
    }

    // 创建一个专栏
    @PostMapping("column")
    public Result<Long> addColumn(
            @RequestBody FolderCreateParam param
    ){
        // TODO 权限检测
        String ownId = "1";
        return Result.success(noteColumnService.addColumn(param, ownId));
    }


    @DeleteMapping("column")
    public Result<Boolean> delColumn(@PathParam("id") Long id){
        // TODO 权限检测
        return Result.success(noteColumnService.delColumn(id));
    }


    //    @PutMapping("/exchangeArticle")
    //    public Result<Boolean> exchangeArticle(
    //            @RequestParam(name="from") Long from,
    //            @RequestParam(name="to") String to
    //    ){
    //        // TODO 权限检测
    //        return noteColumnService.exchangeArticle(param)
    //    }


    @GetMapping("/articleList")
    public Result<List<FileNodeDO>> getArticleList(@RequestParam Long id){
        // TODO 权限
        String ownId = "1";
        return Result.success(noteColumnService.getArticleList(id, ownId));
    }

    @DeleteMapping("/article")
    public Result<Boolean> deleteArticleFromColumn(@RequestParam Long articleId){
        return Result.success(noteColumnService.deleteArticle(articleId));
    }

    @GetMapping("/out2json")
    public Result<ColumnAllData> out2json(
            @RequestParam Long columnId
    ){
        return Result.success(noteColumnService.out2json(columnId));
    }
}
