package com.nihil.note.service.serviceimpl;

import com.nihil.note.client.FileNodeClient;
import com.nihil.note.entity.NoteArticle;
import com.nihil.note.entity.NoteArticleWithBLOBs;
import com.nihil.note.entity.NoteColumn;
import com.nihil.note.mapper.NoteArticleMapper;
import com.nihil.note.mapper.NoteColumnMapper;
import com.nihil.note.pojo.ArticleGetPARM;
import com.nihil.note.pojo.ArticleVO;
import com.nihil.note.service.NoteArticleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteArticleServiceImpl implements NoteArticleService {
    @Resource
    NoteArticleMapper noteArticleMapper;

    @Resource
    NoteColumnMapper noteColumnMapper;

    @Resource
    FileNodeClient fileNodeClient;

    @Value("${note.root-name}")
    String noteRootName;


//    @GlobalTransactional
    public Long addArticle(ArticleVO articleVO) {

        // 如果没有 getColumnId
        if (articleVO.getColumnId() == null) {
            Long columnId = noteColumnMapper.getColumnByAuthorIdAndColumnName(articleVO.getAuthorId(), noteRootName);
            articleVO.setColumnId(columnId);
        }

        // 将文章添加到数据库，并拿到返回的id
        Integer res = noteArticleMapper.addArticle(articleVO);
        if(res.equals(1)){
            noteColumnMapper.increaseNum(articleVO.getColumnId());
        }

        // If there are labels, add them into database
        if (articleVO.getLabelList() != null && articleVO.getLabelList().size() != 0) {
            noteArticleMapper.addArticleLabel(articleVO.getId(), articleVO.getLabelList());
        }

        return articleVO.getId();
    }

    /**
     * 拷贝创建一条文章记录，此方法将复制一个文章记录，并插入到指定文章的后面
     * @return
     */
//    @Transactional
//    public Integer copyCreateArticle(CopyCreateArticlePARAM param) {
//
//        // 获取文件信息
//        // Generate ID by random function
//        String articleId = RandomUtil.getRandomString();
//
//        param.setArticleId(articleId);
//
//        ArticleExchangePARAM toColumnPreAftArticleId = noteColumnMapper.getPreAftArticleId(
//                param.getToBeforeArticleId(), param.getToColumnId());
//
//        // If there are labels, add them into database
//        List<NoteArticleLabel> labelList = noteArticleMapper.getArticleLabelByArticleId(param.getFromArticleId());
//        if (labelList != null && labelList.size() != 0) {
////            noteArticleMapper.addArticleLabel(articleId, labelList);
//        }
//
//        // 拷贝创建一条记录
//        int res = noteArticleMapper.copyCreateArticle(param.getFromArticleId(), param.getArticleId());
//        if (res == 0) {
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return null;
//        }
//
//        // 修改专栏的数量
//        res = noteColumnMapper.changeArticleNum(param.getToColumnId(), 1);
//        if (res == 0) {
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return null;
//        }
//
//        // 添加一条对应关系
//        res = 0;
//        // rticleId, String columnId, String preArticleId, String aftArticleId
//        NoteArticleColumn noteArticleColumn = new NoteArticleColumn(
//                articleId, param.getToColumnId(), param.getToBeforeArticleId(), toColumnPreAftArticleId.getArticleId2());
//
//        // 修改后一条数据的前一条数据
//        if(!"final".equals(toColumnPreAftArticleId.getArticleId2())) {
//            if(!noteColumnMapper.updateBeforeArticleId(param.getToColumnId(), toColumnPreAftArticleId.getArticleId2(), articleId).equals(1)){
//                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return 0;
//            }
//        }
//
//        // 修改前的一条数据的后一条数据
//        if(!noteColumnMapper.updateAfterAricleId(param.getToColumnId(), param.getToBeforeArticleId(), param.getArticleId()).equals(1)) {
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return 0;
//        }
//
//        // 添加这条数据
//        return noteColumnMapper.addArticleToColumn(noteArticleColumn);
//    }

    @Override
    public Boolean deleteArticleById(Long articleID) {
        NoteArticle article = noteArticleMapper.getArticleById(articleID);
        Integer deleteRes = noteArticleMapper.delArticleById(articleID);
        if(deleteRes.equals(1)){
            deleteRes = noteColumnMapper.decreaseNum(article.getParentId());
        }
        return deleteRes.equals(1);
    }


    /**
     * 更新【数据库】中的【文章】
     */
    @Override
    public Integer updateArticle(ArticleVO articleVo, String owner) {
        // TODO 如果修改标签的话，需要在修改文章的同时对标签列表进行修改
        return noteArticleMapper.updateArticle(articleVo);
    }

    @Override
    public List<NoteArticle> getArticleById(String articleID) {
        return null;
    }

    @Override
    public List<NoteArticle> getArticleByLabelId(Integer labelId) {
        return null;
    }

    @Override
    public List<NoteArticle> getArticle(ArticleGetPARM parm) {
        return noteArticleMapper.getArticle(parm);
    }

    @Override
    public NoteArticleWithBLOBs getArticleDetail(Long id) {
//        return fileNodeClient.getDocument(FileConst.TYPE_Markdown, id);
        return noteArticleMapper.getArticleById(id);
    }
}
