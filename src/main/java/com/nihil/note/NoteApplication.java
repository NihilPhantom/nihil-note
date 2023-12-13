package com.nihil.note;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.net.Socket;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.nihil.note.mapper")
public class NoteApplication {
    private static Logger log = LoggerFactory.getLogger(NoteApplication.class);
    public static void main(String[] args) {

        boolean isNacosHealth = isPortOpen("localhost", 8848);
        boolean isSeataHealth = isPortOpen("localhost", 8091);

        if (!isNacosHealth) {
            log.warn("未检测 到 Nacos");
            args = updateArgs(args, "spring.cloud.discovery.enabled", "false");
            args = updateArgs(args, "spring.cloud.nacos.config.enabled", "false");
            args = updateArgs(args, "spring.cloud.nacos.discovery.enabled", "false");
            args = updateArgs(args, "file_server.url", "http://localhost:8001");
        }
        if(!isSeataHealth){
            log.warn("未检测 到 Seata、项目将不进行分布式事务处理");
            args = updateArgs(args, "seata.enabled", "false");
            args = updateArgs(args, "seata.enable-auto-data-source-proxy", "false");
        }

        SpringApplication.run(NoteApplication.class, args);
    }

    private static boolean isPortOpen(String host, int port) {
        try (Socket ignored = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String[] updateArgs(String[] args, String key, String value) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--" + key + "=")) {
                args[i] = "--" + key + "=" + value;
                return args;
            }
        }

        // 如果参数中不存在该 key，则添加新的参数
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = "--" + key + "=" + value;
        return newArgs;
    }
}