package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ByteBufferNativeDispatcher extends NativeDispatcher {

    public abstract int write(FileDescriptor fdObj, ByteBuffer bb) throws IOException;
    public abstract int read(FileDescriptor fdObj, ByteBuffer bb) throws IOException;

}
