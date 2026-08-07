package models;

public class AlgorithmRun {
    private final int runId;
    private final String algorithmName;
    private final int inputSize;
    private final long timeNs;
    private final long memoryKb;
    private final String dateRun;

    public AlgorithmRun(int runId, String algorithmName, int inputSize, long timeNs, long memoryKb, String dateRun) {
        this.runId = runId;
        this.algorithmName = algorithmName;
        this.inputSize = inputSize;
        this.timeNs = timeNs;
        this.memoryKb = memoryKb;
        this.dateRun = dateRun;
    }

    public int getRunId() { return runId; }
    public String getAlgorithmName() { return algorithmName; }
    public int getInputSize() { return inputSize; }
    public long getTimeNs() { return timeNs; }
    public long getMemoryKb() { return memoryKb; }
    public String getDateRun() { return dateRun; }

    @Override
    public String toString() {
        return String.format("Run{id=%d, algo='%s', size=%d, time=%,dns, mem=%,dKB, date='%s'}",
                runId, algorithmName, inputSize, timeNs, memoryKb, dateRun);
    }
}
