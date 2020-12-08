package com.taulia.devtask1;


import com.taulia.devtask1.transformer.CompositeTransformer;
import com.taulia.devtask1.transformer.DiskTransformer;
import com.taulia.devtask1.transformer.InMemoryTransformer;
import com.taulia.devtask1.transformer.SplittingTransformer;
import com.taulia.devtask1.transformer.Transformer;
import com.taulia.devtask1.transformer.context.Split;
import com.taulia.devtask1.transformer.context.TransformerConfig;
import com.taulia.devtask1.transformer.context.TransformerContext;
import com.taulia.devtask1.transformer.context.helper.SplitHelper;
import com.taulia.devtask1.transformer.io.model.TransformedItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TransformCommand {

    private SplitHelper helper;
    private CompositeTransformer compositeTransformer;

    public TransformCommand() {
        helper = new SplitHelper();

        final Function<Object, TransformedItem<?>> transformFunction = getTransformFunction();
        final Transformer splittingTransformer = new SplittingTransformer<TransformedItem<?>>(transformFunction);
        final Transformer diskTransformer = new DiskTransformer<TransformedItem<?>>(transformFunction);
        final Transformer inMemoryTransformer = new InMemoryTransformer<TransformedItem<?>>(transformFunction);
        compositeTransformer = new CompositeTransformer(splittingTransformer, diskTransformer, inMemoryTransformer);
    }

    private Function<Object, TransformedItem<?>> getTransformFunction() {
        return o -> new TransformedItem<Object>() {
            @Override
            public Object getPayload() {
                return o;
            }
        };
    }

    public void executeCommand(File inputFile, File outputFolder, TransformerContext.OutputType outputType) {
        final TransformerConfig config = prepareTransformerConfig();
        final TransformerContext context = getTransformerContext(inputFile, outputFolder, outputType, config);

        try {
            compositeTransformer.transform(context);
        }
        catch (Exception exc) {
            cleanUp(context);
        }
    }

    private void cleanUp(TransformerContext context) {
        // clean up the whole output folder
    }

    private TransformerConfig prepareTransformerConfig() {
        TransformerConfig config = new TransformerConfig();
        config.setMaxOpenHandlers(2);
        config.setMaxInMemoryFileSizeInBytes(512*1024);
        config.setTraverPolicy(new DeepFirstTraversePolicy());
        return config;
    }

    private TransformerContext getTransformerContext(File inputFile, File outputFolder, TransformerContext.OutputType outputType, TransformerConfig config) {
        final TransformerContext context = new TransformerContext();
        context.setCurrentSplit(prepareInitialSplit(inputFile, config));
        context.setSplitList(new ArrayList<>());
        context.setOutputFolder(outputFolder);
        context.setOutputType(outputType);
        context.setOutputGroupPrefix("buyer");
        context.setOutputGroupIndex(0L);
        context.setOutputOtherPrefix("other");
        context.setOutputOtherIndex(0L);
        context.setImagePrefix("image");
        context.setImageIndex(0L);
        context.setConfig(config);
        return context;
    }

    private Split prepareInitialSplit(File inputFile, TransformerConfig config) {
        return helper.buildRootSplit(inputFile, config);
    }

    private static class DeepFirstTraversePolicy implements TransformerConfig.TraversePolicy {

        @Override
        public void addSplits(TransformerContext context, List<Split> splits) {
            context.getSplitList().addAll(0, splits);
        }

        @Override
        public Split nextSplit(TransformerContext context) {
            Split result = null;

            if (context.getSplitList().size() > 0) {
                result = context.getSplitList().remove(0);
            }

            return result;
        }
    }
}