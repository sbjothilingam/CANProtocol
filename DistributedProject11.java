/*
 * @author:     Suresh Babu Jothilingam
 */
import java.io.*;
import java.net.InetAddress;
import java.net.*;
import java.util.*;

/*
 *  This class is for the Peer which provides functionalities for the peer to join the CAN, search a file, insert a file, 
 *  view the details of the current peer and leave the CAN
 */
public class DistributedProject11 extends Thread {
    String bootStrapIp="";
    Node curr;
    double initialx1 = 0;
    double initialy1 = 0;
    double initialx2 = 10;
    double initialy2 = 10;
    boolean found;

    public DistributedProject11() throws Exception {
        curr = new Node();
        found = false;
    }
    //to get the x coordinate of the input string
    double getX(String fileName){
        double x=0;
        for(int i=1;i<fileName.length();i=i+2){
            x+=(double)fileName.charAt(i);
        }
        return x%10;
    }
    //to get the y coordinate of the given string
    double getY(String fileName){
        double y=0;
        for(int i=0;i<fileName.length();i=i+2){
            y+=(double)fileName.charAt(i);
        }
        return y%10;
    }
    //inform new neighbours to add this peer to its neighbours list
    public void informNeighbours(){
        for(int i=0;i<curr.neiNode.size();i++){
            try{
                Socket sock = new Socket(curr.neiNode.get(i).ipAddress, 1771);
                BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                pw.println("add");
                pw.println(curr.ipAddress);
                pw.println(String.valueOf(curr.x1));
                pw.println(String.valueOf(curr.y1));
                pw.println(String.valueOf(curr.x2));
                pw.println(String.valueOf(curr.y2));
                double x1=Double.parseDouble(br.readLine());
                double y1=Double.parseDouble(br.readLine());
                double x2=Double.parseDouble(br.readLine());
                double y2=Double.parseDouble(br.readLine());
                curr.neiNode.get(i).updateZone(x1, y1, x2, y2);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    //it check if the peer is already in the neighbours list
    boolean isAlreadyNei(String ip){
        boolean check=false;
        for(int i=0;i<curr.neiNode.size();i++){
            if(curr.neiNode.get(i).ipAddress.equals(ip)){
                check=true;
            }
        }
        if(check)
            return true;
        else
            return false;
    }
    //joining CAN
    public void joinNode() {
        try {
            //contacts boot strap
            Socket sock = new Socket(bootStrapIp, 1789);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("getnode");
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            curr.ipAddress=br.readLine();
            String randIp = br.readLine();
            if (randIp.equals("null")) {
                curr.updateZone(initialx1, initialy1, initialx2, initialy2);
                System.out.println("Successfully joined");
            } 
            //this part gets executed when the bootstrap server returns an IP address
            else {
                double x = Math.random() * 10;
                double y = Math.random() * 10;
                Socket sock1 = new Socket(randIp, 1771);
                PrintWriter print = new PrintWriter(sock1.getOutputStream(), true);
                print.println("requestjoin");
                print.println(curr.ipAddress);
                print.println(String.valueOf(x));
                print.println(String.valueOf(y));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //to insert a file key word of the file is given as input
    public void insert(String keyword) {
        boolean found=false;
        boolean leftFound=false;
        int index=0;
        double x=getX(keyword);
        double y=getY(keyword);
        if(curr.isPresent(x, y)){
            curr.updateFile(keyword);
            System.out.println("File is inserted in "+curr.ipAddress);
        }
        else{
            try{
                    String ip=curr.nearestN(x, y);
                    Socket sock=new Socket(ip,1771);
                    PrintWriter pw=new PrintWriter(sock.getOutputStream(),true);
                    pw.println("requestinsert");
                    pw.println(curr.ipAddress);
                    pw.println(String.valueOf(x));
                    pw.println(String.valueOf(y));
                    pw.println(keyword);
  
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        
    }
    //search the location of the file and display the path taken to reach the destination
    public void search(String keyword) {
        boolean found=false;
        boolean leftFound=false;
        int index=0;
        double x=getX(keyword);
        double y=getY(keyword);
         //checks if the coordinates lie within the zone of the peers which left
        if(curr.isPresent(x, y)){
            if(curr.fileNames.contains(keyword)){
                System.out.println("File is present in "+curr.ipAddress);
            }
            else{
                System.out.println("Specified file is not present in the peer");
            }
        }
        else{
            //gets the nearest neighbour to that file
            String ip=curr.nearestN(x, y);
            String path=curr.ipAddress.concat("-->");
            try{
                    Socket sock=new Socket(ip,1771);
                    PrintWriter pw=new PrintWriter(sock.getOutputStream(),true);
                    pw.println("requestsearch");
                    pw.println(curr.ipAddress);
                    pw.println(path);
                    pw.println(String.valueOf(x));
                    pw.println(String.valueOf(y));
                    pw.println(keyword);
                   
            }
            catch(Exception e){
               e.printStackTrace();
            }
        }
    }
    //view the details of the current peer
    public void view() {
        //PEER DETAILS
        System.out.println("IP "+curr.ipAddress);
        System.out.println("Zone "+curr.x1+" "+curr.y1+" "+curr.x2+" "+curr.y2);
        System.out.println("FILES");
        for(int i=0;i<curr.fileNames.size();i++)
            System.out.println(curr.fileNames.get(i));
        if(!curr.nodesLeft.isEmpty()){
            for(int i=0;i<curr.nodesLeft.size();i++){
                for(int j=0;j<curr.nodesLeft.get(i).fileNames.size();j++){
                    System.out.println(curr.nodesLeft.get(i).fileNames.get(j));
                }
            }
        }
        System.out.println("NEIGHBOURS");
        for(int i=0;i<curr.neiNode.size();i++)
            System.out.println(curr.neiNode.get(i).ipAddress);
    }
    //inform that the current peer's neighbours that the current peer is leaving CAN
    public void informLeave(){
        if(!curr.neiNode.isEmpty()){
            for(int i=0;i<curr.neiNode.size();i++){
                try{
                    Socket sock = new Socket(curr.neiNode.get(i).getIP(), 1771);
                    PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                    pw.println("leave");
                    pw.println(curr.ipAddress);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("LEFT FROM CAN");
        }
    }
    //to delete the entry of the current peer from boot strap
    public void deleteEntryFromBootStrap(){
        try{
            Socket sock = new Socket(bootStrapIp, 1789);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("bye");
            pw.println(curr.ipAddress);
            System.out.println("Entry deleted from BootStrap");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //to transfer the entire zone to its neighbour when it cannot form a rectangle or square when it leaves
    public void sendTransferZoneRequest(){
        try{
            for(int j=0;j<curr.neiNode.size();j++){
                Socket sock = new Socket(curr.neiNode.get(j).ipAddress, 1771);
                PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                pw.println("transferzonerequest");
                pw.println(curr.ipAddress);
                pw.println(String.valueOf(curr.x1));
                pw.println(String.valueOf(curr.y1));
                pw.println(String.valueOf(curr.x2));
                pw.println(String.valueOf(curr.y2));
                if(!curr.fileNames.isEmpty()){
                        pw.println("files");
                        for(int i=0;i<curr.fileNames.size();i++){
                            pw.println(curr.fileNames.get(i));
                        }
                        pw.println("end");
                    }
                    else{
                        pw.println("nope");
                    }
                    if(!curr.neiNode.isEmpty()){
                        pw.println("neighbours");
                        for(int i=0;i<curr.neiNode.size();i++){
                            pw.println(curr.neiNode.get(i).ipAddress);
                        }
                        pw.println("end");
                    }
                    else{
                        pw.println("nope");
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
    }
    //to leave the CAN
    public void leave() {
        try{
            String ip=curr.nearestNLeave();
            if(!ip.equals("Nope")){
                System.out.println(ip);
                Socket sock = new Socket(ip, 1771);
                PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                pw.println("transferrequest");
                pw.println(String.valueOf(curr.x1));
                pw.println(String.valueOf(curr.y1));
                pw.println(String.valueOf(curr.x2));
                pw.println(String.valueOf(curr.y2));
                if(!curr.fileNames.isEmpty()){
                    pw.println("files");
                    for(int i=0;i<curr.fileNames.size();i++){
                        pw.println(curr.fileNames.get(i));
                    }
                    pw.println("end");
                }
                else{
                    pw.println("nope");
                }
                if(!curr.neiNode.isEmpty()){
                    pw.println("neighbours");
                    for(int i=0;i<curr.neiNode.size();i++){
                        pw.println(curr.neiNode.get(i).ipAddress);
                    }
                    pw.println("end");
                }
                else{
                    pw.println("nope");
                }
            }
            else{
                sendTransferZoneRequest();
            }
            informLeave();
            deleteEntryFromBootStrap();
            curr.fileNames.clear();
            curr.neiNode.clear();
            curr.nodesLeft.clear();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
    }
    //checks if the current zone is in square shape to split vertically
    public boolean isSquare(double x1, double y1, double x2, double y2) {
        double x = 0, y = 0;
        x = x1 - x2;
        y = y1 - y2;
        if (x < 0) {
            x = -1 * x;
        }
        if (y < 0) {
            y = -1 * y;
        }
        if (x == y) {
            return true;
        } else {
            return false;
        }
    }
    //this method is called to update the neighbours about the current zone coordinates 
    void updateNeighbours(){
        if(!curr.neiNode.isEmpty()){
            for(int i=0;i<curr.neiNode.size();i++){
                try{
                    Socket sock = new Socket(curr.neiNode.get(i).getIP(), 1771);
                    PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                    pw.println("update");
                    pw.println(curr.ipAddress);
                    pw.println(curr.x1);
                    pw.println(curr.y1);
                    pw.println(curr.x2);
                    pw.println(curr.y2);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    //CURRENT NODE REMOVES ITSELF FROM OLD NEIGHBOURS
    public void delete(ArrayList<Node> sendNei){
        for(int i=0;i<sendNei.size();i++){
            try{
                Socket sock = new Socket(sendNei.get(i).ipAddress, 1771);
                PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                pw.println("delete");
                pw.println(curr.ipAddress);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    //contact neighbour node if the random coordinates id not within its zone
    public void contactNeighbour(String recIp,String ip,double x,double y){
        try{
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("requestjoin");
            pw.println(recIp);
            pw.println(String.valueOf(x));
            pw.println(String.valueOf(y));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //if the random coordinates fall within the zone of the peer left it transfer the zone to the new peer
    public void acceptJoinSendPeerLeft(String ip,int indexx){
        try {
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("leftfound");
            pw.println(String.valueOf(curr.nodesLeft.get(indexx).x1));
            pw.println(String.valueOf(curr.nodesLeft.get(indexx).y1));
            pw.println(String.valueOf(curr.nodesLeft.get(indexx).x2));
            pw.println(String.valueOf(curr.nodesLeft.get(indexx).y2));
            if (!curr.nodesLeft.get(indexx).fileNames.isEmpty()) {
                pw.println("files");
                for (int i = 0; i < curr.nodesLeft.get(indexx).fileNames.size(); i++) {
                    pw.println(curr.nodesLeft.get(indexx).fileNames.get(i));
                }
                pw.println("end");
            } else {
                pw.println("no files");
            }
            if (!curr.nodesLeft.get(indexx).neiNode.isEmpty()) {
                pw.println("neighbours");
                for (int i = 0; i < curr.nodesLeft.get(indexx).neiNode.size(); i++) {
                    pw.println(curr.nodesLeft.get(indexx).neiNode.get(i).ipAddress);
                    pw.println(curr.nodesLeft.get(indexx).neiNode.get(i).x1);
                    pw.println(curr.nodesLeft.get(indexx).neiNode.get(i).y1);
                    pw.println(curr.nodesLeft.get(indexx).neiNode.get(i).x2);
                    pw.println(curr.nodesLeft.get(indexx).neiNode.get(i).y2);

                }
                pw.println("end");
            } else {
                pw.println("no nei");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //accepts join request if the random coordinates fall within current peers zone
    public void acceptJoinRequest(String ip){
        try{
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("found");
            if (isSquare(curr.x1, curr.y1, curr.x2, curr.y2)) {
                double mid = (curr.x2 + curr.x1) / 2;
                pw.println(String.valueOf(mid));
                pw.println(String.valueOf(curr.y1));
                pw.println(String.valueOf(curr.x2));
                pw.println(String.valueOf(curr.y2));
                //addds the peer as its neighbour
                Node neiNode = new Node();
                neiNode.ipAddress = ip;
                neiNode.updateZone(mid, curr.y1, curr.x2, curr.y2);
                curr.updateNei(neiNode);
                //updates its current zone after split
                curr.updateZone(curr.x1, curr.y1, mid, curr.y2);
                //sends the current zone details
                pw.println(curr.ipAddress);
                pw.println(String.valueOf(curr.x1));
                pw.println(String.valueOf(curr.y1));
                pw.println(String.valueOf(curr.x2));
                pw.println(String.valueOf(curr.y2));
                /*
                 * TRYING TO COMPUTE THE NEIGHBOURS TO BE SENT
                 */
                ArrayList<Node> nodesToBeSent = new ArrayList<Node>();
                ArrayList<String> filesToBeSent=new ArrayList<String>();
                for (int i = 0; i < curr.neiNode.size(); i++) {
                    if (!curr.neiNode.get(i).ipAddress.equals(neiNode.ipAddress)) {
                        if (curr.neiNode.get(i).x1 == neiNode.x2) {
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                        if((curr.neiNode.get(i).x1==curr.x2) &&(curr.neiNode.get(i).y1==curr.y2)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                        if((curr.neiNode.get(i).x2==curr.x1)&&(curr.neiNode.get(i).y1==curr.y2)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                        if((curr.neiNode.get(i).x2==curr.x1)&&(curr.neiNode.get(i).y2==curr.y1)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                        if((curr.neiNode.get(i).x1==curr.x2)&&(curr.neiNode.get(i).y2==curr.y1)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                        
                    }
                }
                delete(nodesToBeSent);
                for (int i = 0; i < curr.neiNode.size(); i++) {
                    if (!curr.neiNode.get(i).ipAddress.equals(neiNode.ipAddress)) {
                        nodesToBeSent.add(curr.neiNode.get(i));
                    }
                }
                for (int i = 0; i < nodesToBeSent.size(); i++) {
                    if(nodesToBeSent.get(i).x2==curr.x1){
                        nodesToBeSent.remove(i);
                        break;
                    }
                   if((nodesToBeSent.get(i).x2==neiNode.x1 ) &&(nodesToBeSent.get(i).y2==neiNode.y1)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                   if((nodesToBeSent.get(i).x1==neiNode.x2 ) &&(nodesToBeSent.get(i).y1==neiNode.y2)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                   if((nodesToBeSent.get(i).x2==neiNode.x1 ) &&(nodesToBeSent.get(i).y1==neiNode.y2)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                   if((nodesToBeSent.get(i).x1==neiNode.x2 ) &&(nodesToBeSent.get(i).y2==neiNode.y1)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                }
                //sending this zones neighbours
                if (!nodesToBeSent.isEmpty()) {
                    pw.println("neighbours");
                    for (int i = 0; i < nodesToBeSent.size(); i++) {
                        pw.println(nodesToBeSent.get(i).ipAddress);
                        pw.println(String.valueOf(nodesToBeSent.get(i).x1));
                        pw.println(String.valueOf(nodesToBeSent.get(i).y1));
                        pw.println(String.valueOf(nodesToBeSent.get(i).x2));
                        pw.println(String.valueOf(nodesToBeSent.get(i).y2));
                    }
                    pw.println("end");
                } else {
                    pw.println("no Nei");
                }
                for(int i=0;i<curr.fileNames.size();i++){
                    double x=getX(curr.fileNames.get(i));
                    double y=getY(curr.fileNames.get(i));
                    if(neiNode.isPresent(x, y)){
                        filesToBeSent.add(curr.fileNames.get(i));
                        curr.fileNames.remove(i);
                    }
                }
                if(!filesToBeSent.isEmpty()){
                    pw.println("files");
                    for(int i=0;i<filesToBeSent.size();i++){
                        pw.println(filesToBeSent.get(i));
                    }
                    pw.println("end");
                }
                else{
                    pw.println("no files");
                }
                //updates the current zone neighbours about the zone coordinate change
                updateNeighbours();
            } //this is to split if the zone is rectangular in shape
            else {
                double mid = (curr.y1 + curr.y2) / 2;
                pw.println(String.valueOf(curr.x1));
                pw.println(String.valueOf(mid));
                pw.println(String.valueOf(curr.x2));
                pw.println(String.valueOf(curr.y2));
                Node neiNode = new Node();
                neiNode.ipAddress = ip;
                neiNode.updateZone(curr.x1, mid, curr.x2, curr.y2);
                curr.updateNei(neiNode);
                curr.updateZone(curr.x1, curr.y1, curr.x2, mid);
                pw.println(curr.ipAddress);
                pw.println(String.valueOf(curr.x1));
                pw.println(String.valueOf(curr.y1));
                pw.println(String.valueOf(curr.x2));
                pw.println(String.valueOf(curr.y2));
                //computes the neighbour to send
                ArrayList<Node> nodesToBeSent = new ArrayList<Node>();
                ArrayList<String> filesToBeSent=new ArrayList<String>();
                for (int i = 0; i < curr.neiNode.size(); i++) {
                    if (!curr.neiNode.get(i).ipAddress.equals(neiNode.ipAddress)) {
                        if(curr.neiNode.get(i).y1==neiNode.y2){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                       if((curr.neiNode.get(i).x1==curr.x2) &&(curr.neiNode.get(i).y1==curr.y2)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                       if((curr.neiNode.get(i).x2==curr.x1)&&(curr.neiNode.get(i).y1==curr.y2)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                       if((curr.neiNode.get(i).x2==curr.x1)&&(curr.neiNode.get(i).y2==curr.y1)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                       if((curr.neiNode.get(i).x1==curr.x2)&&(curr.neiNode.get(i).y2==curr.y1)){
                            nodesToBeSent.add(curr.neiNode.get(i));
                            curr.neiNode.remove(i);
                            break;
                        }
                        
                    }
                }
                delete(nodesToBeSent);
                for (int i = 0; i < curr.neiNode.size(); i++) {
                    if (!curr.neiNode.get(i).ipAddress.equals(neiNode.ipAddress)) {
                        nodesToBeSent.add(curr.neiNode.get(i));
                    }
                }
                for (int i = 0; i < nodesToBeSent.size(); i++) {
                    if(nodesToBeSent.get(i).y2==curr.y1){
                        nodesToBeSent.remove(i);
                        break;
                    }
                    if((nodesToBeSent.get(i).x2==neiNode.x1 ) &&(nodesToBeSent.get(i).y2==neiNode.y1)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                    if((nodesToBeSent.get(i).x1==neiNode.x2 ) &&(nodesToBeSent.get(i).y1==neiNode.y2)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                    if((nodesToBeSent.get(i).x2==neiNode.x1 ) &&(nodesToBeSent.get(i).y1==neiNode.y2)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                   if((nodesToBeSent.get(i).x1==neiNode.x2 ) &&(nodesToBeSent.get(i).y2==neiNode.y1)){
                        nodesToBeSent.remove(i);
                        break;
                    }
                }
                //sending this zones neighbours
                if (!nodesToBeSent.isEmpty()) {
                    pw.println("neighbours");
                    for (int i = 0; i < nodesToBeSent.size(); i++) {
                        pw.println(nodesToBeSent.get(i).ipAddress);
                        pw.println(String.valueOf(nodesToBeSent.get(i).x1));
                        pw.println(String.valueOf(nodesToBeSent.get(i).y1));
                        pw.println(String.valueOf(nodesToBeSent.get(i).x2));
                        pw.println(String.valueOf(nodesToBeSent.get(i).y2));
                    }
                    pw.println("end");
                } else {
                    pw.println("no Nei");
                }
                for(int i=0;i<curr.fileNames.size();i++){
                    double x=getX(curr.fileNames.get(i));
                    double y=getY(curr.fileNames.get(i));
                    if(neiNode.isPresent(x, y)){
                        filesToBeSent.add(curr.fileNames.get(i));
                        curr.fileNames.remove(i);
                    }
                }
                if(!filesToBeSent.isEmpty()){
                    pw.println("files");
                    for(int i=0;i<filesToBeSent.size();i++){
                        pw.println(filesToBeSent.get(i));
                    }
                    pw.println("end");
                }
                else{
                    pw.println("no files");
                }
                updateNeighbours();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //re routing the insert request to the neighbour
    public void contactNeighbourInsert(String recIp,String ip,String fileName,double x,double y){
        try{
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("requestinsert");
            pw.println(recIp);
            pw.println(String.valueOf(x));
            pw.println(String.valueOf(y));
            pw.println(fileName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //accepts the request and inserts the file in the current zone
    public void acceptInsertRequest(String ip,String fileName){
        try{
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("foundinsert");
            pw.println(curr.ipAddress);
            curr.updateFile(fileName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //insert the file in the nodes left's table
    public void acceptInsertPeerLeft(String recIp,String fileName,int index){
        try{
            Socket sock = new Socket(recIp, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("foundinsertleft");
            pw.println(curr.ipAddress);
            curr.nodesLeft.get(index).updateFile(fileName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //reroutes the request for search
    public void contactNeighbourSearch(String recIp,String path,String ip,String fname,double x,double y){
        try{
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("requestsearch");
            pw.println(recIp);
            pw.println(path.concat(curr.ipAddress.concat("-->")));
            pw.println(String.valueOf(x));
            pw.println(String.valueOf(y));
            pw.println(fname);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //accepts the request for search and inform the request originating peer
    public void acceptSearchRequest(String ip,String path){
        try{
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("foundsearch");
            pw.println(curr.ipAddress);
            pw.println(path.concat(curr.ipAddress));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public void sendErrorMessage(String ip){
        try{
            Socket sock = new Socket(ip, 1771);
            PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
            pw.println("errorsearch");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    //to inform the the node which has left to its neighbours
    public void informLeftNodeNeighbours(int indexx){
        for(int i=0;i<curr.nodesLeft.get(indexx).neiNode.size();i++){
            try{
                Socket sock = new Socket(curr.nodesLeft.get(indexx).neiNode.get(i).ipAddress, 1771);
                PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);
                pw.println("deleteme");
                pw.println(curr.nodesLeft.get(indexx).x1);
                pw.println(curr.nodesLeft.get(indexx).y1);
                pw.println(curr.nodesLeft.get(indexx).x2);
                pw.println(curr.nodesLeft.get(indexx).y2);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    //server code of the peer
    public void server() {
        (new Thread() {
            public void run() {
                System.out.println("Server started");
                try {
                    ServerSocket sesock = new ServerSocket(1771);
                    while(true){
                    Socket sock2 = sesock.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(sock2.getInputStream()));
                    PrintWriter pw = new PrintWriter(sock2.getOutputStream(), true);
                    String rec = br.readLine();
                    //if it receives join request from a client
                    if (rec.equals("requestjoin")) {
                        String recIp=br.readLine();
                        double x = Double.parseDouble(br.readLine());
                        double y = Double.parseDouble(br.readLine());
                        boolean foundLeft=false;
                        int indexx=0;
                        //to check if the coordinates lie within the node left
                        if(!curr.nodesLeft.isEmpty()){
                            foundLeft=true;
                        }
                        if(foundLeft){
                            acceptJoinSendPeerLeft(recIp,indexx);
                            informLeftNodeNeighbours(indexx);
                            curr.nodesLeft.remove(indexx);
                        }
                        //checks if it lies in the current peer zone
                       else if (curr.isPresent(x, y)) {
                            acceptJoinRequest(recIp);
                        } 
                        else {
                             contactNeighbour(recIp,curr.nearestN(x, y),x,y);
                        }
                    }
                    if(rec.equals("found")){
                        double x1 = Double.parseDouble(br.readLine());
                        double y1 = Double.parseDouble(br.readLine());
                        double x2 = Double.parseDouble(br.readLine());
                        double y2 = Double.parseDouble(br.readLine());
                        String ip=br.readLine();
                        //zone of the peer which spilts
                        double x11 = Double.parseDouble(br.readLine());
                        double y11 = Double.parseDouble(br.readLine());
                        double x22 = Double.parseDouble(br.readLine());
                        double y22 = Double.parseDouble(br.readLine());
                        curr.updateZone(x1, y1, x2, y2);
                        Node n=new Node();
                        n.ipAddress=ip;
                        n.updateZone(x11, y11, x22, y22);
                        curr.updateNei(n);
                        String mssg=br.readLine();
                        if(mssg.equals("neighbours")){
                            String neiIp="";
                            mssg=br.readLine();
                            while(!mssg.equals("end")){
                                neiIp=mssg;
                                double xx = Double.parseDouble(br.readLine());
                                double yy = Double.parseDouble(br.readLine());
                                double xx2 = Double.parseDouble(br.readLine());
                                double yy2 = Double.parseDouble(br.readLine());
                                if(!neiIp.equals(curr.ipAddress) && !isAlreadyNei(neiIp)){
                                    Node neiNode=new Node();
                                    neiNode.ipAddress=neiIp;
                                    neiNode.updateZone(xx, yy, xx2, yy2);
                                    curr.updateNei(neiNode);
                                }
                                mssg=br.readLine();
                            }
                            
                        }
                        mssg=br.readLine();
                        if(mssg.equals("files")){
                            String fileName=br.readLine();
                            while(!fileName.equals("end")){
                                curr.fileNames.add(fileName);
                                fileName=br.readLine();
                            }
                        }
                        informNeighbours();
                        updateNeighbours();
                        System.out.println("Successfully joined");
                    }
                    if(rec.equals("leftfound")){
                        double x1 = Double.parseDouble(br.readLine());
                        double y1 = Double.parseDouble(br.readLine());
                        double x2 = Double.parseDouble(br.readLine());
                        double y2 = Double.parseDouble(br.readLine());
                        curr.updateZone(x1, y1, x2, y2);
                        String mes=br.readLine();
                        if(mes.equals("files")){
                            String file=br.readLine();
                            while(!file.equals("end")){
                                curr.updateFile(file);
                                file=br.readLine();
                            }
                        }
                        mes=br.readLine();
                        if(mes.equals("neighbours")){
                            String ip=br.readLine();
                            while(!ip.equals("end")){
                                double xx1=Double.parseDouble(br.readLine());
                                double yy1=Double.parseDouble(br.readLine());
                                double xx2=Double.parseDouble(br.readLine());
                                double yy2=Double.parseDouble(br.readLine());
                                Node temp=new Node();
                                temp.ipAddress=ip;
                                temp.updateZone(xx1, yy1, xx2, yy2);
                                curr.updateNei(temp);
                                ip=br.readLine();
                            }
                        }
                        informNeighbours();
                        updateNeighbours();
                        System.out.println("Successfully joined");
                    }
                    //when server recieves a insert request
                    if(rec.equals("requestinsert")){
                        boolean found=false;
                        int index=0;
                        String recIp=br.readLine();
                        double x=Double.parseDouble(br.readLine());
                        double y=Double.parseDouble(br.readLine());
                        String fileName=br.readLine();
                        if(!curr.nodesLeft.isEmpty()){
                           for(int i=0;i<curr.nodesLeft.size();i++){
                               if(curr.nodesLeft.get(i).isPresent(x, y)){
                                   found=true;
                                   index=i;
                               }
                           } 
                        }
                        if(found){
                            acceptInsertPeerLeft(recIp,fileName,index);
                        }
                        else if(curr.isPresent(x, y)){
                            acceptInsertRequest(recIp, fileName);
                        }
                        else{
                            contactNeighbourInsert(recIp,curr.nearestN(x, y),fileName,x,y);
                        }
                    }
                    if(rec.equals("foundinsert")){
                        String insertedIp=br.readLine();
                        System.out.println("inserted file in "+insertedIp);
                    }
                    if(rec.equals("foundinsertleft")){
                        String insertedIp=br.readLine();
                        System.out.println("inserted file in "+insertedIp);
                    }
                    //search request
                    if(rec.equals("requestsearch")){
                        boolean found=false;
                        int index=0;
                        String recIp=br.readLine();
                        String path=br.readLine();
                        double x=Double.parseDouble(br.readLine());
                        double y=Double.parseDouble(br.readLine());
                        String fname=br.readLine();
                        if(!curr.nodesLeft.isEmpty()){
                           for(int i=0;i<curr.nodesLeft.size();i++){
                               if(curr.nodesLeft.get(i).checkFile(fname)){
                                   found=true;
                               }
                           } 
                        }
                        if(found){
                            acceptSearchRequest(recIp,path);
                        }
                        else if(curr.isPresent(x, y)){
                            if(curr.checkFile(fname)){
                                acceptSearchRequest(recIp,path);
                            }
                            else{
                                sendErrorMessage(recIp);
                            }
                        }
                        else{
                            contactNeighbourSearch(recIp,path,curr.nearestN(x, y),fname,x,y);
                        }
                    }
                    if(rec.equals("foundsearch")){
                        String foundIp=br.readLine();
                        String path=br.readLine();
                        System.out.println("File is present in "+foundIp);
                        System.out.println("Path "+path);
                    }
                    if(rec.equals("errorsearch")){
                        System.out.println("Specified file is not present in the peer");
                    }
                    if(rec.equals("transferrequest")){
                        double x1=Double.parseDouble(br.readLine());
                        double y1=Double.parseDouble(br.readLine());
                        double x2=Double.parseDouble(br.readLine());
                        double y2=Double.parseDouble(br.readLine());
                        if(((x1==curr.x2)&&(y1==curr.y1))&&((x1==curr.x2)&&(y2==curr.y2))){
                            curr.updateZone(curr.x1, curr.y1, x2, y2);
                        }
                        else if(((x2==curr.x1)&&(y1==curr.y1))&&((x2==curr.x1)&&(y2==curr.y2))){
                            curr.updateZone(x1, y1, curr.x2, curr.y2);
                        }
                        else if(((x1==curr.x1)&&(y1==curr.y2))&&((x2==curr.x2)&&(y1==curr.y2))){
                            curr.updateZone(curr.x1, curr.y1, x2, y2);
                        }
                        else if(((x1==curr.x1)&&(y2==curr.y1))&&((x2==curr.x2)&&(y2==curr.y1))){
                            curr.updateZone(x1, y1, curr.x2,curr.y2);
                        }
                        String msg=br.readLine();
                        if(msg.equals("files")){
                            String fname=br.readLine();
                            while(!fname.equals("end")){
                                curr.updateFile(fname);
                                fname=br.readLine();
                            }
                        }
                        msg=br.readLine();
                        if(msg.equals("neighbours")){
                            String ip=br.readLine();
                            while(!ip.equals("end")){
                                Node temp=new Node();
                                temp.ipAddress=ip;
                                if(!isAlreadyNei(ip)){
                                    if(!curr.ipAddress.equals(ip)){
                                        curr.updateNei(temp);
                                    }
                                }
                                ip=br.readLine();
                            }
                            updateNeighbours();
                            informNeighbours();
                        }
                    }
                    if(rec.equals("transferzonerequest")){
                        String ip=br.readLine();
                        double x1=Double.parseDouble(br.readLine());
                        double y1=Double.parseDouble(br.readLine());
                        double x2=Double.parseDouble(br.readLine());
                        double y2=Double.parseDouble(br.readLine());
                        Node temp=new Node();
                        temp.ipAddress=ip;
                        temp.updateZone(x1, y1, x2, y2);
                        String msg=br.readLine();
                        if(msg.equals("files")){
                            String fname=br.readLine();
                            while(!fname.equals("end")){
                                temp.updateFile(fname);
                                fname=br.readLine();
                            }
                        }
                        msg=br.readLine();
                        if(msg.equals("neighbours")){
                            String ipNei=br.readLine();
                            while(!ipNei.equals("end")){
                                Node tempNei=new Node();
                                tempNei.ipAddress=ipNei;
                                temp.updateNei(tempNei);
                                ipNei=br.readLine();
                            }
                        }
                        curr.nodesLeft.add(temp);
                    }
                    if(rec.equals("deleteme")){
                        double x1=Double.parseDouble(br.readLine());
                        double y1=Double.parseDouble(br.readLine());
                        double x2=Double.parseDouble(br.readLine());
                        double y2=Double.parseDouble(br.readLine());
                        for(int i=0;i<curr.nodesLeft.size();i++){
                            if(curr.nodesLeft.get(i).x1==x1 && curr.nodesLeft.get(i).y1==y1 && curr.nodesLeft.get(i).x2==x2 && curr.nodesLeft.get(i).y2==y2){
                                curr.nodesLeft.remove(i);
                            }
                        }
                    }
                    //update request when the neighbour node has changed its coordinate
                    if(rec.equals("update")){
                        String ip=br.readLine();
                        double x1=Double.parseDouble(br.readLine());
                        double y1=Double.parseDouble(br.readLine());
                        double x2=Double.parseDouble(br.readLine());
                        double y2=Double.parseDouble(br.readLine());
                        for(int i=0;i<curr.neiNode.size();i++){
                            if(curr.neiNode.get(i).getIP().equals(ip)){
                                curr.neiNode.get(i).updateZone(x1, y1, x2, y2);
                            }
                        }
                    }
                    //sends the coordinates of its zone
                    if(rec.equals("getzone")){
                        pw.print(curr.x1);
                        pw.print(curr.y1);
                        pw.print(curr.x2);
                        pw.print(curr.y2);
                    }
                    //delete the received node from its neighbours list
                    if(rec.equals("delete")){
                        String ip=br.readLine();
                        for(int i=0;i<curr.neiNode.size();i++){
                            if(curr.neiNode.get(i).ipAddress.equals(ip)){
                                curr.neiNode.remove(i);
                            }
                        }
                    }
                    //to add the new neighbour
                    if(rec.equals("add")){
                        boolean found=false;
                        String ip=br.readLine();
                        double x1=Double.parseDouble(br.readLine());
                        double y1=Double.parseDouble(br.readLine());
                        double x2=Double.parseDouble(br.readLine());
                        double y2=Double.parseDouble(br.readLine());
                        pw.println(String.valueOf(curr.x1));
                        pw.println(String.valueOf(curr.y1));
                        pw.println(String.valueOf(curr.x2));
                        pw.println(String.valueOf(curr.y2));
                        Node temp=new Node();
                        temp.ipAddress=ip;
                        temp.updateZone(x1, y1, x2, y2);
                        for(int i=0;i<curr.neiNode.size();i++){
                            if(curr.neiNode.get(i).ipAddress.equals(temp.ipAddress)){
                               found=true; 
                            }
                        }
                        if(!found){
                            curr.updateNei(temp);
                        }
                    }
                    if(rec.equals("leave")){
                        String ip=br.readLine();
                        for(int i=0;i<curr.neiNode.size();i++){
                            if(curr.neiNode.get(i).ipAddress.equals(ip)){
                                curr.neiNode.remove(i);
                            }
                        }
                    }
                }
           }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            DistributedProject11 dp1 = new DistributedProject11();
            dp1.server();
            dp1.bootStrapIp=args[0];
            Scanner sc = new Scanner(System.in);
            while (true) {
                String input = sc.next();
                if (input.equals("join")) {
                    dp1.joinNode();
                }
                if(input.equals("insert")){
                    String file=sc.next();
                    dp1.insert(file);
                }
                if(input.equals("search")){
                    String file=sc.next();
                    dp1.search(file);
                }
                if(input.equals("view")){
                    dp1.view();
                }
                if(input.equals("leave")){
                    dp1.leave();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
