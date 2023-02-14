package de.vb.monitor;

import java.util.ArrayList;
import java.util.List;

public class EndpointForm {
    private boolean endpoint1;
    private boolean endpoint2;
    private boolean endpoint3;
    private boolean endpoint4;

    public boolean isEndpoint1() {
        return endpoint1;
    }

    public void setEndpoint1(boolean endpoint1) {
        this.endpoint1 = endpoint1;
    }

    public boolean isEndpoint2() {
        return endpoint2;
    }

    public void setEndpoint2(boolean endpoint2) {
        this.endpoint2 = endpoint2;
    }

    public boolean isEndpoint3() {
        return endpoint3;
    }

    public void setEndpoint3(boolean endpoint3) {
        this.endpoint3 = endpoint3;
    }

    public boolean isEndpoint4() {
        return endpoint4;
    }

    public void setEndpoint4(boolean endpoint4) {
        this.endpoint4 = endpoint4;
    }

    public List<String> getSelectedEndpoints() {
        List<String> endpoints = new ArrayList<>();
        if (endpoint1) {
            endpoints.add("https://vbtest3.volkswohl-bund.de/vbl/ws/taa/2022-10/bipro/VBLService_2.4.6.1.12");
        }
        if (endpoint2) {
            endpoints.add("https://vbnet3.volkswohl-bund.de/vbl/ws/taa/2022-10/bipro/VBLService_2.4.6.1.12");
        }
        if (endpoint3) {
            endpoints.add("https://vbtest3.volkswohl-bund.de/vbl/ws/taa/2022-10/bipro/VBLService_2.4.6.1.12");
        }
        if (endpoint4) {
            endpoints.add("https://vbnet3.volkswohl-bund.de/vbl/ws/taa/2022-10/bipro/VBLService_2.4.6.1.12");
        }
        return endpoints;
    }
}
