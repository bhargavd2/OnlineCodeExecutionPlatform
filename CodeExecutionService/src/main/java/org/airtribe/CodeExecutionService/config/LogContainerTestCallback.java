package org.airtribe.CodeExecutionService.config;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;

public class LogContainerTestCallback extends ResultCallback.Adapter<Frame> {
    private final StringBuilder output = new StringBuilder();

    @Override
    public void onNext(Frame frame) {
        output.append(new String(frame.getPayload()));
    }

    public String getOutput() {
        return output.toString();
    }
}