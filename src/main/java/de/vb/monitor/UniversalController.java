package de.vb.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Controller
public class UniversalController {

    private static final Logger log = LoggerFactory.getLogger(UniversalController.class);
    Queue<Long> averages = new LinkedList<>();
    Queue<Long> mins = new LinkedList<>();
    Queue<Long> maxs = new LinkedList<>();
    @RequestMapping(value = "/hello")
    public String index(Model model) {
        // return "index";
        System.out.println("hello");
        return "index";
    }
    @RequestMapping(value = "/statistics")
    public String statistics(Model model) {
        String fileName = "response_data.csv";
        long sum = 0;
        int count = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        String line;
        Queue<String> queue = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                queue.add(line);
                if (queue.size() > 20) {
                    queue.poll();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + fileName);
            e.printStackTrace();
        }

        for (String data : queue) {
            String[] values = data.split(";");
            if (values.length >= 5) {
                long value = Long.parseLong(values[4]);
                sum += value;
                count++;
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }

        long average = sum / count;
        averages.add(average);
        mins.add(min);
        maxs.add(max);
        if (averages.size() > 20) {
            averages.poll();
        }
        log.info("The average of the last 20 values in the fifth column is: " + average);
        log.info("The minimum response time is: " + min);
        log.info("The maximum response time is: " + max);
        log.info("The average of the values in the fifth column is: " + average);
        log.info("The last 20 averages are: " + averages);
        log.info("statistics");
        model.addAttribute("averageResponseTime", average);
        model.addAttribute("minResponseTime", min);
        model.addAttribute("maxResponseTime", max);
        model.addAttribute("averages", averages);
        model.addAttribute("mins", mins);
        model.addAttribute("maxs", maxs);

        return "statistics";
    }
}