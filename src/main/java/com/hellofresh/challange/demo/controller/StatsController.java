package com.hellofresh.challange.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping()
public class StatsController {


    Predicate<Long> last60Second = item -> item >= System.currentTimeMillis() - 60000;
    ConcurrentHashMap<Long, DoubleSummaryStatistics> xMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Long, IntSummaryStatistics> yMap = new ConcurrentHashMap<>();

    @PostMapping("/event")
    public ResponseEntity event(@RequestBody String events) {

        if(events != null && events.length()> 0 &&  events.split("\\s+").length==0){
            return new ResponseEntity<>("Event list could not be empty", HttpStatus.BAD_REQUEST);
        }

        Runnable x = new XRunnable(events);
        new Thread(x).start();

        Runnable y = new YRunnable(events);
        new Thread(y).start();

        return new ResponseEntity<>("Success", HttpStatus.ACCEPTED);
    }

    @GetMapping("/stats")
    public ResponseEntity stats() {

        double sumX = 0;
        long sumY = 0;
        long countX = 0;
        long countY = 0;

        List<DoubleSummaryStatistics>  doubleSummaryStatistics= xMap.entrySet()
                .stream()
                .filter(e -> last60Second.test(e.getKey()))
                .map(item -> item.getValue()).collect(Collectors.toList());


        List<IntSummaryStatistics>  intSummaryStatistics= yMap.entrySet()
                .stream()
                .filter(e -> last60Second.test(e.getKey()))
                .map(item -> item.getValue()).collect(Collectors.toList());

        for (DoubleSummaryStatistics xStat:doubleSummaryStatistics) {
            sumX = sumX + xStat.getSum();
            countX = countX + xStat.getCount();
        }
        for (IntSummaryStatistics yStat:intSummaryStatistics) {
            sumY = sumY + yStat.getSum();
            countY = countY + yStat.getCount();
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (countX == 0) {
            return new ResponseEntity<>("Failed", HttpStatus.NO_CONTENT);
        }
            stringBuilder.append(countX).append(",");
            stringBuilder.append(sumX).append(",");
            stringBuilder.append(sumX / countX).append(",");
            stringBuilder.append(sumY).append(",");
            stringBuilder.append(sumY/ countY);


        return ResponseEntity.ok(stringBuilder.toString());
    }

    public class YRunnable implements Runnable {

        String events =  null;
        public YRunnable(String events) {
            this.events = events;
        }

        public void run() {
            Map<Long, IntSummaryStatistics> yInner = Arrays.stream(events.split("\\s+"))
                    .map(elem -> elem.split(","))
                    .collect(Collectors.groupingBy(e -> Long.parseLong(e[0]), Collectors.summarizingInt(e -> Integer.parseInt(e[2]))));
            yInner.forEach((k, v) -> {
                if (yMap.get(k) != null)
                    v.combine(yMap.get(k));
                else
                    yMap.put(k, v);
            });
        }
    }
    public class XRunnable implements Runnable {

        String events =  null;
        public XRunnable(String events) {
            this.events = events;
        }

        public void run() {
            Map<Long, IntSummaryStatistics> yInner = Arrays.stream(events.split("\\s+"))
                    .map(elem -> elem.split(","))
                    .collect(Collectors.groupingBy(e -> Long.parseLong(e[0]), Collectors.summarizingInt(e -> Integer.parseInt(e[2]))));
            yInner.forEach((k, v) -> {
                if (yMap.get(k) != null)
                    v.combine(yMap.get(k));
                else
                    yMap.put(k, v);
            });
        }
    }

}
