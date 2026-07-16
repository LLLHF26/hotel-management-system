package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.model.vo.OccupancyByTypeVO;
import com.lhf.hotel.finance.model.vo.OccupancyTrendVO;
import com.lhf.hotel.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final FinanceService financeService;

    @GetMapping("/occupancy")
    public Result<OccupancyTrendVO> occupancy(@RequestParam String startDate,
                                              @RequestParam String endDate,
                                              @RequestParam(defaultValue = "day") String type) {
        return Result.ok(financeService.occupancyTrend(startDate, endDate, type));
    }

    @GetMapping("/occupancy-by-type")
    public Result<List<OccupancyByTypeVO>> occupancyByType(@RequestParam String startDate,
                                                           @RequestParam String endDate) {
        log.info("AnalysisController.occupancyByType:{}",financeService.occupancyByType(startDate, endDate));
        return Result.ok(financeService.occupancyByType(startDate, endDate));
    }
}
