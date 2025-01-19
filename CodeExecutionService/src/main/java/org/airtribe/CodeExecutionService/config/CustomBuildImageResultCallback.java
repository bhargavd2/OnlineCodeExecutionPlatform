package org.airtribe.CodeExecutionService.config;

import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;

public class CustomBuildImageResultCallback extends BuildImageResultCallback {
    @Override
    public void onNext(BuildResponseItem item) {
        System.out.print(item.getStream());
        super.onNext(item);
    }
}
