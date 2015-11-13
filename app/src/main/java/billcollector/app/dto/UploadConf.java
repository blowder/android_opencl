package billcollector.app.dto;

/**
 * Created by vfedin on 11.11.2015.
 */
public class UploadConf {
    private int chunkSize;
    private int maxSize;
    private int optWidth;
    private int minWidth;
    private int timeout;
    private String expectedMime;

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getOptWidth() {
        return optWidth;
    }

    public void setOptWidth(int optWidth) {
        this.optWidth = optWidth;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getExpectedMime() {
        return expectedMime;
    }

    public void setExpectedMime(String expectedMime) {
        this.expectedMime = expectedMime;
    }

    @Override
    public String toString() {
        return "UploadConf{" +
                "chunkSize=" + chunkSize +
                ", maxSize=" + maxSize +
                ", optWidth=" + optWidth +
                ", minWidth=" + minWidth +
                ", timeout=" + timeout +
                ", expectedMime='" + expectedMime + '\'' +
                '}';
    }
}
