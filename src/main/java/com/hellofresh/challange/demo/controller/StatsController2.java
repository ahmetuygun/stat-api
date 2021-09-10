package com.hellofresh.challange.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1")
public class StatsController2 {


    Predicate<Long> last60Second = item -> item > System.currentTimeMillis() - 60000;
    ConcurrentHashMap<Long, Transanction> xyMap = new ConcurrentHashMap<>();

    @PostMapping("/event")
    public ResponseEntity event(@RequestBody String events) {

        for (String s : events.split("\\s+")) {
            String[] split = s.split(",");
            Long timestamp = Long.parseLong(split[0]);
            Double xVal = Double.parseDouble(split[1]);
            Integer yVal = Integer.parseInt(split[2]);
            if (xyMap.get(timestamp) != null) {
                xyMap.get(timestamp).getX().accept(xVal);
                xyMap.get(timestamp).getY().accept(yVal);
            } else {
                DoubleSummaryStatistics doubleSummaryStatistics = new DoubleSummaryStatistics();
                IntSummaryStatistics intSummaryStatistics = new IntSummaryStatistics();
                intSummaryStatistics.accept(yVal);
                doubleSummaryStatistics.accept(xVal);
                xyMap.put(timestamp, new Transanction(intSummaryStatistics, doubleSummaryStatistics));
            }
        }

        return new ResponseEntity<>(
                "Success",
                HttpStatus.ACCEPTED);
    }

    @GetMapping("/stats")
    public ResponseEntity stats() {

        double sumX = 0;
        long sumY = 0;
        long countX = 0;
        long countY = 0;

        List<Transanction> transanctions = xyMap.entrySet()
                .stream()
                .filter(e -> last60Second.test(e.getKey()))
                .map(item -> item.getValue()).collect(Collectors.toList());


        for (Transanction trans : transanctions) {
            sumX = sumX + trans.getX().getSum();
            countX = countX + trans.getX().getCount();
            sumY = sumY + trans.getY().getSum();
            countY = countY + trans.getY().getCount();
        }

        if (countX == 0) {
            return new ResponseEntity<>("Failed", HttpStatus.NO_CONTENT);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(countX).append(",");
        stringBuilder.append(sumX).append(",");
        stringBuilder.append(sumX / countX).append(",");
        stringBuilder.append(sumY).append(",");
        stringBuilder.append(sumY / countY);

        return ResponseEntity.ok(stringBuilder.toString());
    }


}
