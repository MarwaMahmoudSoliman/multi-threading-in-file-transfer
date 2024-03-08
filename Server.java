import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1000)) {
            System.out.println("Server is listening on port 1000");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to client");

                // Start a new thread to handle each client
                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());

        // Read file name from client
        String fileName = inputStream.readUTF();
        System.out.println("Client requested file: " + fileName);

        // Read the file and send it to the client
        File fileToSend = new File(fileName);
        if (fileToSend.exists() && !fileToSend.isDirectory()) {
            // Send confirmation to client
            outputStream.writeBoolean(true);

            // Send file size to client
            outputStream.writeLong(fileToSend.length());

            // Send file data to client
            try (FileInputStream fileInputStream = new FileInputStream(fileToSend)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("File sent successfully to client");
        } else {
            // If file doesn't exist, send false to client
            outputStream.writeBoolean(false);
            System.out.println("File not found on server");
        }

        // Close streams and socket
        outputStream.close();
        inputStream.close();
        clientSocket.close();
    }
}
