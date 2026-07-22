package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.OssUtil;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "房间图片上传", description = "上传房型封面图、房间图片")
@RestController
@RequestMapping("/api/room/upload")
public class RoomUploadController {

    private final OssUtil ossUtil;

    public RoomUploadController(OssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    /** 上传房型封面图 */
    @Operation(summary = "上传房型封面图")
    @PostMapping("/cover")
    public Result<Map<String, String>> uploadCover(@RequestParam("file") @Parameter(description = "图片文件") MultipartFile file) {
        String url = ossUtil.upload(file, "hotel/room-images");
        return Result.ok("上传成功", Map.of("url", url));
    }

    /** 上传房间图片 */
    @Operation(summary = "上传房间图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") @Parameter(description = "图片文件") MultipartFile file) {
        String url = ossUtil.upload(file, "hotel/room-images");
        return Result.ok("上传成功", Map.of("url", url));
    }
}
