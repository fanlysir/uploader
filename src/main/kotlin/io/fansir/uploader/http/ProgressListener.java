package io.fansir.uploader.http;

public interface ProgressListener {
    void transferred(String percent);
}
