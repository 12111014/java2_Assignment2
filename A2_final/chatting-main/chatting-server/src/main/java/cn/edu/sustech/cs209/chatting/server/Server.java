package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Client;
import cn.edu.sustech.cs209.chatting.common.Room;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private String hostIP = "localhost";

    private static int port = 8765;

    private static volatile List<Client> onlineClient;

    private static List<Client> allClient;

    public static List<Service> allService;

    public static List<Room> allOnlineRoom;

    public static void main(String[] args) throws IOException {
        onlineClient = new ArrayList<>();
        allClient = new ArrayList<>();
        allService = new ArrayList<>();
        allOnlineRoom = new ArrayList<>();
        getClientInfo();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("waiting for clients to connect...");

        while (true) {
            Socket socket = serverSocket.accept();
            Socket socketForClient = serverSocket.accept();
            Socket socketForMessage = serverSocket.accept();
            Service service = new Service(socket, socketForClient,
                    socketForMessage,
                    allClient, onlineClient, allService, allOnlineRoom);
            Thread t = new Thread(service);
            t.start();
            allService.add(service);
            //  TODO to check the client's name and password
        }
    }

    private static void getClientInfo() {
        Path path = Paths.get(
                "chatting-server\\src\\main\\resources\\allClients.csv");
        File allClientFile = new File(path.toUri());
        try (FileReader fileReader = new FileReader(allClientFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)
        ) {
            bufferedReader.readLine();
            String info;
            while (true) {
                info = bufferedReader.readLine();
                if (info == null) {
                    break;
                }
                String[] clientInfo = info.split(",");
                Client client = new Client(
                        Integer.parseInt(clientInfo[0]), clientInfo[1], clientInfo[2]);
                allClient.add(client);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
