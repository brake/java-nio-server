/*
 * Copyright (c) 2016 Constantin Roganov 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * <h1>MessageReaderImpl</h1>
 * <p>
 *
 * @author Constantin Roganov {@literal <rccbox @ gmail . com>}
 * @version 1.0.0
 * @since 17.03.16
 */
class MessageReaderImpl implements MessageReader {

    private final ByteBuffer buffer;
    private final SocketChannel channel;
    private boolean haveFullMessage;

    MessageReaderImpl(SocketChannel channel, int bufferSize) {
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.channel = channel;
        this.haveFullMessage = false;
    }

    @Override
    public int read() throws IOException {

        int bytesRead = 0;
        int totalRead = 0;

        do {
            totalRead += bytesRead;
            bytesRead = channel.read(buffer);

        } while (bytesRead > 0);

        if(totalRead == 0) {
            return 0;
        }
        String data = new String(
                buffer.array(),
                buffer.arrayOffset(),
                buffer.limit() - buffer.arrayOffset()).trim();

        if(containsStopWord()) {
            return -1;
        }
        buffer.flip();
        haveFullMessage = true;

        return bytesRead == 0 ? totalRead : -1;
    }

    @Override
    public boolean hasFullMessage() {
        return haveFullMessage;
    }

    @Override
    public ByteBuffer getFullMessage() {
        return hasFullMessage() ? buffer: null;
    }

    private boolean containsStopWord() {
        String data = new String(
                    buffer.array(),
                    buffer.arrayOffset(),
                    buffer.limit() - buffer.arrayOffset())
                .trim();

        return data.equals(Handler.STOP_WORD);
    }
}
