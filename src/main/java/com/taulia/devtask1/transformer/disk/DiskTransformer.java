package com.taulia.devtask1.transformer.disk;

import com.taulia.devtask1.transformer.AbstractTransformer;
import com.taulia.devtask1.transformer.consumer.TransformerConsumer;
import com.taulia.devtask1.transformer.helper.TransformerContext;

public class DiskTransformer extends AbstractTransformer {

    private DiskConsumer diskConsumer;

    @Override
    protected TransformerConsumer getConsumer(TransformerContext context) {
        diskConsumer = new DiskConsumer(context);
        return diskConsumer;
    }
}
