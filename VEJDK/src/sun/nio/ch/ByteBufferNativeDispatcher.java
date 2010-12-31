package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ByteBufferNativeDispatcher extends NativeDispatcher {

    public abstract int write(FileDescriptor fdObj, ByteBuffer bb) throws IOException;
    public abstract int write(FileDescriptor fdObj, ByteBuffer[] bbs) throws IOException;
    public abstract int write(FileDescriptor fdObj, long fileOffset, ByteBuffer... bbs) throws IOException;

    public abstract int read(FileDescriptor fdObj, ByteBuffer bb) throws IOException;
    public abstract int read(FileDescriptor fdObj, ByteBuffer[] bbs) throws IOException;
    public abstract int read(FileDescriptor fdObj, long fileOffset , ByteBuffer... bb) throws IOException;
}
