package DailyAdviceClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ilnurgazizov on 12.09.15.
 */
public class DailyAdviceServer {
    String[] adviceList = {"Ешьте меньшими порциями", "Купите облегающие джинсы", "Два слова: не годится", "Будьте честны хотя бы сегодня"};

    public void go(){
        try{
            ServerSocket serverSock = new ServerSocket(4242);
            // Сервер входит в постоянный цикл, ожидая клиентских подключений
            while(true){
                Socket sock = serverSock.accept();

                PrintWriter writer = new PrintWriter(sock.getOutputStream());
                String advice = getAdvice();
                writer.println(advice);
                writer.close();
                System.out.println(advice);
            }
            } catch(IOException ex) {ex.printStackTrace();}
    }

    private String getAdvice(){
        int random = (int)(Math.random() * adviceList.length);
        return adviceList[random];
    }

    public static void main(String[] args) {
        DailyAdviceServer server = new DailyAdviceServer();
        server.go();
    }
}
