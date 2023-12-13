package com.nihil.note.service.serviceimpl;

import com.nihil.common.exception.NihilException;
import com.nihil.common.file.FileConst;
import com.nihil.common.file.FileNodeDO;
import com.nihil.common.file.FolderCreateParam;
import com.nihil.common.file.FolderInfoVO;
import com.nihil.note.entity.NoteArticle;
import com.nihil.note.entity.NoteArticleWithBLOBs;
import com.nihil.note.entity.NoteColumn;
import com.nihil.note.mapper.NoteArticleMapper;
import com.nihil.note.mapper.NoteColumnMapper;
import com.nihil.note.pojo.ArticleExchangePARAM;
import com.nihil.note.pojo.ColumnAllData;
import com.nihil.note.pojo.ColumnChildrenData;
import com.nihil.note.pojo.ColumnGetPARM;
import com.nihil.note.service.NoteColumnService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class NoteColumnServiceImpl implements NoteColumnService {
    @Resource
    NoteArticleMapper noteArticleMapper;

    @Resource
    NoteColumnMapper columnMapper;

    @Value("${note.root-name}")
    String noteRootName;

    @Override
    public FolderInfoVO getColumn(ColumnGetPARM columnGetPARM) {
        // 获取用户【根专栏】的ID
        Long pid = columnMapper.getColumnByAuthorIdAndColumnName(columnGetPARM.getAuthorId(), noteRootName);
        // 如果获取失败，就创建一个【根专栏】
        if(pid == null){
            NoteColumn noteColumn = new NoteColumn();
            noteColumn.setParentId(0L);
            noteColumn.setName(noteRootName);
            noteColumn.setAuthorId(columnGetPARM.getAuthorId());
            noteColumn.setCreateTime(new Date());
            noteColumn.setPublished(columnGetPARM.getPublished());
            columnMapper.addColumn(noteColumn);
            pid = noteColumn.getId();
        }
        FolderInfoVO res = new FolderInfoVO();
        res.setFolderName(noteRootName);
        res.setFolderId(pid);
        return res;
    }

    @Override
    public Long addColumn(FolderCreateParam param, String ownId) {
        if(param.getPid() == null){
            throw new RuntimeException("专栏不存在");
        }
        NoteColumn noteColumn = new NoteColumn();
        noteColumn.setAuthorId(ownId);
        noteColumn.setName(param.getName());
        noteColumn.setParentId(param.getPid());
        Integer addRes = columnMapper.addColumn(noteColumn);
        if(addRes.equals(1)){
            columnMapper.increaseNum(param.getPid());
        }
        return noteColumn.getId();
    }

    @Override
    @Transactional
    public Integer exchangeArticle(ArticleExchangePARAM param) {

        // 1. 找到 articleId1 前后的文章
        ArticleExchangePARAM preAftArticleId1 = columnMapper.getPreAftArticleId(param.getArticleId1(),
                param.getColumnId());

        ArticleExchangePARAM preAftArticleId2 = columnMapper.getPreAftArticleId(param.getArticleId2(),
                param.getColumnId());

        if(preAftArticleId1==null || preAftArticleId2==null){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }

        // 2. 修改两个节点本身的前后文
        Integer res;
        res = columnMapper.updatePreAftArticleId(param.getColumnId(), param.getArticleId1(),
                preAftArticleId2.getArticleId1().equals(param.getArticleId1())?param.getArticleId2():preAftArticleId2.getArticleId1(),
                preAftArticleId2.getArticleId2().equals(param.getArticleId1())?param.getArticleId2():preAftArticleId2.getArticleId2()
        );

        res += columnMapper.updatePreAftArticleId(param.getColumnId(), param.getArticleId2(),
                preAftArticleId1.getArticleId1().equals(param.getArticleId2())?param.getArticleId1():preAftArticleId1.getArticleId1(),
                preAftArticleId1.getArticleId2().equals(param.getArticleId2())?param.getArticleId1():preAftArticleId1.getArticleId2()
        );

        if( res!=2 ){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return 0;
        }

        // 3. 修改两个节点前后文 的 节点
        if(!param.getArticleId2().equals(preAftArticleId1.getArticleId2())) { // 1  2 相连
            // 改 1 后面节点的【前面】
            if(!"final".equals(preAftArticleId1.getArticleId2())) {
                if(!columnMapper.updateBeforeArticleId(param.getColumnId(), preAftArticleId1.getArticleId2(), param.getArticleId2()).equals(1)){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return 0;
                }
            }

            // 改 2 前面节点的 【后面】
            if(!"start".equals(preAftArticleId2.getArticleId1())){
                if(!columnMapper.updateAfterAricleId(param.getColumnId(), preAftArticleId2.getArticleId1(), param.getArticleId1()).equals(1)){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return 0;
                }
            }
        }

        if(!param.getArticleId1().equals(preAftArticleId2.getArticleId2())) { // 2  1 相连
            if(!"start".equals(preAftArticleId1.getArticleId1())){
                if(!columnMapper.updateAfterAricleId(param.getColumnId(), preAftArticleId1.getArticleId1(), param.getArticleId2()).equals(1)){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return 0;
                }
            }

            if(!"final".equals(preAftArticleId2.getArticleId2())) {
                if(!columnMapper.updateBeforeArticleId(param.getColumnId(), preAftArticleId2.getArticleId2(), param.getArticleId1()).equals(1)){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return 0;
                }
            }
        }
        return 1;
    }

    @Override
    public Boolean delColumn(Long id) {
        // 获取文章的数目，如果文章的数目不是0，则提示错误
        NoteColumn column = columnMapper.getById(id);
        if(!column.getNum().equals(0)){
            throw new RuntimeException("专栏任然存在文章");
        }
        Integer deleteRes = columnMapper.deleteNoteColumn(id);
        if(deleteRes.equals(1)){
            deleteRes = columnMapper.decreaseNum(column.getParentId());
        }
        return deleteRes.equals(1);
    }

    @Override
    public NoteColumn getColumnByParentIdAndName(long pid, String name) {
        return columnMapper.getColumnByParentIdAndName(pid, name);
    }

    /**
     * 使用层次遍历法，当前专栏
     * @param columnId
     * @return
     */
//    @Override
//    public ColumnAllData out2json(Long columnId) {
//        List<NoteColumn> columnList = new ArrayList<>();
//        List<NoteArticle> articles = new ArrayList<>();
//        // 获取根节点的信息
//        NoteColumn rootColumn = getColumnDetailById(columnId);
//        if(rootColumn == null){
//            throw new NihilException("请正确选择专栏");
//        }
//        columnList.add(rootColumn);
//        int prePosition = 0; // 指针
//        while (prePosition >= columnList.size()) {
//            ColumnChildrenData childrenData = getChildrenByPid(columnList.get(prePosition++).getParentId());
//            for (NoteColumn column : childrenData.getColumns()) {
//                columnList.add(column);
//            }
//            for (NoteArticle article : childrenData.getArticles()) {
//                articles.add(article);
//            }
//        }
//        ColumnAllData res = new ColumnAllData();
//        res.setRootId(rootColumn.getId());
//        res.setRootName(rootColumn.getName());
//        res.setDes(rootColumn.getDes());
//        res.setColumns(columnList);
//        res.setArticles(articles);
//        return res;
//    }

    @Override
    public List<FileNodeDO> getArticleList(Long pid, String authorId) {
        List<NoteColumn> columnList = columnMapper.getColumnListByPid(pid);
        if(columnList == null) columnList = new ArrayList<>();
        List<NoteArticle> articles = noteArticleMapper.getArticleByPid(pid);

        List<FileNodeDO> childFiles1 = new ArrayList<>(columnList.stream().map(item -> {
            FileNodeDO fileNodeDO = new FileNodeDO();
            fileNodeDO.setId(item.getId());
            fileNodeDO.setName(item.getName());
            fileNodeDO.setParentId(item.getParentId());
            fileNodeDO.setType(FileConst.TYPE_FOLDER);
            fileNodeDO.setOwner(item.getAuthorId());
            fileNodeDO.setCreateTime(item.getCreateTime());
            fileNodeDO.setFileNum(item.getNum());
            return fileNodeDO;
        }).toList());

        List<FileNodeDO> childFiles2 = articles.stream().map(item -> {
            FileNodeDO fileNodeDO = new FileNodeDO();
            fileNodeDO.setId(item.getId());
            fileNodeDO.setName(item.getTitle());
            fileNodeDO.setParentId(item.getParentId());
            fileNodeDO.setType(FileConst.TYPE_Markdown);
            fileNodeDO.setOwner(item.getAuthorId());
            fileNodeDO.setCreateTime(item.getCreateTime());
            fileNodeDO.setFileNum(0);
            return fileNodeDO;
        }).toList();
        childFiles1.addAll(childFiles2);
        return childFiles1;
    }

    @Override
    public Boolean deleteArticle(Long articleId) {
        Integer res = noteArticleMapper.delArticleById(articleId);
        if(res.equals(1)){
            res = columnMapper.decreaseNumByArticleId(articleId);
        }
        return res.equals(1);
    }

    @Override
    public NoteColumn getColumnDetailById(Long id){
        return columnMapper.getColumnDetailById(id);
    }

    @Override
    public ColumnChildrenData getChildrenByPid(Long pid) {
        List<NoteColumn> columnList = columnMapper.getColumnListByPid(pid);
        List<NoteArticleWithBLOBs> articleList = noteArticleMapper.getArticleByPidWithBLOBs(pid);
        ColumnChildrenData res = new ColumnChildrenData();
        res.setColumns(columnList);
        res.setArticles(articleList);
        return res;
    }
}