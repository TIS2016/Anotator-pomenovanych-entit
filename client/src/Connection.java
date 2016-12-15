import java.io.*;
import java.net.Socket;

/**
 * Created by michal on 12/7/16.
 */
public class Connection implements Runnable {

    private Socket s;
    private BufferedWriter bw;

    public Connection(Socket s) {
        this.s = s;
        new Thread(this).start();
    }

    public void sendDate(String data) throws IOException {
        bw.write(data);
        bw.newLine();
        bw.flush();
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            while (true) {
                String x = br.readLine();
                System.out.println(x);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
