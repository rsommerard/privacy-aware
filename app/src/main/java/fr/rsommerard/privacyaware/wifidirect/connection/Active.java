package fr.rsommerard.privacyaware.wifidirect.connection;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataEvent;
import fr.rsommerard.privacyaware.data.DataManager;

public class Active extends Thread implements Runnable {
    private static final int SOCKET_TIMEOUT = 10000;

    private final InetAddress mGroupOwnerAddress;
    private final DataManager mDataManager;
    private final Random mRandom;
    private final EventBus mEventBus;

    public Active(final InetAddress groupOwnerAddress, final DataManager dataManager, final EventBus eventBus) {
        mGroupOwnerAddress = groupOwnerAddress;
        mDataManager = dataManager;

        mRandom = new Random();

        mEventBus = eventBus;
    }

    @Override
    public void run() {
        Socket socket = connect();

        if (socket == null) {
            Log.e(WiFiDirect.TAG, "Cannot open socket with the groupOwner");
            return;
        }

        try {
            sendMessage(socket, Protocol.HELLO);
            waitAndCheck(socket, Protocol.HELLO);
            sendData(socket);
            waitData(socket);
            socket.close();
        } catch (ClassNotFoundException | IOException | SQLiteConstraintException e) {
            e.printStackTrace();
            closeSocket(socket);
        }
    }

    private void waitData(Socket socket) throws IOException, ClassNotFoundException, SQLiteConstraintException {
        sendMessage(socket, Protocol.SEND);

        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        String gsonStr = (String) objectInputStream.readObject();

        List<Data> data = DataManager.deGsonify(gsonStr);

        Log.i(WiFiDirect.TAG, data + " received");

        for (Data d : data) {
            mEventBus.post(new DataEvent(d));
        }

        sendMessage(socket, Protocol.ACK);
    }

    private void sendData(final Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

        List<Data> data = mDataManager.getAllData();
        Collections.shuffle(data);

        int nb = mRandom.nextInt(data.size());
        Log.i(WiFiDirect.TAG, "Nb data to send: " + nb);

        List<Data> dataList = new ArrayList<Data>();
        for (int i = 0; i < nb; i++) {
            dataList.add(new Data(null, data.get(i).getContent()));
        }

        waitAndCheck(socket, Protocol.SEND);
        objectOutputStream.writeObject(DataManager.gsonify(dataList));
        objectOutputStream.flush();

        Log.d(WiFiDirect.TAG, DataManager.gsonify(dataList) + " sent");

        waitAndCheck(socket, Protocol.ACK);

        for (int i = 0; i < nb; i++) {
            mDataManager.removeData(data.get(i));
        }
    }

    private void closeSocket(final Socket socket) {
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage(final Socket socket, final String message) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        Log.i(WiFiDirect.TAG, message + " sent");
    }

    private void waitAndCheck(final Socket socket, final String message) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        String received = (String) objectInputStream.readObject();

        Log.i(WiFiDirect.TAG, received + " received");

        if (!message.equals(received)) {
            closeSocket(socket);
        }
    }

    private Socket connect() {
        InetSocketAddress inetSocketAddress =
                new InetSocketAddress(mGroupOwnerAddress, 54412);

        try {
            Socket socket = new Socket();
            socket.connect(inetSocketAddress, SOCKET_TIMEOUT);
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}