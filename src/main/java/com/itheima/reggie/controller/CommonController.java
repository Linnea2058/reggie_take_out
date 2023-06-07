package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 上传和下载文件
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 上传文件
     * @param file
     * @return
     */
    @PostMapping("/upload")
    private R<String> upload(MultipartFile file){
        log.info("上传图片{}",file.toString());

        //获得上传文件的名称后缀
        String originalFilename = file.getOriginalFilename();//获得文件名称
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));//获得后缀

        //使用uuid重新生成文件名
        String fileName = UUID.randomUUID().toString()+suffix;

        //上传的文件放在临时区域，需要存储到新的位置，否则请求结束后删除
        //创建一个目录对象
        File dir = new File(basePath);
        dir.mkdirs();

        // 转存到新的位置
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);
    }

    @GetMapping("/download")
    private void download(HttpServletResponse response, String name){
        log.info("下载图片：{}",name);

        try {
            //输入流，用于读取文件的内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //输出流，将文件的内容写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            //类型：图片
            response.setContentType("image/jpeg");

            byte[] bytes = new byte[1024];

            int len = 0 ;
            while((len =fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
