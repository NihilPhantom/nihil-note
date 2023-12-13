package com.nihil.note.controller;

import com.nihil.common.file.FileMoveParam;
import com.nihil.common.file.FileNodeDO;
import com.nihil.common.response.Result;
import com.nihil.note.client.FileNodeClient;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/node")
@ConditionalOnProperty(
        name = "note.file-node-enable",
        havingValue = "true"
)
public class NodeController {

    @Resource
    FileNodeClient fileNodeClient;

    @PutMapping("/move")
    public Result<Boolean> fileNodeMove(
            @RequestBody FileMoveParam param
    ){
        // TODO 权限检测
        return fileNodeClient.fileNodeMove(param);
    }
}
