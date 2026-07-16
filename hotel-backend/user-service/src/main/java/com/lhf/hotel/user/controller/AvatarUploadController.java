package com.lhf.hotel.user.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.OssUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user/upload")
public class AvatarUploadController {

    private final OssUtil ossUtil;

    public AvatarUploadController(OssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = ossUtil.upload(file, "hotel/avatars");
        return Result.ok("上传成功", Map.of("url", url));
    }
}
