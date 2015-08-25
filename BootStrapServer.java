/*
 * @author:     Suresh Babu Jothilingam
 */
import java.io.*;
import java.net.*;
import java.util.*;
/*
 * This class is the BootStrapServer class which maintains the list of peers which are currently in the CAN and add the new
 * Peer by adding its entry and giving a random ip address of the Peer which is already in the CAN
 */
public class BootStrapServer{
    static ArrayList<String> nodesIP=new ArrayList<String>();
    String ip="";
    public BootStrapServer(){
    }
    public void insertNode(String ip) {
        nodesIP.add(ip);
    }
    //to get a random node
    public String getNode(){
        if(!nodesIP.isEmpty()){
            double randomNode=Math.random()*nodesIP.size();
            insertNode(ip);
            ip=nodesIP.get((int)randomNode);
        }
        else{
            insertNode(ip);
            ip="null";
        }
        return ip;
    }
    //delete the entry when the node leaves
    public void leave(String ip){
        for(int i=0;i<nodesIP.size();i++){
            if(nodesIP.get(i).equals(ip)){
                nodesIP.remove(i);
            }
        }
    }
    public static void main(String arg[]){
        try{
            BootStrapServer bss=new BootStrapServer();
            ServerSocket sock=new ServerSocket(1789);
            System.out.println("BOOT STRAP SERVER STARTED "+InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
            while(true){
            Socket so=sock.accept();
            BufferedReader br=new BufferedReader(new InputStreamReader(so.getInputStream()));
            String received=br.readLine();
                if(received.equals("getnode")){
                    String ip=so.getInetAddress().getHostAddress();
                    bss.ip=ip;
                    System.out.println("Node ip "+bss.ip);
                    PrintWriter print=new PrintWriter(so.getOutputStream(),true);
                    print.println(ip);
                    print.println(bss.getNode());
                }
                if(received.equals("bye")){
                    String ip=br.readLine();
                    bss.leave(ip);
                }
        }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
