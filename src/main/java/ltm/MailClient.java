package ltm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MailClient extends JFrame{
    JTextField username;
    JButton loginButton;
    JButton regiButton;
    JTextArea mailContent;
    JPanel mailList;
    DatagramSocket clientSocket = null;

    public MailClient(){
        super("Client");
        setSize(600, 300);
        JPanel loginPanel = new JPanel(new GridLayout(1, 3));
        username = new JTextField();
        loginButton = new JButton("Login");
        regiButton = new JButton("Register");
        loginPanel.add(username);
        loginPanel.add(loginButton);
        loginPanel.add(regiButton);

        JPanel mailPanel = new JPanel(new GridLayout(1, 2));
        mailContent = new JTextArea();
        mailList = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mailPanel.add(mailList);
        mailPanel.add(mailContent, BorderLayout.CENTER);

        JPanel sendJPanel = new JPanel(new GridLayout(1, 2));
        JTextField sendTo = new JTextField();
        JButton sendButton = new JButton("Send");
        sendJPanel.add(sendTo);
        sendJPanel.add(sendButton);
        mailPanel.add(sendJPanel, BorderLayout.SOUTH);
        
        add(loginPanel, BorderLayout.NORTH);
        add(mailPanel, BorderLayout.CENTER);
        add(sendJPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e->{
            String user = username.getText().trim();
            if(user.length() == 0) return;
            sendPacket("LOGIN:" + user);
            mailList.removeAll();
        });
        regiButton.addActionListener(e->{
            String user = username.getText().trim();
            if(user.length() == 0) return;
            sendPacket("REG:" + user);
        });
        sendButton.addActionListener(e->{
            String user = sendTo.getText().trim();
            String content = mailContent.getText().trim();
            if(user.length() == 0 || content.length() == 0) return;
            mailContent.setText("");
            sendTo.setText("");
            sendPacket("SEND:" + user + ":" + content);
        });
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);

        try {
            File folder = new File("users_client");
            if(!folder.exists()) folder.mkdir();
            clientSocket = new DatagramSocket();
            listen();
        } catch (Exception e) {
            System.exit(0);
        }
        
    }
    public void listen(){
        new Thread(()->{
            byte[] receiveData = new byte[1024];
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(receiveData, 1024);
                try{
                    clientSocket.receive(receivePacket);
                    onPacket(receivePacket);
                    receiveData = new byte[1024];
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void onPacket(DatagramPacket packet){
        String data = new String(packet.getData());
        if(data.startsWith("MAIL:")){
            System.out.println("Packet: " + data);
            String[] parts = data.split(":");
            String filename = parts[1];
            String content = parts[2];
            JButton button = new JButton(filename);
            button.addActionListener(e->{
                mailContent.setText(content);
            });
            mailList.add(button);
            mailList.revalidate();
            mailList.repaint();
        }
    }
    public void sendPacket(String data){
        try {
            clientSocket.send(new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName("103.176.110.169"), 25565));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new MailClient();
    }
}
