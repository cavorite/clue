package io.dashbase.clue;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ClueAppConfigurationTest {

    @Test
    public void testLoadConfiguration() throws Exception{
        File exampleConfig = new File("/tmp/a.yml");

        ClueAppConfiguration c = ClueAppConfiguration.load(exampleConfig);
        assertNotNull(c.analyzerFactory.forQuery());;
    }


    private void testWriteConfiguration() throws Exception{
        File exampleConfig = new File("/tmp/a.yml");
        ClueAppConfiguration.load(exampleConfig);

        ClueAppConfiguration config = new ClueAppConfiguration();

        ClueAppConfiguration.MAPPER.writeValue(
                exampleConfig,
                config
        );
    }
}