package de.vb.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RestartController {

    @Autowired
    private ConfigurableApplicationContext context;

    @GetMapping("/restart")
    public void restart() {
        SpringApplication.exit(context, () -> 0);
        SpringApplication.run(WSOnlineMonitor.class);
    }
}
