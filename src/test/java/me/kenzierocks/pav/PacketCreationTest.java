package me.kenzierocks.pav;

import com.google.auto.value.processor.AutoValueProcessor;
import com.google.common.collect.ImmutableList;
import com.google.testing.compile.JavaFileObjects;
import me.kenzierocks.pav.avext.AVExtPacket;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.Assert.fail;

public class PacketCreationTest {

    private static final Path SOURCES_LOCATION = Paths.get("src/test/resources");
    private static final String PACKAGE = "me.kenzierocks.pav.sources";

    @Test
    public void helloWorld() {
        testCompiles("HelloWorld");
    }

    private void testCompiles(String className) {
        assertThat(className).isNotEmpty();
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource(convertClassNameToFile(className)))
                .processedWith(new AutoValueProcessor(ImmutableList.of(new AVExtPacket())))
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource(convertClassNameToFile("AutoValue_" + className)));
    }

    private URL convertClassNameToFile(String className) {
        String fileRel = PACKAGE.replace('.', '/') + "/" + className + ".java";
        try {
            return SOURCES_LOCATION.resolve(fileRel).toUri().toURL();
        } catch (MalformedURLException e) {
            fail("Couldn't resolve file: " + e.getMessage());
            throw new AssertionError("impossible to reach");
        }
    }

}


