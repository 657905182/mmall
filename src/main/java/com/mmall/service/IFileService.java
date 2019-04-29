package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 〈文件service接口〉
 *
 * @author liu
 * @create 2019/3/1
 * @since 1.0.0
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
