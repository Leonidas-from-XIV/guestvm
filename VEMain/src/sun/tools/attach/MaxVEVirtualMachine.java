/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, U.S.A. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are
 * subject to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems,
 * licensed from the University of California. UNIX is a registered
 * trademark in the U.S.  and in other countries, exclusively licensed
 * through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and
 * may be subject to the export or import laws in other
 * countries. Nuclear, missile, chemical biological weapons or nuclear
 * maritime end uses or end users, whether direct or indirect, are
 * strictly prohibited. Export or reexport to countries subject to
 * U.S. embargo or to entities identified on U.S. export exclusion lists,
 * including, but not limited to, the denied persons and specially
 * designated nationals lists is strictly prohibited.
 *
 */
package sun.tools.attach;

import java.io.*;
import java.net.Socket;

import com.sun.tools.attach.*;
import com.sun.tools.attach.spi.AttachProvider;

import com.sun.max.ve.attach.AttachPort;

public class MaxVEVirtualMachine extends HotSpotVirtualMachine {

    private MaxVEVirtualMachineDescriptor _vmd;

    MaxVEVirtualMachine(AttachProvider provider, String vmid, MaxVEVirtualMachineDescriptor vmd) throws AttachNotSupportedException, IOException {
        super(provider, vmid);
        _vmd = vmd;
    }

    InputStream execute(String cmd, Object... args) throws AgentLoadException, IOException {
        System.out.print("execute:" + cmd);
        final Socket sock = new Socket(_vmd.host(), AttachPort.getPort());
        InputStream in = null;
        OutputStream out = null;
        System.out.println("connected");
        for (Object arg : args) {
            if (arg != null) {
                System.out.print(" ");
                System.out.print(arg);
            }
        }
        System.out.println();
        out = sock.getOutputStream();
        in = sock.getInputStream();
        out.write(1 + args.length);
        writeString(out, cmd);
        for (Object arg : args) {
            writeString(out, (String) arg);
        }
        return in;
    }

    /*
     * Write/sends the given to the target VM. String is transmitted in UTF-8 encoding.
     */
    private void writeString(OutputStream out, String s) throws IOException {
        byte[] b;
        if (s != null && s.length() > 0) {
            try {
                b = s.getBytes("UTF-8");
                out.write(b.length);
                out.write(b);
            } catch (java.io.UnsupportedEncodingException x) {
                throw new InternalError();
            }
        } else {
            out.write(0);
        }
    }

    public void detach() throws IOException {

    }

    @Override
    public void loadAgent(String agent, String options) throws AgentLoadException, AgentInitializationException, IOException {
        final InputStream in = execute("load", agent, "true", options);
        try {
            final int result = readInt(in);
            if (result != 0) {
                throw new AgentInitializationException("Agent_OnAttach failed", result);
            }
        } finally {
            in.close();
        }
    }

    @Override
    public void loadAgentLibrary(String agentLibrary, String options) throws AgentLoadException, AgentInitializationException, IOException {
        throwAgentLibraryNotSupported();
    }

    @Override
    public void loadAgentPath(String agentLibrary, String options) throws AgentLoadException, AgentInitializationException, IOException {
        throwAgentLibraryNotSupported();
    }

    private static void throwAgentLibraryNotSupported() throws AgentLoadException {
        throw new AgentLoadException("native agent libraries not supported");
    }

}
