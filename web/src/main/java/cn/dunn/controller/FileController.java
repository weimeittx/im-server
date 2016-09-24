package cn.dunn.controller;

import cn.dunn.service.FileService;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/file")
public class FileController {
  @Resource
  private FileService fileService;

  @RequestMapping("/view/{id}")
  @ResponseBody
  public void getImg(@PathVariable String id, HttpServletResponse response) throws IOException {
    GridFSDBFile file = fileService.getOneFile(id);
    if (file != null) {
      ServletOutputStream outputStream = response.getOutputStream();
      InputStream inputStream = file.getInputStream();
      IOUtils.copy(inputStream, outputStream);
    }
  }
}
