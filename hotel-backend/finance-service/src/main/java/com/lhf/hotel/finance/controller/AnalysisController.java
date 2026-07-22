package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.model.vo.OccupancyByTypeVO;
import com.lhf.hotel.finance.model.vo.OccupancyTrendVO;
import com.lhf.hotel.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/analysis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "数据分析", description = "入住率趋势、各房型入住率")
public class AnalysisController {

    private final FinanceService financeService;

    /** 入住率趋势分析 */
    @Operation(summary = "入住率趋势分析")
    @GetMapping("/occupancy")
    public Result<OccupancyTrendVO> occupancy(@Parameter(description = "开始日期") @RequestParam String startDate,
                                              @Parameter(description = "结束日期") @RequestParam String endDate,
                                              @Parameter(description = "统计粒度（day/week/month，默认day）") @RequestParam(defaultValue = "day") String type) {
        return Result.ok(financeService.occupancyTrend(startDate, endDate, type));
    }

    /** 各房型入住率统计 */
    @Operation(summary = "各房型入住率统计")
    @GetMapping("/occupancy-by-type")
    public Result<List<OccupancyByTypeVO>> occupancyByType(@Parameter(description = "开始日期") @RequestParam String startDate,
                                                           @Parameter(description = "结束日期") @RequestParam String endDate) {
        log.info("AnalysisController.occupancyByType:{}",financeService.occupancyByType(startDate, endDate));
        return Result.ok(financeService.occupancyByType(startDate, endDate));
    }
}
