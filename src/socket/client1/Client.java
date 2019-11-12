package socket.client1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client extends Thread {
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public Client(String serverAddress, int serverPort) throws UnknownHostException, IOException {
        socket = new Socket(InetAddress.getByName(serverAddress), serverPort);
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    private void send(String message) throws IOException {
        out.write(message.getBytes());
    }

    // nhận tin nhắn, chuyển từ byte về String để in ra tin nhắn từ client khác nhắn tới
    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        try {
            while (true) {
                int receivedBytes = in.read(buffer);
                if (receivedBytes < 1)
                    break;
                String message = new String(buffer, 0, receivedBytes);
                System.out.println(message);
            }
        } catch (IOException e) {
        }
        close();
        System.exit(0);
    }

    private void close() {
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    // đầu tiên sẽ bắt nhập vào 1 chuỗi ký tự, nó sẽ là nick name của client muốn nhắn tin
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client client = null;
        String username = scanner.nextLine();
        try {
            // mở kết nối tới server (hàm đợi client mới sẽ chạy )
            client = new Client("localhost", 3393);
            // gửi nick name cho server để server biết là ai và gắn vào List,
            // tin nhắn về sau sẽ có dạng nick name: content tin nhắn
            client.send(username);
            client.start();
            while (true) {
                String message = scanner.nextLine();
                client.send(message);
            }
        } catch (IOException e) {
        }
        if (client != null)
            client.close();
        scanner.close();
    }
}