package com.zjx.controller;

import com.zjx.model.UploadFile;
import com.zjx.service.FileService;
import com.zjx.service.impl.StatusMap;
import com.zjx.util.ResponseMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 资源管理
 */
@Controller
@RequestMapping("/filesInfo")
public class FilesController {

    @Resource
    private FileService fileService;
    @Resource
    private StatusMap statusMap;

    private static final Logger logger = LoggerFactory.getLogger(FilesController.class);

    /**
     * @Description:资源管理
     * @Author: 张金鑫
     * @Date: 2020/4/18
     */
    @RequestMapping("/filesInfo")
    public String mindmap2(){
        return "view";
    }


    /**
     * 获得用户资源文件
     * @param request
     */
    @RequestMapping(value = "getAllFile" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseMsg getAllFile(HttpServletRequest request) {
        ResponseMsg responseMsg = new ResponseMsg();

        HttpSession session = request.getSession();
        String userId = session.getAttribute("userId").toString();
        List<UploadFile> fileList = fileService.getAllFile(Integer.parseInt(userId));

        if(fileList.size() > 0){
            responseMsg.setCode(200);
            responseMsg.setData(fileList);
            return responseMsg;
        } else {
            responseMsg.setCode(400);
            responseMsg.setMsg("未查询到资源");
            return responseMsg;
        }
    }

    /**
     * 上传文件
     * @param files
     */
    @RequestMapping(value = "uploadallfile" ,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String uploadallfile(@RequestParam("file") MultipartFile[] files,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String uploadtima = df.format(new Date());
        HttpSession session = request.getSession();
        String userId = String.valueOf(session.getAttribute("userId"));


        Calendar cal = Calendar.getInstance();
        String month = (cal.get(Calendar.MONTH)+1)+"";
        String year = (cal.get(Calendar.YEAR))+"";

        String pathUrl = "D:\\mindMapFile";
        File newFile = new File(pathUrl+File.separator+year+File.separator+month);

        newFile.mkdirs();//判断年 月 日文件夹是否创建，没有则新建

        for (int i = 0;i < files.length; i++){ //遍历文件

            String fileName = files[i].getOriginalFilename(); //获取文件全名称
            String preFix1 = fileName.substring(0,fileName.indexOf("."));//获得文件名称
            String preFix2 = fileName.substring(fileName.lastIndexOf(".")+1);//获得文件类型
            String tubiao = "";
            InputStream in = null;

            try {
                in = files[i].getInputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                String path=pathUrl+File.separator+year+File.separator+month+File.separator+fileName;

                File file = new File(path);
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();//创建父级文件路径
                    file.createNewFile();//创建文件
                }
                FileOutputStream out = new FileOutputStream(path);

                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();
                in.close();

                if ((preFix2.equals("bmp")) || (preFix2.equals("drw"))
                        || (preFix2.equals("dxf"))
                        || (preFix2.equals("eps"))
                        || (preFix2.equals("gif"))
                        || (preFix2.equals("jpg"))
                        || (preFix2.equals("png"))
                        || (preFix2.equals("pcd"))
                        || (preFix2.equals("pcx"))) {
                    preFix2 = "picture";
                    tubiao = "/assets/avatars/tupian.jpg";
                } else if ((preFix2.equals("avi"))
                        || (preFix2.equals("mpeg"))
                        || (preFix2.equals("mpg"))
                        || (preFix2.equals("dat"))
                        || (preFix2.equals("ra")) || (preFix2.equals("rm"))
                        || (preFix2.equals("wmv"))
                        || (preFix2.equals("mp4"))
                        || (preFix2.equals("swf"))
                        || (preFix2.equals("f4v"))) {
                    preFix2 = "video";
                    tubiao = "/assets/avatars/shipin.jpg";
                } else if ((preFix2.equals("cd"))
                        || (preFix2.equals("ogg"))
                        || (preFix2.equals("mp3"))
                        || (preFix2.equals("asf"))
                        || (preFix2.equals("wma"))
                        || (preFix2.equals("wav"))
                        || (preFix2.equals("rm"))
                        || (preFix2.equals("midi"))
                        || (preFix2.equals("ape"))) {
                    preFix2 = "doc";
                    tubiao = "/assets/avatars/word.jpg";
                } else {
                    preFix2 = "other";
                    tubiao = "/assets/avatars/tuzhi.jpg";
                }

                //path = ""+path;
                //将文件路径存入数据库
                UploadFile uploadFile = new UploadFile();
                uploadFile.setUserId(Integer.parseInt(userId));
                uploadFile.setFileName(preFix1);
                uploadFile.setFilePath(path);
                uploadFile.setFileType(preFix2);
                uploadFile.setTubiao(tubiao);
                fileService.insertNodeFile(uploadFile);
            } catch (Exception e) {
                logger.info("---------文件上传失败————————");
                e.printStackTrace();
                return statusMap.a("2");
            }
        }

        return statusMap.a("1");
    }
}
