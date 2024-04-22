package io.dashbase.clue.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import io.dropwizard.jackson.Jackson;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultAnalyzerFactory implements AnalyzerFactory {

    private static final String DEFAULT_ANALYZER = "org.apache.lucene.analysis.standard.StandardAnalyzer";

    private static final ForStageSettings DEFAULT_SETTINGS = new ForStageSettings(
            new AnalyzerConfiguration(
                    DEFAULT_ANALYZER, null, Collections.emptyList(), Collections.emptyList()
            ),
            Collections.emptyMap()
    );

    public DefaultAnalyzerFactory(ForStageSettings forQuery, ForStageSettings forIndexing) {
        this.forQuery = forQuery;
        this.forIndexing = forIndexing;
    }

    public DefaultAnalyzerFactory() {
        this(DEFAULT_SETTINGS, DEFAULT_SETTINGS);
    }

    public static class AnalyzerConfiguration {
        private final String className;
        private final ComponentConfiguration tokenizer;
        private final List<ComponentConfiguration> tokenFilters;
        private final List<ComponentConfiguration> charFilters;

        @JsonCreator
        public AnalyzerConfiguration(String className, ComponentConfiguration tokenizer, List<ComponentConfiguration> tokenFilters, List<ComponentConfiguration> charFilters) {
            this.className = className;
            this.tokenizer = tokenizer;
            this.tokenFilters = MoreObjects.firstNonNull(tokenFilters, Collections.emptyList());
            this.charFilters = MoreObjects.firstNonNull(charFilters, Collections.emptyList());
        }

        public String getClassName() {
            return className;
        }

        public ComponentConfiguration getTokenizer() {
            return tokenizer;
        }

        public List<ComponentConfiguration> getTokenFilters() {
            return tokenFilters;
        }

        public List<ComponentConfiguration> getCharFilters() {
            return charFilters;
        }
    }

    private final ForStageSettings forQuery;
    private final ForStageSettings forIndexing;

    public static class ComponentConfiguration {
        private String className;
        private Map<String, String> args;
    }

    public static class ForStageSettings {

        private final AnalyzerConfiguration defaultAnalyzer;
        private final Map<String, AnalyzerConfiguration> fieldAnalyzers;

    public ForStageSettings(AnalyzerConfiguration defaultAnalyzer, Map<String, AnalyzerConfiguration> fieldAnalyzers) {
        this.defaultAnalyzer = defaultAnalyzer;
        this.fieldAnalyzers = fieldAnalyzers;
    }

        public AnalyzerConfiguration getDefaultAnalyzer() {
            return defaultAnalyzer;
        }

        public Map<String, AnalyzerConfiguration> getFieldAnalyzers() {
            return fieldAnalyzers;
        }
    }

    public ForStageSettings getForQuery() {
        return forQuery;
    }

    public ForStageSettings getForIndexing() {
        return forIndexing;
    }

    @Override
    public Analyzer forQuery() throws Exception {
        return createForFields(forQuery);
    }

    @Override
    public Analyzer forIndexing() throws Exception {
        return createForFields(forIndexing);
    }

    private static Analyzer createForFields(ForStageSettings settings) throws Exception {
        Map<String, Analyzer> analyzersByFieldName = new HashMap<>();
        for (Map.Entry<String, AnalyzerConfiguration> nameAndConfig : settings.fieldAnalyzers.entrySet()) {
            analyzersByFieldName.put(
                    nameAndConfig.getKey(),
                    create(nameAndConfig.getValue())
            );
        }
        return new PerFieldAnalyzerWrapper(
                create(settings.defaultAnalyzer),
                analyzersByFieldName
        );
    }

    @SuppressWarnings("unchecked")
    private static Analyzer create(AnalyzerConfiguration config) throws Exception {
        if (config.className != null) {
            Class<Analyzer> analyzerClass = (Class<Analyzer>) Class.forName(config.className);
            return analyzerClass.getDeclaredConstructor().newInstance();
        }
        CustomAnalyzer.Builder builder = CustomAnalyzer.builder();
        builder.withTokenizer(
                config.tokenizer.className,
                config.tokenizer.args
        );

        for (ComponentConfiguration tokenFilter : config.tokenFilters) {
            builder.addTokenFilter(
                    tokenFilter.className,
                    tokenFilter.args
            );
        }

        for (ComponentConfiguration charFilter : config.charFilters) {
            builder.addCharFilter(
                    charFilter.className,
                    charFilter.args
            );
        }

        return builder.build();
    }

}
