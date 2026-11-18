package com.campus_platform.controller;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.campus_platform.common.Result;

@RestController
@RequestMapping("/chaos")
@RequiredArgsConstructor
public class ChaosController {

    private final ApplicationContext context;

    /**
     * 答辩专用：模拟系统出现死锁或严重级内存溢出
     * 强行将 K8s Liveness 状态置为 BROKEN
     */
    @GetMapping("/kill-liveness")
    public Result<String> killLiveness() {
        // 发布事件，主动破坏存活状态，此时 /actuator/health 将返回 503
        AvailabilityChangeEvent.publish(context, LivenessState.BROKEN);
        return Result.success("系统存活状态已破坏，等待 K8s 探针介入并处决...");
    }
}