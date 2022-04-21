package com.cao.reggie.controller;

import com.cao.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.pic-path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //获取文件后缀名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        File path = new File(basePath);
        //如果目录不存在则创建
        if (!path.exists()) {
            path.mkdirs();
        }

        try {
            //存储文件
            file.transferTo(new File(basePath + fileName));
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("上传失败");
        }

        //返回随机生成的文件名
        return R.success(fileName);
    }

    /**
     * 文件下载
     *
     * @param name     随机生成的文件名
     * @param response
     */
    @RequestMapping("/download")
    public void download(String name, HttpServletResponse response) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            //输入流读取图片
            inputStream = new FileInputStream(basePath + name);
            //输出流写回浏览器
            outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            //数据流传输
            int len = 0;
            byte[] bytes = new byte[1024];

            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
