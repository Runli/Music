package BeatBox;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by ilnurgazizov on 08.09.15.
 * App from Head First "Learning Java" + some my new features and updates
 */
public class BeatBox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxList; // for checkboxes
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
        "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
        "High Agogo", "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().buildGui();
    }

    public void buildGui(){
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Пустая граница позволяет создать поля между краями панели и местом размещения компонентов.

        checkBoxList = new ArrayList<JCheckBox>(); // For all checkboxes
        Box buttonBox = new Box(BoxLayout.Y_AXIS); // For Start Stop Tempo Up and Tempo Down buttons

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton clear = new JButton("Clean");
        clear.addActionListener(new MyClearListener());
        buttonBox.add(clear);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        newMenuItem.addActionListener(new MyReadInListener ());
        saveMenuItem.addActionListener(new MySendListener());
        fileMenu.add(newMenuItem);
        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);
        theFrame.setJMenuBar(menuBar);

//        JButton save = new JButton("Save");
//        save.addActionListener(new MySendListener());
//        buttonBox.add(save);
//
//        JButton savedMusic = new JButton("New");
//        savedMusic.addActionListener(new MyReadInListener());
//        buttonBox.add(savedMusic);

        // All names of instruments of dramm
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }
        // Add to our panel buttonBox and nameBox
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(1);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        // Создаем флажки, присваиваем им значения false (чтобы они не были установлены),
        // а затем добавляем их в массив ArrayList и на панель
        for (int i = 0; i < 256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }
    // Method for setup Midi
    // Обычный Midi-код для получения синтезатора, сиквинсера и дорожки
    public void setUpMidi(){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4); // создаем последовательность и дорожку
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120); //
        }catch(Exception ex) {ex.printStackTrace();}
    }
    // Тут все происходит! Преобразуем состояния флажков в MIDI-события и добавляем их на дорожку
    public void buildTrackAndStart(){
        int[] trackList = null; //Создаем массив из 16 элементов, чтобы хранить значения для каждого инструмента, на все 16 тактов.

        sequence.deleteTrack(track); // Избавляемся от старой дорожки и создаем новую.
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) { // Делаем это для каждого из 16 рядов
            trackList = new int[16];

            int key = instruments[i]; // Задаем клавишу, которая представляет инструмент.
            // Массив содержит MIDI-числа для каждого инструмента

            for (int j = 0; j < 16; j++) { // делаем это для каждого такта текущего ряда
                JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16 * i));
                if ( jc.isSelected()){
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }
            makeTracks(trackList); // Для этого инструмента и для всех 16 тактов создаем события и добавляем их на дорожку
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        track.add(makeEvent(192, 9, 1, 0, 15)); // мы всегда должны быть уверены, что событие на ткт 16 существует(они идут от 0 до 15)
                                                // Иначе BeatBox может не пройти все 16 тактов, перед тем как заново начнет последовательность.
        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY); // Позволяет задать количество повторений цикла или, ка в этом случае, непрерывный цикл.
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch(Exception e) {e.printStackTrace();} // Теперь мы проигрываем мелодию!

    }
    // Inner class for start
    public class MyStartListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    }
    // Inner class for stop
    public class MyStopListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            sequencer.stop();
        }
    }
    //Inner class for increase Temp of track
    public class MyUpTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }
    // Inner class for deacrease Tempo of track
    public class MyDownTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }
    // Inner class for clear all check box
    public class MyClearListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            ClearCheckBox(checkBoxList);
        }
    }
    // Method which clean all check boxes in mainPanel
    public void ClearCheckBox(ArrayList<JCheckBox> checkBoxList){
        for (JCheckBox check : checkBoxList){
            check.setSelected(false);
        }
    }

    // Метод создает события для одного инструмента за каждый проход цикла для всех 16 тактов.
    // Можно получить int[] дял Bass drum, и каждый элемент массива будет содержать либо клавишу этого инструмента, либо ноль.
    // Если это ноль, то инструмент не должен играть на текущем такте. Иначе нужно создать событие и добавить его в дорожку.
    public void makeTracks(int[] list){
        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0){
                track.add(makeEvent(144, 9, key, 100, i)); // Создаем события включения и выключения
                track.add(makeEvent(128, 9, key, 100, i + 1)); // и добавляем их в дорожку.
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch(Exception ex){ex.printStackTrace();}
        return event;
    }

    // Сериализация схемы флажков
    public class MySendListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            // Булев массив для хранения состояния каждого флажка.
            boolean[] checkboxState = new boolean[256];
            // Пробегаем через checkbosList (ArrayList содержащий состояния флажков),
            // считываем состояния и добавляем полученные значения в булев массив
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                if (check.isSelected()){
                    checkboxState[i] = true;
                }
            }
            try {
                JFileChooser fileSave = new JFileChooser();
                fileSave.showSaveDialog(theFrame);
                FileOutputStream fileStream = new FileOutputStream(fileSave.getSelectedFile());
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkboxState);
            } catch(Exception ex){ex.printStackTrace();}
        }
    }
    // Восстановление схемы
    public class MyReadInListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            boolean[] checkboxState = null;
            try {
                JFileChooser fileOpen = new JFileChooser();
                fileOpen.showOpenDialog(theFrame);
                FileInputStream fileIn = new FileInputStream(fileOpen.getSelectedFile());
                ObjectInputStream is = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) is.readObject();
            } catch(Exception ex){ex.printStackTrace();}
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkBoxList.get(i);
                if (checkboxState[i]){
                    check.setSelected(true);
                } else {
                    check.setSelected(false);
                }
            }
            // Останавливаем проигрываение мелодии и восстанавливаем последовательность
            // используя новые состояния флажков в ArrayList
            sequencer.stop();
            buildTrackAndStart();
        }
    }
}
