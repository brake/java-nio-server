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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

/**
 * <h1>Handler</h1>
 * <p>
 *
 * @author Constantin Roganov {@literal <rccbox @ gmail . com>}
 * @version 1.0.0
 * @since 17.03.16
 */
public class Handler extends Thread {
    private static final int BUFFER_SIZE = 1024 * 6;
    public static final String STOP_WORD = "Bye.";

    private final Queue<SocketChannel> inbox;
    private final Selector selector;

    public Handler(Queue<SocketChannel> exchangeQueue) throws IOException {
        super();

        inbox = exchangeQueue;
        selector = Selector.open();

        setDaemon(true);
    }

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see #start()
     * @see #stop()
     */
    @Override
    public void run() {
        while (true) try {
            checkInbox();
            int keysReady = selector.selectNow();
            if(keysReady > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> activeKeys = selectionKeys.iterator();

                // TODO: try to change to foreach loop
                while (activeKeys.hasNext()) {
                    SelectionKey key = activeKeys.next();
                    if (key.isReadable()) {
                        readFromChannel(key);

                    } else if (key.isWritable()) {
                        writeToChannel(key);
                    }
                    activeKeys.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkInbox() throws IOException {
        while (!inbox.isEmpty()) {
            SocketChannel channel = inbox.poll();
            registerForRead(channel);
        }
    }

    private void registerForRead(SocketChannel channel) throws IOException {
        MessageReader reader = new MessageReaderImpl(channel, BUFFER_SIZE);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, reader);
    }

    private void registerForWrite(SocketChannel channel, ByteBuffer buffer) throws IOException {
        MessageWriter writer = new MessageWriterImpl(channel, buffer);
        channel.register(selector, SelectionKey.OP_WRITE, writer);
    }

    private void readFromChannel(SelectionKey key) throws IOException {
        MessageReader reader = (MessageReader)key.attachment();
        SocketChannel channel = (SocketChannel)key.channel();

        if(reader.read() == -1) {
            channel.close();

            System.out.println("Stop word received, closing channel");

        } else if (reader.hasFullMessage()) {
            registerForWrite(channel, reader.getFullMessage());
            System.out.println("Message received, sending back");
        }
    }

    private void writeToChannel(SelectionKey key) throws IOException {
        MessageWriter writer = (MessageWriter)key.attachment();
        writer.write();

        if(writer.messageWritten()) {
            registerForRead((SocketChannel)key.channel());
        }
    }
}
