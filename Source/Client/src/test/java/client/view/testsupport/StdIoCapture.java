package client.view.testsupport;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Test-only helper to capture {@link System#out} and {@link System#err} using UTF-8.
 */
public final class StdIoCapture implements AutoCloseable {

    private final PrintStream originalOut;
    private final PrintStream originalErr;

    private final ByteArrayOutputStream outBuffer;
    private final ByteArrayOutputStream errBuffer;

    public StdIoCapture() {
        this.originalOut = System.out;
        this.originalErr = System.err;
        this.outBuffer = new ByteArrayOutputStream();
        this.errBuffer = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outBuffer, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
    }

    public String stdout() {
        return outBuffer.toString(StandardCharsets.UTF_8);
    }

    public String stderr() {
        return errBuffer.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}
