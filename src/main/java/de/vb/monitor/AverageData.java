package de.vb.monitor;

import java.util.List;

public class AverageData {
    private List<Long> averages;

    public AverageData(List<Long> averages) {
        this.averages = averages;
    }

    public List<Long> getAverages() {
        return averages;
    }
}
