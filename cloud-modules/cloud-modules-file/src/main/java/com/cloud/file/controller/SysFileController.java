package com.cloud.file.controller;

import com.cloud.common.core.domain.R;
import com.cloud.common.core.utils.file.FileUtils;
import com.cloud.file.service.ISysFileService;
import com.cloud.system.api.domain.SysFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Request Handler
 * 
 * @author cloud
 */
@RestController
public class SysFileController
{
    private static final Logger log = LoggerFactory.getLogger(SysFileController.class);

    @Autowired
    private ISysFileService sysFileService;

    /**
     * File upload request
     */
    @PostMapping("upload")
    public R<SysFile> upload(@RequestPart("file") MultipartFile file) {
        try {
            // Upload and return access address
            String url = sysFileService.uploadFile(file);
            SysFile sysFile = new SysFile();
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            return R.ok(sysFile);
        }
        catch (Exception e) {
            log.error("Failed to upload file", e);
            return R.fail(e.getMessage());
        }
    }
}
