package com.nihil.note.client;

import com.nihil.common.file.*;
import com.nihil.common.response.Result;
import com.nihil.note.entity.NoteArticleWithBLOBs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "nihil-order-file-server", path = "/nihil-order-file-server", url="${file_server.url:}")
public interface FileNodeClient {
    /**
     * 修改文件名
     * @param id
     * @param newName
     * @return
     */
    @PutMapping("/node/name/{id}")
    Result<Boolean> updateFileNodeName(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "name") String newName
    );

    @PostMapping("/node/folder")
    Result<Long> createFolder(
            @RequestBody FolderCreateParam param
    );

    @GetMapping("/node/getChildNodesByParentName")
    Result<FolderInfoVO> getChildNodesByParentName(
            @RequestParam(name = "pName") String pName
    );

    // 获取一个文件夹下的所有项目
    @GetMapping("/node/child/{pid}")
    Result<List<FileNodeDO>> getChildNodesByParentId(
            @PathVariable(name = "pid") Long pid
    );

    /**
     * 添加文件方法
     */
    @PostMapping("/meili/upload")
    Result<Long> meiliUpload(
            @RequestBody MeiliUploadParam param
    );

    @DeleteMapping("/meili/document/{index}/{id}")
    Result<Boolean> delDocument(
            @PathVariable(value = "index") String index,
            @PathVariable(value = "id") Long id
    );

    /**
     * 修改文件内容
     * @param index
     * @param json
     * @return
     */
    @PutMapping("/meili/document/{index}/{id}")
    Result<Boolean> updateDocument(
            @PathVariable(value = "index") String index,
            @PathVariable(value = "id") Long id,
            @RequestBody String json
    );

    @GetMapping("/meili/document/{index}/{id}")
    NoteArticleWithBLOBs getDocument(
            @PathVariable(value = "index") String index,
            @PathVariable(value = "id") Long id
    );

    @DeleteMapping ("/node/safeDelete/{id}")
    Result<Boolean> safeDelete(
            @PathVariable(name = "id") Long id
    );

    @PutMapping ("/node/move")
    Result<Boolean> fileNodeMove(FileMoveParam param);

    @PutMapping("/node/node")
    Result<Boolean> updateFileNode(@RequestBody FileNodeDO param);

    @GetMapping("/node")
    Result<List<FileNodeDO>> getNode(
            @RequestParam(required = false) Long pid,
            @RequestParam(required = false) String name
    );
}
