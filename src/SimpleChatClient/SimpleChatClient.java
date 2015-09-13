package SimpleChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by ilnurgazizov on 13.09.15.
 */
public class SimpleChatClient {
    JTextArea incoming;
    JTextField outgoing;
    BufferedReader reader;
    PrintWriter writer;
    Socket sock;

    public static void main(String[] args) {
        new SimpleChatClient().go();
    }

    public void go(){
        // Создаем GUI и подключаем слушатель для событий к кнопке отправки
        // Вызываем метод stUpNetWorking()
        JFrame frame = new JFrame("Ludicrously Simple Chat Client");
        JPanel mainPanel = new JPanel();

        incoming = new JTextArea(15, 25);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);

        JScrollPane qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outgoing = new JTextField(20);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        mainPanel.add(qScroller);
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);
        setUpNetWorking();

        // Запускаепм новый поток, используя вложенный класс в качестве Runnable
        // Работа потока заключаемся в чтении данных с сервера через сокет и выводе
        // любых входящих сообщений в прокручиваемую текстовую обалсть.
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(400, 500);
        frame.setVisible(true);
    }

    private void setUpNetWorking(){
        // Создаем сокет и PrintWriter
        // Присваиваем PrintWriter переменной writer
        // Мы используем сокет для получения входящего и исходящего потоков.
        // Исходщего поток уже задействован для отправки данных, но теперь к нему добавился входящий поток,
        // поэтому наш объект Thread может получать сообщения от сервера.
        try {
            sock = new Socket("127.0.0.1", 5000);
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("networking established");
        } catch(IOException ex) {ex.printStackTrace();}
    }
    public class SendButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent ev){
            // Получаем текс изтекстового поля и отправляем
            // его на сервер с помощью переменной writer
            try {
                writer.println(outgoing.getText());
                writer.flush();
            } catch(Exception ex){ex.printStackTrace();}
            outgoing.setText("");
            outgoing.requestFocus();
        }
    }

    // Это работа которую выполняет поток!!!!
    // В методе run() поток входит в цикл(пока ответ сервера будет равняться null)
    // считывает за раз одну строку и добавляет ее в прокурчиваемую текстовую область
    // (используемуя в конце символ переноса строки)

    public class IncomingReader implements Runnable{
        public void run(){
            String message;
            try{
                while ((message = reader.readLine()) != null){
                    System.out.println("read " + message);
                    incoming.append(Thread.currentThread().getName() + ": " + message + "\n");
                }
            } catch(Exception ex){ex.printStackTrace();}
        }
    }
}
