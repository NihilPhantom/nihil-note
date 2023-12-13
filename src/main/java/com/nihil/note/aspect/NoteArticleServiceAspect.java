package com.nihil.note.aspect;

import com.alibaba.fastjson2.JSON;
import com.nihil.common.file.MeiliUploadParam;
import com.nihil.common.response.Result;
import com.nihil.note.client.FileNodeClient;
import com.nihil.note.mapper.NoteFileMapper;
import com.nihil.note.pojo.ArticleVO;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(
        name = "note.file-node-enable",
        havingValue = "true"
)
public class NoteArticleServiceAspect {

    @Resource
    FileNodeClient fileNodeClient;

    @Resource
    NoteFileMapper noteFileMapper;

    @Value("${meili.index}")
    String meiliIndex;

    /**
     * 添加文章的【循环增强方法】：将文章添加到 搜索引擎【MeiliSearch】中
     */
    @Around("execution(* com.nihil.note.service.NoteArticleService.addArticle(..))")
    public Object aroundAddArticle(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取原始方法的输入参数
        Object[] args = joinPoint.getArgs();
        ArticleVO articleVO = (ArticleVO) args[0];

        // 调用原始方法， 将数据存入数据库 ，并且获取到数据库自增的主键
        Long fileParentId = articleVO.getColumnId();
        Long articleColumnId = noteFileMapper.getColumnIdByFileId(articleVO.getColumnId());
        articleVO.setColumnId(articleColumnId);
        args[0] = articleVO;
        Long articleId = (Long) joinPoint.proceed(args);

        // 将文章存到 搜索引擎 MeiliSearch 中
        articleVO.setId(articleId);
        MeiliUploadParam param = new MeiliUploadParam();
        param.setFileName(articleVO.getTitle());
        param.setIndex(meiliIndex);
        param.setJson(JSON.toJSONString(articleVO));
        param.setPid(fileParentId);
        Result<Long> addFileRes = fileNodeClient.meiliUpload(param);

        // 创建一个文章 到 文件之间的对应关系
        noteFileMapper.addNoteFile(articleId, addFileRes.getData());

        // 返回原始方法的返回值
        return addFileRes.getData();
    }

    @Around("execution(* com.nihil.note.service.NoteArticleService.deleteArticleById(..))")
    public Object aroundDeleteArticleById(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原方法的参数
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];

        // 在原方法执行后的逻辑
        fileNodeClient.safeDelete(fileId);

        Long articleId = noteFileMapper.getArticleIdByFileId(fileId);
        noteFileMapper.delColumnFile(articleId);
        noteFileMapper.delArticleFile(articleId);
        args[0] = articleId;
        Boolean res = (Boolean) joinPoint.proceed(args);
        return res;
    }

    @Around("execution(* com.nihil.note.service.NoteArticleService.updateArticle(..))")
    public Object aroundUpdateArticle(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取原始方法的输入参数
        Object[] args = joinPoint.getArgs();
        ArticleVO articleVO = (ArticleVO) args[0];
        String owner = args[1].toString();

        // 获取文件系统中的数据ID
        Long fileId= articleVO.getId();

        // 调用原始方法， 修改数据库中文章
        Long articleId = noteFileMapper.getArticleIdByFileId(fileId);
        articleVO.setId(articleId);
        joinPoint.proceed(args);



        // 对【MeiLiSearch】中的 文档数据进行修改
        if(articleVO.getTitle() != null && !articleVO.getTitle().equals("")){
            fileNodeClient.updateFileNodeName(fileId, articleVO.getTitle());
        }
        articleVO.setId(fileId);
        String json = JSON.toJSONString(articleVO);
        fileNodeClient.updateDocument(meiliIndex, fileId, json);
        // 返回原始方法的返回值
        return 1;
    }

    @Around("execution(* com.nihil.note.service.NoteArticleService.getArticleDetail(..))")
    public Object aroundGetArticleDetail(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原始方法的输入参数
        Object[] args = joinPoint.getArgs();
        Long fileId = (Long) args[0];
        return fileNodeClient.getDocument(meiliIndex, fileId);
    }
}
