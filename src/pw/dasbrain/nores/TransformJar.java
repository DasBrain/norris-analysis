package pw.dasbrain.nores;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class TransformJar implements Closeable {
    
    static final String INPUT_JAR = "codesnores.jar";
    static final String OUTPUT_JAR = "codesnores-simpl.jar";
    
    private final FileSystem out;
    private final FileSystem in;
    
    public TransformJar() throws IOException {
        in = FileSystems.newFileSystem(Path.of(INPUT_JAR));
        Path outjar = Path.of(OUTPUT_JAR);
        Files.deleteIfExists(outjar);
        String outpath = outjar.toAbsolutePath().toString()
                .replace(File.separatorChar, '/');
        out = FileSystems.newFileSystem(URI.create("jar:file:/" + outpath),
                Map.of("create", "true"));
    }
    
    public static void main(String[] args) throws IOException {
        ASMMapper.init(Path.of("ASM-5.0.4-noshrink.zip"));
        try (var transformJar = new TransformJar()) {
            transformJar.run();
        }
    }
    
    private String mapName(String original) {
        original = NorrisRemapper.INSTANCE.map(original);
        original = ASMMapper.INSTANCE.map(original);
        return original;
    }
    
    private void transform(Path p) {
        try {
            Path pout = out.getPath(p.toString());
            if (Files.isRegularFile(p)) {
                byte[] bytes = Files.readAllBytes(p);
                String name = p.toString();
                if (name.endsWith(".class")) {
                    bytes = ClassTransformer.transform(bytes);
                    pout = out.getPath("/" + mapName(name.substring(1, name.length() - 6))
                            + ".class");
                    // just to be sure
                    Files.createDirectories(pout.getParent());
                }
                
                Files.write(pout, bytes, StandardOpenOption.CREATE);
            } else if (Files.isDirectory(p)) {
                Files.createDirectories(pout);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void run() throws IOException {
        for (var root : in.getRootDirectories()) {
            try (var files = Files.find(root, 20, (a, b) -> true)) {
                files.forEach(this::transform);
            }
        }
    }
    
    @Override
    public void close() throws IOException {
        try {
            in.close();
        } finally {
            out.close();
        }
        
    }
}