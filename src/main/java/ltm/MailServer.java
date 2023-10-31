package ltm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MailServer {
    DatagramSocket serverSocket = null;
    public MailServer(int port) throws Exception{
        serverSocket = new DatagramSocket(port);
        try{
            File folder = new File("users_server");
            if(!folder.exists()) folder.mkdir();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void listen(){
        new Thread(()->{
            byte[] receiveData = new byte[1024];
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(receiveData, 1024);
                try{
                    serverSocket.receive(receivePacket);
                    onPacket(receivePacket);
                    receiveData = new byte[1024];
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void onPacket(DatagramPacket packet){
        if(packet == null) return;
        String data = new String(packet.getData());
        if(data.length() > 0) System.out.println("Packet: " + data);
        if(data.startsWith("REG:")){
            String username = data.substring(4).trim();
            System.out.println("Register: " + username);
            File folder = new File("users_server/" + username);
            if(!folder.exists()) folder.mkdir();
            File file = new File("users_server/" + username + "/new_email.txt");
            try {
                file.createNewFile();
                DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
                out.write("Thank you for using this service. we hope that you will feel comfortabl........".getBytes());
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(data.startsWith("SEND:")){
            String[] parts = data.split(":");
            String username = parts[1];
            String content = parts[2];
            System.out.println("Send to " + username + ": " + content);
            try{

                File folder = new File("users_server/" + username);
                if(!folder.exists()) throw new Exception("User not found");

                String mailName = "/mail" + System.currentTimeMillis() + ".txt";
                File file = new File("users_server/" + username + mailName);
                if(!file.exists()) file.createNewFile();
                DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
                out.write(content.getBytes());
                out.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(data.startsWith("LOGIN:")){
            String[] parts = data.split(":");
            String username = parts[1].trim();
            System.out.println("LOGIN : " + username);
            try{
                File folder = new File("users_server/" + username);
                File[] files = folder.listFiles();
                for(File file : files){
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    String content = new String(in.readAllBytes());
                    in.close();
                    String send = "MAIL:" + file.getName() + ":" + content;
                    System.out.println("Send: " + send);
                    sendPacket(send.getBytes(), packet.getAddress(), packet.getPort());
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public void sendPacket(byte[] data, InetAddress address, int port) throws Exception{
        serverSocket.send(new DatagramPacket(data, data.length, address, port));
    }
    public static void main(String[] args) throws Exception{
        MailServer server = new MailServer(25565);
        server.listen();

        // fake client
        // String packetSend1 = "REG:wtf";
        // server.onPacket(new DatagramPacket(packetSend1.getBytes(), packetSend1.getBytes().length));
        // String packetSend2 = "SEND:nguyenvana:Hello";
        // server.onPacket(new DatagramPacket(packetSend2.getBytes(), packetSend2.getBytes().length));
        // String packetSend3 = "LOGIN:nguyenvana";
        // server.onPacket(new DatagramPacket(packetSend3.getBytes(), packetSend3.getBytes().length));

    }
}
