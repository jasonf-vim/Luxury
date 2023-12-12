package com.jasonf.file.controller;

import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.file.utils.FastDFSClient;
import com.jasonf.file.utils.FastDFSFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("file")
public class FileController {
    @PostMapping("upload")
    public Result<String> upload(MultipartFile file) {
        String url;
        try {
            String filename = file.getOriginalFilename();
            byte[] content = file.getBytes();
            String ext = null;
            if (filename != null) {
                ext = filename.substring(filename.lastIndexOf(".") + 1);
            }
            FastDFSFile fastDFSFile = new FastDFSFile(filename, content, ext);
            String[] upload = FastDFSClient.upload(fastDFSFile);
            url = FastDFSClient.getTrackerUrl() + upload[0] + "/" + upload[1];
        } catch (IOException ex) {
            return new Result<>(true, StatusCode.ERROR, "上传失败");
        }
        return new Result<>(true, StatusCode.OK, "上传成功", url);
    }
}
