package com.nihil.note.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.nihil.common.exception.NihilException;
import com.nihil.common.file.FileNodeDO;
import com.nihil.common.file.FolderCreateParam;
import com.nihil.common.file.FolderInfoVO;
import com.nihil.common.response.Result;
import com.nihil.note.entity.NoteArticleWithBLOBs;
import com.nihil.note.entity.NoteColumn;
import com.nihil.note.pojo.*;
import com.nihil.note.service.NoteArticleService;
import com.nihil.note.service.NoteColumnService;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/column")
public class ColumnController{
    @Resource
    NoteColumnService noteColumnService;

    @Resource
    NoteArticleService noteArticleService;

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
        List<NoteColumnWithArticles> columnList = new ArrayList<>();

        // 获取根节点的信息
        NoteColumn rootColumn = noteColumnService.getColumnDetailById(columnId);
        if(rootColumn == null){
            throw new NihilException("请正确选择专栏");
        }
        columnList.add(new NoteColumnWithArticles(rootColumn));


        int prePosition = 0; // 指针
        while (prePosition < columnList.size()) {
            ColumnChildrenData childrenData = noteColumnService.getChildrenByPid(columnList.get(prePosition).getId());
            for (NoteColumn column : childrenData.getColumns()) {
                columnList.add(new NoteColumnWithArticles(column));
            }
            for (NoteArticleWithBLOBs articleWithBLOBs : childrenData.getArticles()) {
                columnList.get(prePosition).getArticleList().add(articleWithBLOBs);
            }
            prePosition++;
        }
        ColumnAllData res = new ColumnAllData();
        res.setRootId(rootColumn.getId());
        res.setRootName(rootColumn.getName());
        res.setDes(rootColumn.getDes());
        res.setColumnWithArticles(columnList);
        return Result.success(res);
    }


    @PostMapping("loadFromJson")
    public Result<ColumnAllData> out2json(
            @RequestParam Long columnId,
            @RequestParam("json") MultipartFile json
    ) {

        // TODO 权限
        String ownId = "1";

        if(json.isEmpty()){
            throw new NihilException("json数据为空");
        }

        try {
            // 获取文件的字节数据
            byte[] bytes = json.getBytes();
            // 将字节数据转换为字符串
            String jsonStr = new String(bytes);
            // 使用FastJson将JSON字符串转换为实体类对象
            TypeReference<Result<ColumnAllData>> typeRef = new TypeReference<>() {};
            ColumnAllData loadData = JSON.parseObject(jsonStr, typeRef).getData();

            /* 存放 json中的专栏ID 到 数据库中的专栏的映射, 方便进行查询 */
            class ColumnInDB{
                Long id;
                boolean isNew;
                List<NoteColumn> subColumnList;
            }

            Map<Long, ColumnInDB> map4OldColumnId2ColumnInDB = new HashMap<>();
            ColumnInDB rootColunmInDB = new ColumnInDB();
            rootColunmInDB.id = columnId;
            rootColunmInDB.isNew = false;
            boolean unfindRoot = true;

            for(NoteColumnWithArticles columnInJson: loadData.getColumnWithArticles()){  // 遍历json中的每个专栏

                // 获取 Json中的专栏 放到数据库中 并且返回在数据库中的Id
                Long oldColumnId = columnInJson.getId();
                Long oldParentId = columnInJson.getParentId();
                Long columnIdInDataBase = 0L;  // 专栏存到数据库后，应该分配的Id
                boolean isNew = true;     // 是否的专栏已经在数据库中
                if(unfindRoot && columnInJson.getId().equals(loadData.getRootId())){
                    columnIdInDataBase = columnId;
                    isNew = false;

                }
                else{
                    ColumnInDB parentInDB = map4OldColumnId2ColumnInDB.get(oldParentId);
                    if(oldColumnId.equals(loadData.getRootId())){
                        continue;
                    }
                    if(!parentInDB.isNew && parentInDB.subColumnList!=null){
                        for(NoteColumn column : parentInDB.subColumnList){
                            if(column.getName().equals(columnInJson.getName())){
                                isNew = false;
                                columnIdInDataBase = column.getId();
                                break;
                            }
                        }
                    }
                    if(isNew){ // 不在数据库中就进行添加
                        FolderCreateParam folderCreateParam = new FolderCreateParam();
                        folderCreateParam.setPid(parentInDB.id);
                        folderCreateParam.setName(columnInJson.getName());
                        columnIdInDataBase = noteColumnService.addColumn(folderCreateParam, ownId);
                    }
                }

                // 获取子文章信息，将子文章存放到数据库中
                if (!isNew){  // 如果专栏已经在数据库中，则需要更新文章的名字，以免冲突
                    ColumnChildrenData columnChildrenData = noteColumnService.getChildrenByPid(columnIdInDataBase);
                    HashMap<String, Boolean> map4Name2Article = new HashMap<>();
                    for(NoteArticleWithBLOBs article : columnChildrenData.getArticles()){
                        map4Name2Article.put(article.getTitle(), true);
                    }
                    for(NoteArticleWithBLOBs articleInJson : columnInJson.getArticleList()){
                        if(map4Name2Article.containsKey(articleInJson.getTitle())){
                            articleInJson.setTitle(articleInJson.getTitle() + "(new)");
                        }
                        ArticleVO articleVO = new ArticleVO(articleInJson);
                        articleVO.setColumnId(columnIdInDataBase);
                        noteArticleService.addArticle(articleVO);
                    }

                    // 将这个专栏放到HashMap中，以便其子节点获取信息, （数据导出使用的树的先序遍历，所以保证了父级节点先在list中出现）
                    ColumnInDB columnInDB = new ColumnInDB();
                    columnInDB.id = columnIdInDataBase;
                    columnInDB.isNew = false;
                    columnInDB.subColumnList = columnChildrenData.getColumns();
                    map4OldColumnId2ColumnInDB.put(oldColumnId, columnInDB);
                }
                else{ // 否则就直接将文章添加到专栏
                    for(NoteArticleWithBLOBs articleInJson : columnInJson.getArticleList()){
                        ArticleVO articleVO = new ArticleVO(articleInJson);
                        articleVO.setColumnId(columnIdInDataBase);
                        noteArticleService.addArticle(articleVO);
                    }
                    // 将这个专栏放到HashMap中，以便其子节点获取信息, （数据导出使用的树的先序遍历，所以保证了父级节点先在list中出现）
                    ColumnInDB columnInDB = new ColumnInDB();
                    columnInDB.id = columnIdInDataBase;
                    columnInDB.isNew = false;
                    map4OldColumnId2ColumnInDB.put(oldColumnId, columnInDB);
                }
            }
            return Result.success();
        } catch (IOException e) {
            throw new NihilException(e.getMessage());
        }
    }
}
