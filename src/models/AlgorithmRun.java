package models;

public class AlgorithmRun {
    private final int runId;
    private final String algorithmName;
    private final int inputSize;
    private final long timeNs;
    private final long memoryKb;
    private final String dateRun;
    private final long operationsCount;
    private final long comparisonsCount;
    private final String status;
    private final String resultSummary;

    public AlgorithmRun(int runId, String algorithmName, int inputSize, long timeNs, long memoryKb, String dateRun) {
        this(runId, algorithmName, inputSize, timeNs, memoryKb, dateRun, 0L, 0L, "SUCCESS", "Completed");
    }

    public AlgorithmRun(int runId, String algorithmName, int inputSize, long timeNs, long memoryKb,
                        String dateRun, long operationsCount, long comparisonsCount, String status, String resultSummary) {
        this.runId = runId;
        this.algorithmName = algorithmName;
        this.inputSize = inputSize;
        this.timeNs = timeNs;
        this.memoryKb = memoryKb;
        this.dateRun = dateRun;
        this.operationsCount = operationsCount;
        this.comparisonsCount = comparisonsCount;
        this.status = status != null ? status : "SUCCESS";
        this.resultSummary = resultSummary != null ? resultSummary : "Completed";
    }

    public int getRunId() { return runId; }
    public String getAlgorithmName() { return algorithmName; }
    public int getInputSize() { return inputSize; }
    public long getTimeNs() { return timeNs; }
    public long getMemoryKb() { return memoryKb; }
    public String getDateRun() { return dateRun; }
    public long getOperationsCount() { return operationsCount; }
    public long getComparisonsCount() { return comparisonsCount; }
    public String getStatus() { return status; }
    public String getResultSummary() { return resultSummary; }

    @Override
    public String toString() {
        return String.format("Run{id=%d, algo='%s', size=%d, time=%,dns, mem=%,dKB, ops=%d, cmps=%d, status='%s'}",
                runId, algorithmName, inputSize, timeNs, memoryKb, operationsCount, comparisonsCount, status);
    }
}
