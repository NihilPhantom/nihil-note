package com.nihil.note.aspect;


import com.alibaba.fastjson2.JSON;
import com.nihil.common.file.FileNodeDO;
import com.nihil.common.file.FolderCreateParam;
import com.nihil.common.file.FolderInfoVO;
import com.nihil.common.response.Result;
import com.nihil.note.client.FileNodeClient;
import com.nihil.note.entity.NoteColumn;
import com.nihil.note.entity.NoteColumnFile;
import com.nihil.note.mapper.NoteColumnMapper;
import com.nihil.note.mapper.NoteFileMapper;
import com.nihil.note.pojo.ColumnGetPARM;
import feign.FeignException;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
@ConditionalOnProperty(
        name = "note.file-node-enable",
        havingValue = "true"
)
public class NoteColumnServiceAspect {
    @Resource
    FileNodeClient fileNodeClient;

    @Resource
    NoteFileMapper noteFileMapper;

    @Resource
    NoteColumnMapper noteColumnMapper;

    @Value("${meili.index}")
    String meiliIndex;

    @Value("${note.root-name}")
    String noteRootName;

    @Around("execution(* com.nihil.note.service.NoteColumnService.getColumn(..))")
    public Object aroundGetColumn(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        ColumnGetPARM columnGetPARM = (ColumnGetPARM) args[0];

        // 在原方法执行前的逻辑
        System.out.println("Before MyMethod");
        Result<FolderInfoVO> childNodes = new Result<>();
        try {
            childNodes = fileNodeClient.getChildNodesByParentName(noteRootName);
        }catch (FeignException e){
            Optional<ByteBuffer> response = e.responseBody(); // 获取响应
            ByteBuffer buffer = response.get();
            Result result = JSON.parseObject(buffer, Result.class);
            if (result.getCode() == 404){
                FolderCreateParam param = new FolderCreateParam();
                param.setName(noteRootName);
                param.setPid(1L);
                Result<Long> folderIdRes = fileNodeClient.createFolder(param);
                Long folderId = folderIdRes.getData();
                FolderInfoVO column = (FolderInfoVO) joinPoint.proceed(args);
                noteFileMapper.addColumnFile(column.getFolderId(), folderId);
                childNodes = fileNodeClient.getChildNodesByParentName(noteRootName);
            }
        }
        if (childNodes.getData()!=null){
            return childNodes.getData();
        }
        else  {
            // 执行原方法，并传递参数
            Object result = joinPoint.proceed(args);
            // 在原方法执行后的逻辑
            System.out.println("After MyMethod");

            return result;
        }
    }

    @Around("execution(* com.nihil.note.service.NoteColumnService.addColumn(..))")
    public Object aroundAddColumn(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        FolderCreateParam param = (FolderCreateParam) args[0];

        Long fileParentId = param.getPid();
        Long columnParentId = noteFileMapper.getColumnIdByFileId(fileParentId);
        param.setPid(columnParentId);
        args[0] = param;

        Long columnId = (Long) joinPoint.proceed(args);
        param.setPid(fileParentId);
        // 在原方法执行后的逻辑
        Result<Long> folderIdRes = fileNodeClient.createFolder(param);
        Long folderId = folderIdRes.getData();
        noteFileMapper.addColumnFile(columnId, folderId);
        return folderIdRes.getData();
    }

    @Around("execution(* com.nihil.note.service.NoteColumnService.delColumn(..))")
    public Object aroundDelColumn(ProceedingJoinPoint joinPoint) throws Throwable {
        // 删除文件系统中的 文章数据
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];
        Result<Boolean> delRes = fileNodeClient.safeDelete(fileId);

        // 删除连接数据表中的数据
        Long columnId;
        if(delRes.getData()){
            columnId = noteFileMapper.getColumnIdByFileId(fileId);
            noteFileMapper.delColumnFile(columnId);
            args[0] = columnId;
        }
        Boolean res = (Boolean) joinPoint.proceed(args);
        return res;
    }

    @Around("execution(* com.nihil.note.service.NoteColumnService.getArticleList(..))")
    public Object aroundGetArticleList(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        Long pid = (Long) args[0];
        Result<List<FileNodeDO>> res = fileNodeClient.getChildNodesByParentId(pid);
        return res.getData();
    }

    @Around("execution(* com.nihil.note.service.NoteColumnService.getColumnByParentIdAndName(..))")
    public Object aroundGetColumnByParentIdAndName(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        Long pid = (Long) args[0];
        String name = (String) args[1];
        List<FileNodeDO> fileNodeList = fileNodeClient.getNode(pid, name).getData();
        if(fileNodeList.size()==0){
            return null;
        }
        NoteColumn noteColumn = new NoteColumn();
        FileNodeDO fileNode = fileNodeList.get(0);
        noteColumn.setId(fileNode.getId());
        noteColumn.setParentId(fileNode.getParentId());
        noteColumn.setAuthorId(fileNode.getOwner());
        noteColumn.setName(fileNode.getName());
        noteColumn.setNum(fileNode.getFileNum());
        noteColumn.setCreateTime(fileNode.getCreateTime());
        return noteColumn;
    }

    @Around("execution(* com.nihil.note.service.NoteColumnService.deleteArticle(..))")
    public Object aroundDeleteArticle(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];
        Result<Boolean> fileDeleteRes = fileNodeClient.delDocument(meiliIndex, fileId);
        Long articleId = noteFileMapper.getArticleIdByFileId(fileId);
        noteFileMapper.delArticleFile(articleId);
        args[0] = articleId;
        Boolean res = (Boolean) joinPoint.proceed(args);
        return res;
    }

}
