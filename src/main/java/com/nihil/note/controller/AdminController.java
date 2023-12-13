package com.nihil.note.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nihil.common.file.FileConst;
import com.nihil.common.file.FolderCreateParam;
import com.nihil.common.response.Result;
import com.nihil.note.entity.NoteArticle;
import com.nihil.note.entity.NoteColumn;
import com.nihil.note.mapper.NoteArticleMapper;
import com.nihil.note.mapper.NoteColumnMapper;
import com.nihil.note.pojo.ArticleVO;
import com.nihil.note.service.NoteArticleService;
import com.nihil.note.service.NoteColumnService;
import jakarta.annotation.Resource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@RestController
public class AdminController {

    @Resource
    NoteArticleService noteArticleService;

    @Resource
    NoteColumnService noteColumnService;

    @Resource
    NoteArticleMapper noteArticleMapper;

    @Resource
    NoteColumnMapper noteColumnMapper;

    /**
     * 通过 V1 版本中的 文件系统中的节点西信息 恢复数据。
     * 使用深度遍历，获取 原始数据库中的每一个节点，并和当前数据库进行对比，
     * 当出现重复时，如果是专栏就不再添加新的，如果是文章，为了避免冲突，会修改文章的名字为 原始名+(new)，
     * 而不会跳过或者覆盖。
     * @throws SQLException
     */
    @GetMapping("loadFromDB")
    Result<List<Long[]>> loadFromDB() throws SQLException {
        List<Long[]> res = new ArrayList<>();

        String oldDataBase = "test";
        String oldUser = "user";
        String oldPass = "user123456";
        long oldParentId = 2;
        long newParentId = 1;
        String ownId = "1";

        DataSource dataSource = DataSourceBuilder
                .create()
                .url("jdbc:mysql://localhost:3306/"+ oldDataBase +"?serverTimezone=UTC&useAffectedRows=true")
                .username(oldUser)
                .password(oldPass)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();

        class NodePair {
            long oldId;  // 旧数据库中的节点ID
            long newId;  // 新数据库中的节点ID
            boolean isNew;    // 是否 该对应表是新增的
            NodePair(long oldId, long newId, boolean isNew){
                this.oldId = oldId;
                this.newId = newId;
                this.isNew = isNew;
            }
        }
        class DataReader{
            Connection connection;
            Statement statement;
            ResultSet resultSet;
            DataReader(Connection connection, Statement statement, ResultSet resultSet){
                this.connection = connection;
                this.statement = statement;
                this.resultSet = resultSet;
            }
            DataReader(){}
        }

        DataReader startReader = new DataReader();
        startReader.connection = dataSource.getConnection();
        startReader.statement = startReader.connection.createStatement();


        Stack<NodePair> nodePairStack = new Stack<>();
        Stack<DataReader> readerStack = new Stack<>();
        nodePairStack.push(new NodePair(oldParentId, newParentId, false));
        String startSQL = "SELECT * FROM file_node WHERE parent_id = " + oldParentId + " ORDER BY file_order";
        startReader.resultSet = startReader.statement.executeQuery(startSQL);
        readerStack.push(startReader);


        while(true){
            DataReader dataReader = readerStack.peek();
            boolean isFinished = true;
            // 处理查询结果
            while (dataReader.resultSet.next()) {
                Long oldId = dataReader.resultSet.getLong("id");
                String oldNodeName = dataReader.resultSet.getString("name");
                // 查询新的数据库中是否存在该文档
                String type = dataReader.resultSet.getString("type");
                if(type.equals(FileConst.TYPE_Markdown)){
                    NoteArticle articleInDataBase = noteArticleMapper.getArticleByColumnIdAndName(nodePairStack.peek().newId, oldNodeName);
                    if(articleInDataBase != null){   // 如果找到了同名的文章就在后面加一个 (new)
                        oldNodeName = oldNodeName + "(new)";
                    }
                    ArticleVO articleVO = new ArticleVO();
                    articleVO.setAuthorId(dataReader.resultSet.getString("owner"));
                    articleVO.setTitle(oldNodeName);
                    articleVO.setColumnId(nodePairStack.peek().newId);
                    Long newArticleId = noteArticleService.addArticle(articleVO);
//                    System.out.println("完成文章：" + dataReader.resultSet.getLong("id") + " --- " + newArticleId );
                    res.add(new Long[]{dataReader.resultSet.getLong("id"), newArticleId});
                    continue;
                } else if (type.equals(FileConst.TYPE_FOLDER)) {
                    // 如果父级已经是新的 Column, 那么就不用从现有的数据库中查找了
                    NoteColumn columnInDataBase = nodePairStack.peek().isNew ? null :
                            noteColumnService.getColumnByParentIdAndName(nodePairStack.peek().newId, oldNodeName);
                    if(columnInDataBase!=null){
                        Long newId = columnInDataBase.getId();
                        nodePairStack.push(new NodePair(oldId, newId, false));
                    }else{
                        FolderCreateParam folderCreateParam = new FolderCreateParam();
                        folderCreateParam.setPid(nodePairStack.peek().newId);
                        folderCreateParam.setName(oldNodeName);
                        Long newId = noteColumnService.addColumn(folderCreateParam, ownId);
                        nodePairStack.push(new NodePair(oldId, newId, true));
                    }
                    // 查询 新的数据节点下的所有子元素
                    String sql = "SELECT * FROM file_node WHERE parent_id = " + oldId + " ORDER BY file_order";
                    DataReader reader = new DataReader();
                    reader.connection = dataSource.getConnection();
                    reader.statement = reader.connection.createStatement();
                    reader.resultSet = reader.statement.executeQuery(sql);
                    readerStack.push(reader);
                    isFinished = false;
                    break;
                }
            }
            if(isFinished){
                nodePairStack.pop();
                DataReader pop = readerStack.pop();
                pop.statement.close();
                pop.connection.close();
                if(nodePairStack.empty()){
                    break;
                }
            }
        }
        return Result.success(res);
    }


    @GetMapping("loadMeiliJson")
    Result<Boolean> loadMeiliJson() throws IOException {
        Map<Long, Long> map = new HashMap(300) {{
            put(184L, 876L);
            put(193L, 877L);
            put(356L, 878L);
            put(376L, 879L);
            put(399L, 883L);
            put(400L, 884L);
            put(397L, 885L);
            put(154L, 887L);
            put(155L, 888L);
            put(218L, 889L);
            put(240L, 890L);
            put(247L, 891L);
            put(269L, 892L);
            put(305L, 893L);
            put(203L, 895L);
            put(280L, 896L);
            put(395L, 897L);
            put(404L, 899L);
            put(129L, 900L);
            put(232L, 901L);
            put(228L, 902L);
            put(278L, 905L);
            put(179L, 906L);
            put(340L, 908L);
            put(342L, 909L);
            put(343L, 910L);
            put(345L, 912L);
            put(213L, 913L);
            put(273L, 914L);
            put(219L, 915L);
            put(165L, 916L);
            put(202L, 917L);
            put(286L, 918L);
            put(164L, 919L);
            put(180L, 920L);
            put(206L, 921L);
            put(158L, 922L);
            put(253L, 923L);
            put(176L, 924L);
            put(192L, 925L);
            put(234L, 926L);
            put(182L, 927L);
            put(225L, 928L);
            put(377L, 929L);
            put(198L, 931L);
            put(283L, 932L);
            put(217L, 933L);
            put(290L, 934L);
            put(223L, 935L);
            put(270L, 936L);
            put(237L, 937L);
            put(265L, 939L);
            put(170L, 940L);
            put(197L, 941L);
            put(307L, 942L);
            put(261L, 943L);
            put(291L, 944L);
            put(133L, 947L);
            put(173L, 948L);
            put(183L, 949L);
            put(187L, 950L);
            put(194L, 951L);
            put(207L, 952L);
            put(274L, 953L);
            put(169L, 954L);
            put(221L, 955L);
            put(140L, 957L);
            put(268L, 958L);
            put(208L, 959L);
            put(150L, 960L);
            put(271L, 961L);
            put(134L, 962L);
            put(191L, 963L);
            put(153L, 964L);
            put(308L, 965L);
            put(254L, 966L);
            put(142L, 967L);
            put(168L, 968L);
            put(233L, 969L);
            put(229L, 970L);
            put(162L, 971L);
            put(262L, 972L);
            put(163L, 975L);
            put(201L, 976L);
            put(212L, 977L);
            put(295L, 979L);
            put(159L, 980L);
            put(138L, 981L);
            put(299L, 982L);
            put(242L, 983L);
            put(226L, 984L);
            put(147L, 985L);
            put(296L, 986L);
            put(189L, 987L);
            put(199L, 988L);
            put(246L, 989L);
            put(227L, 990L);
            put(244L, 991L);
            put(245L, 992L);
            put(248L, 993L);
            put(252L, 994L);
            put(275L, 995L);
            put(379L, 996L);
            put(241L, 998L);
            put(258L, 999L);
            put(329L, 1001L);
            put(330L, 1002L);
            put(334L, 1003L);
            put(331L, 1004L);
            put(333L, 1005L);
            put(336L, 1007L);
            put(341L, 1009L);
            put(353L, 1010L);
            put(354L, 1011L);
            put(361L, 1012L);
            put(362L, 1013L);
            put(364L, 1014L);
            put(368L, 1015L);
            put(378L, 1016L);
            put(389L, 1019L);
            put(390L, 1020L);
            put(249L, 1022L);
            put(303L, 1023L);
            put(391L, 1024L);
            put(394L, 1025L);
            put(231L, 1028L);
            put(135L, 1029L);
            put(209L, 1030L);
            put(312L, 1031L);
            put(131L, 1032L);
            put(128L, 1033L);
            put(167L, 1034L);
            put(196L, 1035L);
            put(204L, 1036L);
            put(235L, 1037L);
            put(238L, 1038L);
            put(276L, 1039L);
            put(371L, 1041L);
            put(372L, 1042L);
            put(373L, 1043L);
            put(374L, 1044L);
            put(375L, 1045L);
            put(384L, 1046L);
            put(385L, 1047L);
            put(386L, 1048L);
            put(387L, 1049L);
            put(178L, 1051L);
            put(267L, 1052L);
            put(306L, 1053L);
            put(297L, 1054L);
            put(293L, 1055L);
            put(230L, 1056L);
            put(310L, 1057L);
            put(143L, 1058L);
            put(132L, 1059L);
            put(214L, 1060L);
            put(294L, 1061L);
            put(239L, 1062L);
            put(284L, 1063L);
            put(289L, 1064L);
            put(292L, 1065L);
            put(311L, 1066L);
            put(357L, 1068L);
            put(359L, 1069L);
            put(360L, 1070L);
            put(358L, 1071L);
            put(148L, 1072L);
            put(263L, 1073L);
            put(250L, 1074L);
            put(139L, 1075L);
            put(152L, 1076L);
            put(160L, 1077L);
            put(188L, 1078L);
            put(215L, 1079L);
            put(281L, 1080L);
            put(285L, 1082L);
            put(256L, 1084L);
            put(224L, 1085L);
            put(146L, 1086L);
            put(186L, 1087L);
            put(302L, 1088L);
            put(195L, 1089L);
            put(257L, 1090L);
            put(298L, 1091L);
            put(157L, 1093L);
            put(174L, 1094L);
            put(251L, 1095L);
            put(264L, 1096L);
            put(279L, 1097L);
            put(282L, 1098L);
            put(288L, 1099L);
            put(301L, 1100L);
            put(309L, 1101L);
            put(332L, 1102L);
            put(401L, 1103L);
            put(171L, 1105L);
            put(346L, 1106L);
            put(145L, 1107L);
            put(166L, 1108L);
            put(181L, 1109L);
            put(222L, 1110L);
            put(156L, 1111L);
            put(200L, 1112L);
            put(266L, 1113L);
            put(287L, 1114L);
            put(210L, 1116L);
            put(177L, 1117L);
            put(216L, 1118L);
            put(220L, 1119L);
            put(185L, 1121L);
            put(161L, 1123L);
            put(272L, 1124L);
            put(190L, 1125L);
            put(402L, 1126L);
            put(314L, 1128L);
            put(392L, 1129L);
            put(259L, 1132L);
            put(277L, 1133L);
            put(137L, 1135L);
            put(149L, 1136L);
            put(151L, 1137L);
            put(172L, 1138L);
            put(175L, 1139L);
            put(211L, 1140L);
            put(236L, 1141L);
            put(260L, 1142L);
            put(300L, 1143L);
            put(130L, 1145L);
            put(136L, 1146L);
            put(141L, 1147L);
            put(205L, 1148L);
            put(350L, 1149L);
            put(144L, 1150L);
            put(255L, 1151L);
            put(243L, 1152L);
            put(304L, 1153L);
            put(351L, 1154L);
            put(319L, 1156L);
        }};
        // 读取 JSON 文件
        String jsonFilePath = "D:/backup/OrderFile/markdown.json";
        String owner = "1";
        String jsonContent = readFile(jsonFilePath);

        // 将根元素转换为 JSON 对象

        // 解析 JSON
        JSONObject json = JSON.parseObject(jsonContent);
        if (json != null) {
            // 获取 "results" 数组
            JSONArray resultsArray = json.getJSONArray("results");
            if (resultsArray != null) {
                // 遍历 "results" 数组
                for (int i = 0; i < resultsArray.size(); i++) {
                    JSONObject resultObject = resultsArray.getJSONObject(i);
                    Long id = resultObject.getLong("id");

                    ArticleVO articleVO = new ArticleVO();
                    articleVO.setId(map.get(id));
                    articleVO.setDes(resultObject.getString("des"));
                    articleVO.setImgHref(resultObject.getString("imgHref"));
                    articleVO.setMarkdown(resultObject.getString("markdown"));
                    articleVO.setContent(resultObject.getString("content"));
                    articleVO.setPublished(resultObject.getBoolean("published"));
                    noteArticleService.updateArticle(articleVO, owner);

                    // 输出结果
                    System.out.println(resultObject.toJSONString());
                }
            }
        }
        return Result.success(true);
    }

    private static String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }
}