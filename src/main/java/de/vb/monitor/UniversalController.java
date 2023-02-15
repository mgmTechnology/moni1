package de.vb.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

@Controller
public class UniversalController {

    public static final String RESPONSE_DATA_CSV = "response_data.csv";
    private static final Logger log = LoggerFactory.getLogger(UniversalController.class);
    Queue<Long> averagesQueue = new LinkedList<>();
    Queue<Long> minsQueue = new LinkedList<>();
    Queue<Long> maxsQueue = new LinkedList<>();
    @RequestMapping(value = "/hello")
    public String index(Model model) {
        // return "index";
        System.out.println("hello");
        return "index";
    }

/*    @GetMapping("/endpoints")
    public String showForm(Model model) {
        EndpointForm epf = new EndpointForm();
        epf.setEndpoint1(true);
        model.addAttribute("endpointForm", epf);
        return "endpoints";
    }*/

    @RequestMapping(value = "/statsrealtime")
    public String statistics(Model model) {
        if (!Files.exists(Paths.get(RESPONSE_DATA_CSV))) {
            model.addAttribute("message", "No data found: " +RESPONSE_DATA_CSV);
            return "err_io";
        }
        long sum = 0;
        int count = 0;
        long minOfLastLogEntries = Long.MAX_VALUE;
        long maxOfLastLogEntries = Long.MIN_VALUE;
        String line;
        Queue<String> queueOfLastLogEntries = new LinkedList<>();

        int queueSize = Integer.parseInt(WSOnlineMonitor.appProps.getProperty("queue.size").trim());
        try (BufferedReader br = new BufferedReader(new FileReader(RESPONSE_DATA_CSV))) {
            while ((line = br.readLine()) != null) {
                queueOfLastLogEntries.add(line);
                if (queueOfLastLogEntries.size() > queueSize) {
                    queueOfLastLogEntries.poll();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + RESPONSE_DATA_CSV);
            e.printStackTrace();
        }

        for (String data : queueOfLastLogEntries) {
            String[] values = data.split(";");
            if (values.length >= 5) {
                long value = Long.parseLong(values[4]);
                sum += value;
                count++;
                minOfLastLogEntries = Math.min(minOfLastLogEntries, value);
                maxOfLastLogEntries = Math.max(maxOfLastLogEntries, value);
            }
        }

        long average = sum / count;
        averagesQueue.add(average);
        minsQueue.add(minOfLastLogEntries);
        maxsQueue.add(maxOfLastLogEntries);
        // upate queues to have it correct length

        if (averagesQueue.size() > queueSize) {
            averagesQueue.poll();
        }
        if (minsQueue.size() > queueSize) {
            minsQueue.poll();
        }
        if (maxsQueue.size() > queueSize) {
            maxsQueue.poll();
        }
        log.info("statistics: The average of the last 20 values in the fifth column is: " + average);
        log.info("statistics: The minimum response time is: " + minOfLastLogEntries);
        log.info("statistics: The maximum response time is: " + maxOfLastLogEntries);
        log.info("statistics: The average of the values in the fifth column is: " + average);

        log.info("statistics: The last 20 maximums are: " + maxsQueue);
        log.info("statistics: The last 20 averages are: " + minsQueue);
        log.info("statistics: The last 20 minimums are: " + averagesQueue);
        log.info(String.format("Min/Max/AVG: [%s .. %s] -> %s", minOfLastLogEntries, maxOfLastLogEntries, average));
        model.addAttribute("averageResponseTime", average);
        model.addAttribute("minResponseTime", minOfLastLogEntries);
        model.addAttribute("maxResponseTime", maxOfLastLogEntries);
        model.addAttribute("averages", averagesQueue);
        model.addAttribute("mins", minsQueue);
        model.addAttribute("maxs", maxsQueue);
        model.addAttribute("endpoint", WSOnlineMonitor.getENDPOINTS().get(0));
        model.addAttribute("threadsize", WSOnlineMonitor.appProps.getProperty("thread.size"));
        model.addAttribute("testsize", countXmlFiles(Path.of(WSOnlineMonitor.appProps.getProperty("test.dir"))));
        model.addAttribute("testdir", Path.of(WSOnlineMonitor.appProps.getProperty("test.dir")).getFileName().toString());
        model.addAttribute("queuesize",WSOnlineMonitor.appProps.getProperty("queue.size"));

        return "stats_realtime";
    }
    public static int countXmlFiles(Path directory)  {
        long count = 0;
        try {
            count = Files.walk(directory)
                    .filter(path -> path.toString().endsWith(".xml"))
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            log.error("cannot count files in " +  directory.toFile().getAbsolutePath());
        }
        return (int) count;
    }

}
