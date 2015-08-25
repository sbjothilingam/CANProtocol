/*
 * @author:     Suresh Babu Jothilingam
 */
import java.util.*;

/*
 * This class is to store the details of the Peer with its coordinates, files, neighbours list etc
 */
public class Node {
     ArrayList<String> fileNames=new ArrayList<String>();
     ArrayList<Node> neiNode=new ArrayList<Node>();
     ArrayList<Node> nodesLeft=new ArrayList<Node>();
     double x1,y1;
     double x2,y2;
     double midpointx;
     double midpointy;
     String ipAddress;
    public Node(){
    }
    //to update the current zone and caluclates its midpoint
    public void updateZone(double x1,double y1,double x2,double y2){
        this.x1=x1;
        this.y1=y1;
        this.x2=x2;
        this.y2=y2;
        double x=(x1+x2)/2;
        double y=(y1+y2)/2;
        updateMid(x,y);
    }
    //to update the mid point
    public void updateMid(double x,double y){
        this.midpointx=x;
        this.midpointy=y;
    }
    //adds a new file
    public void updateFile(String fileName){
        this.fileNames.add(fileName);
    }
    public String getIP(){
        return this.ipAddress;
    }
    //checks if the file is present
    public boolean checkFile(String fname){
        return fileNames.contains(fname);
    }
    public boolean isPresent(double x,double y){
        if((x>=this.x1 && x<=this.x2) && (y>=this.y1 && y<=this.y2))
            return true;
        else
            return false;
    }
    //adds a new neighbour
    public void updateNei(Node n){
        this.neiNode.add(n);
    }
    //finds the nearest neighbour to which the request has to re-routed to leave
    public String nearestNLeave(){
      String ip="";
      boolean found=false;
      int index=0;
      while(!found){
          if(index<neiNode.size()){
              if((((x2==neiNode.get(index).x1)&&(y1==neiNode.get(index).y1))&&((x2==neiNode.get(index).x1)&&(y2==neiNode.get(index).y2))) || (((x1==neiNode.get(index).x2)&&(y1==neiNode.get(index).y1))&&((x1==neiNode.get(index).x2)&&(y2==neiNode.get(index).y2)))){
                  found=true;
                  ip=neiNode.get(index).ipAddress;
              }
              else if((((x1==neiNode.get(index).x1)&&(y2==neiNode.get(index).y1))&&((x2==neiNode.get(index).x2)&&(y2==neiNode.get(index).y1))) || (((x1==neiNode.get(index).x1)&&(y1==neiNode.get(index).y2))&&((x2==neiNode.get(index).x2)&&(y1==neiNode.get(index).y2)))){
                 found=true;
                 ip=neiNode.get(index).ipAddress; 
              }
              else{
                  index++;
              }
          }
          else{
              ip="Nope";
              found=true;
          }
      }
      return ip;
    }
    //finds the neares neighbours which is closer to the random coordinates
    public String nearestN(double x,double y){
        String ip="";
        for(int i=0;i<neiNode.size();i++){
            if(neiNode.get(i).isPresent(x, y)){
                ip=neiNode.get(i).ipAddress;
            }
        }
        if(ip.equals("")){
            Node n=neiNode.get(0);
            double dist=Math.sqrt(Math.pow((n.midpointx-x), 2)+Math.pow((n.midpointy-y),2 ));
            ip=n.getIP();
            if(neiNode.size()>0){
            for(int i=1;i<neiNode.size();i++){
                Node nn=neiNode.get(i);
                double ans=Math.sqrt(Math.pow((nn.midpointx-x), 2)+Math.pow((nn.midpointy-y),2 ));
                if(ans<dist){
                    dist=ans;
                    ip=nn.getIP();
                }
                if(ans==dist){
                    ip=nn.getIP();
                }
            }
            return ip;
            }
          
          else{
              return ip;
          }
        }
        return ip;
    }
}
