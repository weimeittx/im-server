package com.baidu.ueditor.upload;

import cn.dunn.service.FileService;
import cn.dunn.util.WebUtil;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.FileType;
import com.baidu.ueditor.define.State;
import com.mongodb.gridfs.GridFSFile;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinaryUploader {

  private static FileService fileService;

  private static FileService getFileService(HttpServletRequest request) {
    if (fileService == null) {
      WebApplicationContext application = (WebApplicationContext) request.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
      fileService = application.getBean(FileService.class);
    }
    return fileService;
  }

  public static final State save(HttpServletRequest request,
                                 Map<String, Object> conf) {
    FileItemStream fileStream = null;
    boolean isAjaxUpload = request.getHeader("X_Requested_With") != null;

    if (!ServletFileUpload.isMultipartContent(request)) {
      return new BaseState(false, AppInfo.NOT_MULTIPART_CONTENT);
    }

    ServletFileUpload upload = new ServletFileUpload(
      new DiskFileItemFactory());

    if (isAjaxUpload) {
      upload.setHeaderEncoding("UTF-8");
    }

    try {
      FileItemIterator iterator = upload.getItemIterator(request);

      while (iterator.hasNext()) {
        fileStream = iterator.next();

        if (!fileStream.isFormField())
          break;
        fileStream = null;
      }

      if (fileStream == null) {
        return new BaseState(false, AppInfo.NOTFOUND_UPLOAD_DATA);
      }

      String originFileName = fileStream.getName();//获取原始文件名称
      String suffix = FileType.getSuffixByFilename(originFileName);//获取后缀

      originFileName = originFileName.substring(0,
        originFileName.length() - suffix.length());


      if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
        return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
      }


      InputStream is = fileStream.openStream();
      FileService fileService = getFileService(request);
      Map<String, Object> metadata = new HashMap<>();
      try {
        metadata.put("user", WebUtil.loginUser(request).getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
      GridFSFile file = null;
      String md5 = request.getParameter("_md5_");
      String paste = request.getParameter("paste");
      if (paste != null && paste.length() > 0) {
        metadata.put("paste", paste);
      }
      if (StringUtils.hasLength(md5)) {
        file = fileService.getFileByMD5(md5);
      }
      if (file == null) {
        file = fileService.saveFile(is, originFileName, suffix, metadata);
      }
      State storageState = new BaseState(true);
      storageState.putInfo("size", file.getLength());
      storageState.putInfo("title", file.getId().toString());
      is.close();

      //TODO
      if (storageState.isSuccess()) {
        storageState.putInfo("url", "/file/view/" + file.getId() + suffix);
        storageState.putInfo("type", suffix);
        storageState.putInfo("original", originFileName + suffix);
      }

      return storageState;
    } catch (FileUploadException e) {
      return new BaseState(false, AppInfo.PARSE_REQUEST_ERROR);
    } catch (IOException e) {
    }
    return new BaseState(false, AppInfo.IO_ERROR);
  }

  private static boolean validType(String type, String[] allowTypes) {
    List<String> list = Arrays.asList(allowTypes);

    return list.contains(type);
  }
}
