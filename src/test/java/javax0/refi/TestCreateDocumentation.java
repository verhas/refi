package javax0.refi;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestCreateDocumentation {


    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }
}
