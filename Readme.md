# NIHIL-NOTE

Nihil 系列下的笔记管理系统

## 项目启动方法
使用IDEA，打开项目即可使用


## 使用文档 

### 对象实体说明

【专栏 Colomn】
> 专栏是指存放 笔记的的文件夹，专栏同样可以存储专栏。 

【文章 Article】
> 笔记本身

### 额外功能

1、 文章排序：需要设置 `${note.file-node-enable}` 为 `true`

2、 微服务支持（自动配置），当检测到 8848 服务正常，会主动进行服务注册。


## 默认规定说明
1、每一个用户都会有一个自己的根专栏， 专栏的名称通过 `${note.root-name}` 设置用户根专栏名称。
<font color = "red"> ！！注意在项目使用中，请不要修改此值，如果需要修改，先到数据库中修改根专栏名称，然后再修此值 </font>

2、存储类型   

public static final String TYPE_FOLDER = "FOLDER";
public static final String TYPE_Markdown = "markdown";

3、专栏Id

当使用了外置的文档系统时，为了避免额外的查询，专栏Id 和 文件Id 都会进行转化为 文档系统的ID