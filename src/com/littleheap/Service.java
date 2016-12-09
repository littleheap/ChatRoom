package com.littleheap;

import javax.swing.*;
import java.net.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

class Service extends JFrame{
	ArrayList<ChatThread> users = new ArrayList<ChatThread>();
	String userList = "";
	String username="";
	String choice;
	String userList_send = "";
	int temp01 = 0;
	JComboBox jcb = new JComboBox();
	JTextField jtf = new JTextField();
	JTextArea jta = new JTextArea();
	JButton jbt = new JButton();
	Service() throws Exception{
		//配置主界面
		getContentPane().setForeground(Color.MAGENTA);
		getContentPane().setBackground(Color.WHITE);
		this.setSize(350,460);
		this.setResizable(false);
		this.setVisible(true);
		this.setTitle("服务器");
		setLocationRelativeTo(null); 
		getContentPane().setLayout(null);
		jtf = new JTextField();
		jtf.setFont(new Font("楷体", Font.PLAIN, 22));
		jtf.setBounds(0, 383, 252, 37);
		getContentPane().add(jtf);
		jtf.setColumns(10);
		jbt = new JButton("\u4E0B\u7EBF");
		jbt.setFont(new Font("幼圆", Font.PLAIN, 17));
		jbt.setForeground(Color.BLUE);
		jbt.setBackground(new Color(204, 255, 102));
		jbt.setBounds(254, 383, 90, 37);
		getContentPane().add(jbt);			
		jta = new JTextArea();
		jta.setFont(new Font("Monospaced", Font.PLAIN, 17));
		jta.setBackground(new Color(255, 255, 255));
		jta.setBounds(0, 32, 344, 345);
		jta.setBackground(new Color(51, 204, 204));
		getContentPane().add(jta);				
		jcb = new JComboBox();
		jcb.setBounds(0, 0, 344, 29);
		getContentPane().add(jcb);
		//下线按钮配置
		jbt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i=0;i<users.size();i++){
					ChatThread ct = (ChatThread)users.get(i);
					if(ct.chatuser.equals(choice)){
						try {
							ct.ps.println("LOGOUT"+"#"+choice);
							users.remove(ct);
							ct.s.close();
							//更新列表
							userList_send = "";
							for(int e=0;e<users.size();e++){
								ChatThread temp = (ChatThread)users.get(e);
								if(temp.chatuser != null)
									userList_send = userList_send +temp.chatuser +"#";
							}												
							String[] strs = userList_send.split("#");
							jcb.removeAllItems();
							for(int ii=0;ii<strs.length;ii++){
								jcb.addItem(strs[ii]);
							}	
							for(int q=0;q<users.size();q++){
								ChatThread temp = (ChatThread)users.get(q);
								temp.ps.println("LOGIN#"+userList_send);
							}
						}	catch (IOException e) {}
						ct.stop();
					}
				}			
			}
		});		
		//群发消息输入框配置
		jtf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				for(int i=0;i<users.size();i++){
					ChatThread ct = (ChatThread)users.get(i);
					ct.ps.println("系统"+"#"+jtf.getText());
				}
				jta.append("已群发系统消息："+jtf.getText());
				jtf.setText("");
			}
		});
		//准备线程接收新客户端
		new Thread(new Runnable() {
			public void run() {
				try{
					ServerSocket ss = new ServerSocket(9999);
					while(true){
						Socket s = ss.accept();
//															sockets.add(s);
						ChatThread ct = new ChatThread(s);
						ct.start(); 
					}
				}catch(Exception ex){}
			}
		}).start();
		//实时获取选择
		new Thread(new Runnable() {
			public void run() {
				while(true){
					choice = (String) jcb.getSelectedItem();
					try{ Thread.sleep(1000); }catch( Exception e){}
				}
			}
		}).start();
}//构	造函数结束	
	
				/////////////////////////////////接收文件//////////////////////////////////
public void receiveFile(String filename) throws IOException { 
	System.out.println("服务器开始接收数据...");   
//			    	String name = filename;
	ServerSocket new_ss= new ServerSocket(6666);
	Socket new_socket = new_ss.accept();
	byte[] inputByte = null;  
	int length = 0;  
	DataInputStream dis = null;  
	FileOutputStream fos = null;  
	String filePath = "D:/服务器数据库/"+filename;
	try {  
		dis = new DataInputStream(new_socket.getInputStream());  
		fos = new FileOutputStream(new File(filePath));      
		inputByte = new byte[1024];     
		while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {  
			fos.write(inputByte, 0, length);  
			fos.flush();      
		}  
		System.out.println("完成接收："+filePath);  
	}   catch (Exception e) {  e.printStackTrace();  } 
	if (fos != null)     fos.close();  
	if (dis != null)  	 dis.close();  
	new_socket.close();
	new_ss.close();
}  
///////////////////////////////发送文件////////////////////////////////
public void send_File(String file_name) throws Exception{
	System.out.print("服务器准备发送文件");
	ServerSocket new_ss= new ServerSocket(6666);
	Socket  new_s = new_ss.accept();
	File file = new File("D:/服务器数据库/"+file_name); //要传输的文件路径  
	long file_length = file.length();
	DataOutputStream dos = new DataOutputStream(new_s.getOutputStream());
	FileInputStream fis = new FileInputStream(file);
	byte[] sendBytes = new byte[1024];
	int length = 0;
	int sumL = 0;
	while ((length  = fis.read(sendBytes, 0, sendBytes.length)) > 0) {  
		sumL += length;    
		System.out.println("已传输："+((sumL/file_length)*100)+"%");  
		dos.write(sendBytes, 0, length);  
		dos.flush();  
	} 
	if(dos!=null) 	dos.close();
	if(fis!=null) 	fis.close();
	new_s.close();
	new_ss.close();
}			
class ChatThread extends Thread{
	Socket s;
	String chatuser;
	BufferedReader br;
	PrintStream ps;
	ChatThread(Socket s) throws Exception{
		users.add(this);
		this.s = s;
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		ps = new PrintStream(s.getOutputStream());
	}
	public void run(){ 
		while(true){
			try{
				String massage = br.readLine();
				if(massage.startsWith("下线#")){
					String[] strs_name= massage.split("#");
					for(int i=0;i<users.size();i++){
						ChatThread ct_special = (ChatThread)users.get(i);
						if(ct_special.chatuser.equals(strs_name[1])) {
							users.remove((ChatThread)ct_special);
							ct_special.s.close();
							userList_send = "";
							for(int i1=0;i1<users.size();i1++){
								ChatThread temp = (ChatThread)users.get(i1);
								if(temp.chatuser != null)
									userList_send = userList_send +temp.chatuser +"#";
							}
							String[] strs = userList_send.split("#");
							jcb.removeAllItems();
							for(int i2=0;i2<strs.length;i2++){
								jcb.addItem(strs[i2]);
							}	
							//客户端更新列表
							//获取列表全部名称
							for(int i3=0;i3<users.size();i3++){
								ChatThread ct = (ChatThread)users.get(i3);
								ct.ps.println("LOGIN#"+userList_send);
							}													
//															break;
							ct_special.stop();
						}
					}
					//服务器更新列表
				}else if(massage.startsWith("LOGIN#")){
					String[] strs = massage.split("#");
					jcb.addItem(strs[1]);
					//给新对象线程命名
					this.chatuser = strs[1];					
					//获取列表全部名称
					userList_send = "";
					for(int i=0;i<users.size();i++){
						ChatThread temp = (ChatThread)users.get(i);
						userList_send = userList_send +temp.chatuser +"#";
					}			
					//遍历发送列表更新
					for(int i=0;i<users.size();i++){
						ChatThread ct = (ChatThread)users.get(i);
						ct.ps.println("LOGIN#"+userList_send);
					}
				}else if(massage.startsWith("传送文件#")){
					String[] strs_temp = massage.split("#");
					receiveFile(strs_temp[3]);							
					System.out.println(strs_temp[2]);
					for(int i=0;i<users.size();i++){
						ChatThread ct_temp = (ChatThread)users.get(i);
						if(ct_temp.chatuser.equals(strs_temp[2])) {
//							send_File(strs_temp[3]);
							ct_temp.ps.println("传送文件#"+strs_temp[3]);
							send_File(strs_temp[3]);
						}
					}
				}else{
					String[] msg = massage.split("#");
					username = msg[0];
					if(username.equals("群聊")){
						for(int i=0;i<users.size();i++){
							ChatThread ct = (ChatThread)users.get(i);
							ct.ps.println(massage);
						}
					}else{
						for(int i=0;i<users.size();i++){/////////////////////////////
							ChatThread ct_special = (ChatThread)users.get(i);
							if(ct_special.chatuser.equals(username)) {
								ct_special.ps.println(massage);
							}
						}
					}
				}//else												
			}catch(Exception ex){}
		}
	}
}
	
	
public static void main (String[] args)  throws Exception{
	new Service();
	}
}