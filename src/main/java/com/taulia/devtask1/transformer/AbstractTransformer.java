package com.taulia.devtask1.transformer;

import com.taulia.devtask1.io.InputReader;
import com.taulia.devtask1.io.data.InvoiceRecord;
import com.taulia.devtask1.io.reader.CsvReader;
import com.taulia.devtask1.io.reader.XmlReader;
import com.taulia.devtask1.io.reader.converter.CsvRowToInvoiceRecordConverter;
import com.taulia.devtask1.transformer.consumer.TransformerConsumer;
import com.taulia.devtask1.transformer.helper.TransformerContext;
import com.taulia.devtask1.transformer.splitter.Split;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public abstract class AbstractTransformer implements Transformer {

    @Override
    public Split[] transform(TransformerContext context) throws Exception {
        final File inputFile = context.getCurrentSplit().getInputFile();
        final InputReader<InvoiceRecord> inputReader = findReader(inputFile);

        final TransformerConsumer transformerConsumer = getConsumer(context);
        return transformerConsumer.process(inputReader);
    }

    private InputReader<InvoiceRecord> findReader(File inputFile) throws IOException {
        InputReader<InvoiceRecord> result;
        final String cannonicalPath = inputFile.getCanonicalPath().toLowerCase(Locale.ROOT);
        if (cannonicalPath.endsWith(".csv")) {
            result = new CsvReader(inputFile, new CsvRowToInvoiceRecordConverter());
        }
        else if (cannonicalPath.endsWith(".xml")) {
            result = new XmlReader(inputFile);
        }
        else {
            throw new IllegalArgumentException("Only .csv and .xml are supported. File: " + cannonicalPath + " is not one of them. " );
        }

        return result;
    }

    protected abstract TransformerConsumer getConsumer(TransformerContext context);

}
