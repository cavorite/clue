package io.dashbase.clue.api;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomizedAnalyzerFactoryTest {

    @Test
    void loadFromJavascript() throws IOException, ScriptException {
        CustomizedAnalyzerFactory factory;
        CharSource configSource = Resources.asCharSource(
                Resources.getResource("io/dashbase/clue/api/test_config1.js")
                , Charsets.UTF_8);
        try (BufferedReader r = configSource.openBufferedStream()) {
            factory = CustomizedAnalyzerFactory.loadFromJavascript(r);
        }

        assertNotNull(factory.forQuery());
        assertNotNull(factory.forIndexing());
    }

}