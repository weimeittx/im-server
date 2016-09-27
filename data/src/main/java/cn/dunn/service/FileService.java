package cn.dunn.service;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件服务
 */
public interface FileService {
    GridFSDBFile getOneFile(String id);

    GridFSFile saveFile(byte[] bytes, String fileName, String type, Map<String, Object> metadata);

    GridFSFile saveFile(InputStream inputStream, String fileName, String type, Map<String, Object> metadata);

    void deleteFile(String id);


    GridFSFile getFileByMD5(String md5);

    List<GridFSDBFile> getFilesByUSerId(String userId);
}
