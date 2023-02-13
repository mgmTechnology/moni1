package de.vb.monitor;

public class ResponseDTO {
    private String rBody;
    private int rStatuscode;
    private long rTime;

    public ResponseDTO(String rBody, int rStatuscode, long rTime) {
        this.rBody = rBody;
        this.rStatuscode = rStatuscode;
        this.rTime = rTime;
    }

    public ResponseDTO() {
    }

    public String getrBody() {
        return rBody;
    }

    public void setrBody(String rBody) {
        this.rBody = rBody;
    }

    public int getrStatuscode() {
        return rStatuscode;
    }

    public void setrStatuscode(int rStatuscode) {
        this.rStatuscode = rStatuscode;
    }

    public long getrTime() {
        return rTime;
    }

    public void setrTime(long rTime) {
        this.rTime = rTime;
    }
}