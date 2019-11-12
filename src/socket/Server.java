package socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final ServerSocket serverSocket;
    private final List<Worker> workers = new ArrayList<>();

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    // chỉ chạy khi có kết nối từ máy client mới
    private void waitForConnection() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            Worker worker = new Worker(socket);
            addWorker(worker);
            worker.start();
        }
    }

    // khi hàm trên chạy, thì hàm này thêm 1 clien vào trong list
    private void addWorker(Worker worker) {
        synchronized (this) {
            workers.add(worker);
        }
    }

    // xóa khi có ng nào đó ngắt kết nối
    private void removeWorker(Worker worker) {
        synchronized (this) {
            workers.remove(worker);
            worker.close();
        }
    }

    // Gửi tin nhắn của 1 ng cho tất cả mọi ng còn lại trừ người soạn
    private void broadcastMessage(Worker from, String message) {
        synchronized (this) {
            message = String.format("%s: %s", from.username, message);
            for (int i = 0; i < workers.size(); i++) {
                Worker worker = workers.get(i);
                if (!worker.equals(from)) {
                    try {
                        worker.send(message);
                    } catch (IOException e) {
                        workers.remove(i--);
                        worker.close();
                    }
                }
            }
        }
    }

    // lớp client
    private class Worker extends Thread {
        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;
        private String username = null;

        public Worker(Socket socket) throws IOException {
            this.socket = socket;
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        private void send(String message) throws IOException {
            out.write(message.getBytes());
        }

        @Override
        public void run() {
            byte[] buffer = new byte[2018];
            try {
                while (true) {
                    int receivedBytes = in.read(buffer);
                    // nếu tin nhắn có kích thước nhỏ hơn 1 byte thì ko làm gì cả
                    if (receivedBytes < 1)
                        break;
                    String message = new String(buffer, 0, receivedBytes);
                    // lấy tin nhắn đầu tiên của client làm nick name
                    if (username == null)
                        username = message;
                    else // nếu không phải là tin nhắn đầu thì gửi cho tất cả mọi người còn lại trong nhóm
                        broadcastMessage(this, message);
                }
            } catch (IOException e) {
            }
            removeWorker(this);
        }

        private void close() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(3393);
            server.waitForConnection();
        } catch (IOException e) {
        }
    }

}
