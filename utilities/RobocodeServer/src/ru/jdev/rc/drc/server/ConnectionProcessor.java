/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;

public class ConnectionProcessor {

    private final Map<SocketChannel, Client> clients = new HashMap<SocketChannel, Client>();

    private final CommandsQueue commandsQueue;

    private ServerSocketChannel serverChannel;
    private Selector socketSelector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 500);
    private boolean isRunned;

    public ConnectionProcessor(CommandsQueue commandsQueue) {
        this.commandsQueue = commandsQueue;
    }

    public void run() throws IOException {
        // Create a new selector
        socketSelector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress("localhost", 1986);
        serverChannel.socket().bind(isa);

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        isRunned = true;
        while (isRunned) {
            try {
                // Wait for an event one of the registered channels
                System.out.println("Selecting...");
                socketSelector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = socketSelector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Connection processor stopped");
    }

    public void stop() {
        isRunned = false;
        try {
            socketSelector.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void read(SelectionKey key) throws IOException, ClassNotFoundException {
        System.out.println("Reading...");
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }

        // Hand the data off to our worker thread
        commandsQueue.addBattleRequest(new Command(clients.get(socketChannel), (BattleRequest) new ObjectInputStream(new ByteArrayInputStream(readBuffer.array())).readObject()));
    }

    private void accept(SelectionKey key) throws IOException {
        System.out.println("Accepting...");
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(socketSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        clients.put(socketChannel, new Client(socketChannel));
    }

    public static void main(String[] args) throws IOException {
        CommandsQueue commandsQueue = new CommandsQueue("st");
        Executors.newSingleThreadExecutor().submit(new CommandsQueueProcessor(commandsQueue, new RCBattlesExecutor()));
        final ConnectionProcessor cp = new ConnectionProcessor(commandsQueue);
        cp.run();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                cp.stop();
            }
        });
    }

}
