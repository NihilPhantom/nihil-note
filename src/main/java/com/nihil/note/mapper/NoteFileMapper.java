package com.nihil.note.mapper;

import com.nihil.note.entity.NoteColumnFile;
import org.apache.ibatis.annotations.*;

@Mapper
public interface NoteFileMapper {
    @Insert("INSERT INTO note_article_file (article_id, file_id) VALUES (#{articleId}, #{fileId})")
    Long addNoteFile(Long articleId, Long fileId);

    @Select("SELECT article_id FROM note_article_file WHERE  file_id = #{fileId}")
    Long getArticleIdByFileId(Long fileId);

    @Select("SELECT file_id FROM note_article_file WHERE article_id = #{articleId}")
    Long getFileIdByArticleId(Long articleId);


    @Insert("INSERT INTO note_column_file (column_id, file_id) VALUES (#{columnId}, #{fileId})")
    Integer addColumnFile(Long columnId, Long fileId);


    @Select("SELECT file_id FROM note_column_file WHERE column_id = #{columnId}")
    Long getFileIdByColumnId(Long columnId);

    @Select("SELECT column_id FROM note_column_file WHERE file_id = #{fileId}")
    Long getColumnIdByFileId(Long fileId);

    @Delete("DELETE FROM note_column_file WHERE column_id = #{columnId}")
    Integer delColumnFile(Long columnId);

    @Delete("DELETE FROM note_column_file WHERE file_id = #{fileId}")
    Integer delColumnFileByFileId(Long fileId);

    @Delete("DELETE FROM note_article_file WHERE article_id = #{articleId}")
    void delArticleFile(Long articleId);
}
