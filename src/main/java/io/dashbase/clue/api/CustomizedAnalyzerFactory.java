package io.dashbase.clue.api;

import org.apache.lucene.analysis.Analyzer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustomizedAnalyzerFactory implements AnalyzerFactory {

    private final Analyzer forQuery;
    private final Analyzer forIndexing;

    public CustomizedAnalyzerFactory(Analyzer forQuery, Analyzer forIndexing) {
        this.forQuery = forQuery;
        this.forIndexing = forIndexing;
    }

    static CustomizedAnalyzerFactory loadFromJavascript(
            Path jsConfigurationFile) throws ScriptException, IOException {
        return loadFromJavascript(Files.newBufferedReader(jsConfigurationFile));
    }

    static CustomizedAnalyzerFactory loadFromJavascript(
            BufferedReader jsConfigReader) throws ScriptException, IOException {

        ScriptEngineManager engineManager =
                new ScriptEngineManager();

        ScriptEngine engine =
                engineManager.getEngineByExtension("js");
        String packages = String.join(",",
                "org.apache.lucene.analysis.core",
                "org.apache.lucene.analysis.custom",
                "org.apache.lucene.analysis.miscellaneous",
                "org.apache.lucene.analysis.standard");
        String scriptHeader = String.format("var LuceneAnalysis = new JavaImporter(%s);", packages);
        engine.eval(scriptHeader);

        StringBuilder configJsScript = new StringBuilder();
        configJsScript.append("\nwith (LuceneAnalysis) {\n");
        jsConfigReader.lines().forEach(line -> {
            configJsScript.append(line).append('\n');
        });
        configJsScript.append("\n}\n");
        engine.eval(configJsScript.toString());

        // TODO: Use fallback values
        Analyzer forQuery = (Analyzer) engine.eval("forQuery");
        Analyzer forIndexing = (Analyzer) engine.eval("forIndexing");
        return new CustomizedAnalyzerFactory(forQuery, forIndexing);
    }

    @Override
    public Analyzer forQuery() {
        return forQuery;
    }

    @Override
    public Analyzer forIndexing() {
        return forIndexing;
    }
}
