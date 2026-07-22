package com.lhf.hotel.user.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.OssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "头像上传", description = "上传用户头像")
@RestController
@RequestMapping("/api/user/upload")
public class AvatarUploadController {

    private final OssUtil ossUtil;

    public AvatarUploadController(OssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    /** 上传用户头像 */
    @Operation(summary = "上传用户头像")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        String url = ossUtil.upload(file, "hotel/avatars");
        return Result.ok("上传成功", Map.of("url", url));
    }
}
