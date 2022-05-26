package io.fansir.uploader.http;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class FileProgressRequestBody extends RequestBody {

    public static final int SEGMENT_SIZE = 4*1024; // okio.Segment.SIZE

    protected long fileSize;
    protected File             file;
    protected String currentPercent;
    protected ProgressListener listener;
    protected String           contentType = "application/octet-stream";

    public FileProgressRequestBody(File file, ProgressListener listener) {
        this.file = file;
        this.listener = listener;
        fileSize = file.length();
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;

            while ((read = source.read(sink.getBuffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                String percent = String.format("%.2f",total * 100.0f / fileSize);
                if (!percent.equals(currentPercent)){
                    currentPercent = percent;
                    this.listener.transferred(currentPercent);
                }
            }
        } finally {
            Util.closeQuietly(source);
        }
    }

}
