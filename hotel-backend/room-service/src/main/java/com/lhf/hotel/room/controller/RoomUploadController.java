package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.util.OssUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/room/upload")
public class RoomUploadController {

    private final OssUtil ossUtil;

    public RoomUploadController(OssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @PostMapping("/cover")
    public Result<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        String url = ossUtil.upload(file, "hotel/room-images");
        return Result.ok("上传成功", Map.of("url", url));
    }

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = ossUtil.upload(file, "hotel/room-images");
        return Result.ok("上传成功", Map.of("url", url));
    }
}
