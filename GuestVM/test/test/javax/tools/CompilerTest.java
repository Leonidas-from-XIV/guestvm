package test.javax.tools;

import javax.tools.*;
import java.util.*;
import java.io.*;

public class CompilerTest {

    public static void main(String args[]) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        System.out.println(compiler);
        File[] files1 = new File[] { new File(args[0])};
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable< ? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files1));
        String[] options = new String[args.length - 1];
        System.arraycopy(args, 1, options, 0, options.length);
        boolean result = compiler.getTask(null, fileManager, null, Arrays.asList(options), null, compilationUnits1).call();
        System.out.println("Done: " + result);
    }
}

