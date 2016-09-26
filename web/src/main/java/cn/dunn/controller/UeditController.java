package cn.dunn.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2016/9/27.
 */
@RequestMapping("uedit")
@Controller
public class UeditController {
  @RequestMapping("execute")
  @ResponseBody
  public Object execute(@RequestParam(value = "upfile", required = false) MultipartFile upfile, String action) {
    System.err.println("action is ==>    " + action);
    if ("config".equals(action)) {
      return "{\"videoMaxSize\":102400000,\"videoActionName\":\"uploadvideo\",\"fileActionName\":\"uploadfile\",\"fileManagerListPath\":\"/ueditor/jsp/upload/file/\",\"imageCompressBorder\":1600,\"imageManagerAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\"],\"imageManagerListPath\":\"/ueditor/jsp/upload/image/\",\"fileMaxSize\":51200000,\"fileManagerAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\",\".flv\",\".swf\",\".mkv\",\".avi\",\".rm\",\".rmvb\",\".mpeg\",\".mpg\",\".ogg\",\".ogv\",\".mov\",\".wmv\",\".mp4\",\".webm\",\".mp3\",\".wav\",\".mid\",\".rar\",\".zip\",\".tar\",\".gz\",\".7z\",\".bz2\",\".cab\",\".iso\",\".doc\",\".docx\",\".xls\",\".xlsx\",\".ppt\",\".pptx\",\".pdf\",\".txt\",\".md\",\".xml\"],\"fileManagerActionName\":\"listfile\",\"snapscreenInsertAlign\":\"none\",\"scrawlActionName\":\"uploadscrawl\",\"videoFieldName\":\"upfile\",\"imageCompressEnable\":true,\"videoUrlPrefix\":\"\",\"fileManagerUrlPrefix\":\"\",\"catcherAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\"],\"imageManagerActionName\":\"listimage\",\"snapscreenPathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"scrawlPathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"scrawlMaxSize\":2048000,\"imageInsertAlign\":\"none\",\"catcherPathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"catcherMaxSize\":2048000,\"snapscreenUrlPrefix\":\"\",\"imagePathFormat\":\"/ueditor/jsp/upload/image/{yyyy}{mm}{dd}/{time}{rand:6}\",\"imageManagerUrlPrefix\":\"\",\"scrawlUrlPrefix\":\"\",\"scrawlFieldName\":\"upfile\",\"imageMaxSize\":2048000,\"imageAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\"],\"snapscreenActionName\":\"uploadimage\",\"catcherActionName\":\"catchimage\",\"fileFieldName\":\"upfile\",\"fileUrlPrefix\":\"\",\"imageManagerInsertAlign\":\"none\",\"catcherLocalDomain\":[\"127.0.0.1\",\"localhost\",\"img.baidu.com\"],\"filePathFormat\":\"/ueditor/jsp/upload/file/{yyyy}{mm}{dd}/{time}{rand:6}\",\"videoPathFormat\":\"/ueditor/jsp/upload/video/{yyyy}{mm}{dd}/{time}{rand:6}\",\"fileManagerListSize\":20,\"imageActionName\":\"uploadimage\",\"imageFieldName\":\"upfile\",\"imageUrlPrefix\":\"\",\"scrawlInsertAlign\":\"none\",\"fileAllowFiles\":[\".png\",\".jpg\",\".jpeg\",\".gif\",\".bmp\",\".flv\",\".swf\",\".mkv\",\".avi\",\".rm\",\".rmvb\",\".mpeg\",\".mpg\",\".ogg\",\".ogv\",\".mov\",\".wmv\",\".mp4\",\".webm\",\".mp3\",\".wav\",\".mid\",\".rar\",\".zip\",\".tar\",\".gz\",\".7z\",\".bz2\",\".cab\",\".iso\",\".doc\",\".docx\",\".xls\",\".xlsx\",\".ppt\",\".pptx\",\".pdf\",\".txt\",\".md\",\".xml\"],\"catcherUrlPrefix\":\"\",\"imageManagerListSize\":20,\"catcherFieldName\":\"source\",\"videoAllowFiles\":[\".flv\",\".swf\",\".mkv\",\".avi\",\".rm\",\".rmvb\",\".mpeg\",\".mpg\",\".ogg\",\".ogv\",\".mov\",\".wmv\",\".mp4\",\".webm\",\".mp3\",\".wav\",\".mid\"]}";
    } else if ("uploadimage".equals(action)) {
      System.err.println(upfile);
      return "{\"state\": \"SUCCESS\",\"original\": \"blob.png\",\"size\": \"1690\",\"title\": \"1474906199949021798.png\",\"type\": \".png\",\"url\": \"/file/view/57e5551472947422655e9103\"}";
    }
    return null;
  }
}
