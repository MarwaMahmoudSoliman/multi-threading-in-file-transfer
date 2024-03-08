
import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String[] fileNames = { "file1.txt", "file2.txt" }; // Two file names to download
        for (String fileName : fileNames) {
            new Thread(() -> {
                try (Socket socket = new Socket("localhost", 1000)) {
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                    // Send file name to server
                    outputStream.writeUTF(fileName);

                    // Receive confirmation from server
                    boolean fileExists = inputStream.readBoolean();
                    if (fileExists) {
                        // Receive file size
                        long fileSize = inputStream.readLong();

                        // Receive file data
                        File receivedFile = new File("downloaded_" + fileName);
                        try (FileOutputStream fileOutputStream = new FileOutputStream(receivedFile)) {
                            byte[] buffer = new byte[4096];

                            long remainingBytes = fileSize;
                            int bytesRead;
                            while (remainingBytes > 0 && (bytesRead = inputStream.read(buffer, 0,
                                    (int) Math.min(buffer.length, remainingBytes))) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                                remainingBytes -= bytesRead;
                            }
                        }
                        System.out.println("File received: " + receivedFile.getName());
                    } else {
                        System.out.println("File does not exist on server");
                    }

                    // Close streams and socket
                    outputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}