package com.nihil.note.tools;


import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootTest
public class Dataloader {

    @Test
    void loadFromJson(){
    }

    @Test
    void testDB() {
        String oldDataBase = "test";
        String oldUser = "user";
        String oldPass = "user123456";

        DataSource dataSource = DataSourceBuilder
                .create()
                .url("jdbc:mysql://localhost:3306/"+ oldDataBase +"?serverTimezone=UTC&useAffectedRows=true")
                .username(oldUser)
                .password(oldPass)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();

        // try(Resource res = xxx)块退出时，会自动调用res.close()方法，关闭资源。
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()
        ) {
            String sql = "SELECT * FROM table1";
            ResultSet resultSet = statement.executeQuery(sql);

            // 处理查询结果
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String key1 = resultSet.getString("key1");
                System.out.println(id + " --- " + key1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}