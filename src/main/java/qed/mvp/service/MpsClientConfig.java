package qed.mvp.service;

import com.mathworks.mps.client.MWHttpClientDefaultConfig;


public class MpsClientConfig extends MWHttpClientDefaultConfig {
    public int getMaxConnectionsPerAddress() {
        return 10;
    }

    public long getTimeOutMs() {
        return 36000000L;
    }

    public boolean isInterruptible() {
        return true;
    }

    public int getResponseSizeLimit() {
        return 1073741824;
    }

}
