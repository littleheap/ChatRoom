package com.littleheap;

import javax.swing.*;
import com.littleheap.Service.ChatThread;
import java.net.*;
import java.util.ArrayList;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

class User extends JFrame{
	ArrayList<Users> users = new ArrayList<Users>();
	Socket s;
	String userName;
	static String filename;
	String choice;
	JTextField jtf = new JTextField();
	JTextArea jta = new JTextArea();
	JComboBox jcb = new JComboBox();
	JButton jbt = new JButton();
	JButton jbt_data = new JButton();
	PrintStream ps;
	BufferedReader br;
	User() throws Exception{
		users.add(new Users("群聊"));
		//////////主窗口前端/////////////
		getContentPane().setForeground(Color.MAGENTA);
		getContentPane().setBackground(Color.WHITE);
		this.setSize(350,460);
		this.setResizable(false);
		setLocationRelativeTo(null); 
		getContentPane().setLayout(null);
		this.setVisible(true);
		/////////jtf前端//////////////////
		jtf= new JTextField();
		jtf.setFont(new Font("楷体", Font.PLAIN, 22));
		jtf.setBounds(0, 383, 252, 37);
		getContentPane().add(jtf);
		jtf.setColumns(10);
		///////jta前端//////////////////
		jta= new JTextArea();
		jta.setFont(new Font("Monospaced", Font.PLAIN, 17));
		jta.setBackground(new Color(255, 255, 255));
		jta.setBounds(0, 32, 344, 345);
		getContentPane().add(jta);
		///////jcb前端////////////////////
		jcb = new JComboBox();
		jcb.setBounds(0, 0, 196, 29);
		getContentPane().add(jcb);
		///////下线按钮前端/////////////
		jbt = new JButton("\u4E0B\u7EBF");
		jbt.setFont(new Font("幼圆", Font.PLAIN, 17));
		jbt.setForeground(Color.BLUE);
		jbt.setBackground(new Color(204, 255, 102));
		jbt.setBounds(254, 383, 90, 37);
		getContentPane().add(jbt);
		///////文件按钮前端/////////////
		jbt_data = new JButton("\u53D1\u9001\u6587\u4EF6");
		jbt_data.setFont(new Font("宋体", Font.PLAIN, 20));
		jbt_data.setBackground(new Color(102, 204, 204));
		jbt_data.setBounds(198, 0, 146, 29);
		getContentPane().add(jbt_data);
		//////////////////输入账号///////////////////
		userName = JOptionPane.showInputDialog("输入昵称");
		this.setTitle(userName);
		//配置Socket
		s = new Socket("127.0.0.1",9999);		
		ps = new PrintStream(s.getOutputStream());
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		ps.println("LOGIN#"+userName);//发昵称给服务器
		//输入框的ActionListener
		jtf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {									
				int i =0;
				ps.println(choice+"#"+userName + "说：" + jtf.getText());
				if(!choice.equals("群聊")){
					for(i=0;i<users.size();i++){
						if(users.get(i).getName().equals(choice)) break;
					}
					users.get(i).setContext(users.get(i).getContext()+userName + "说：" + jtf.getText()+"\n");
				}
				jtf.setText("");
			}
		});
		//发送文件的按钮响应
		jbt_data.addActionListener(new ActionListener() {							
			public void actionPerformed(ActionEvent arg0) {
				if(jbt_data.getText().equals("发送文件")){
					String[] file_name = jtf.getText().split("\\/");	
					for(int i=0;i<users.size();i++){
						if(users.get(i).name.equals(choice))
							users.get(i).setContext(users.get(i).getContext()+"您向"+choice+"发送了文件："+file_name[file_name.length-1]+"\n");
					}
					ps.println("传送文件"+"#"+userName+"#"+choice+"#"+file_name[file_name.length-1]);
					System.out.println("传送文件"+"#"+userName+"#"+choice+"#"+file_name[file_name.length-1]);
					System.out.println(file_name[file_name.length-1]);
					try {
						Thread.sleep(1000);
						send_file(jtf.getText());
					} catch (Exception e) {	e.printStackTrace();   }										
				}else{
					try {
						for(int i=0;i<users.size();i++){
							if(users.get(i).getName().equals(choice))
								users.get(i).setContext(users.get(i).getContext()+"您已接收文件："+filename);
						}
						receiveFile(jtf.getText()+filename);
					} catch (IOException e) {
						e.printStackTrace();
					}
					jbt_data.setText("发送文件");
				}
			}
		});
		//下线按钮的ActionListener
		jbt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for(int i=0;i<users.size();i++){
					if(users.get(i).getName().equals(userName)){
						users.remove(i);
						break;
					}
				}
				ps.println("下线"+"#"+userName);
				try { s.close(); } catch (IOException e) {e.printStackTrace();}//关闭socket
				dispose();
			}
		});
		//实时获取信息线程
		new Thread(new Runnable() {
			public void run() {
				while(true){
					try{
						String massage= br.readLine();
						String[] strs = massage.split("#");
						if(strs[0].equals("LOGOUT")){
							s.close();
							dispose();
						}else if(strs[0].equals("LOGIN")){
							int j=0;
							jcb.removeAllItems();
							for(int i=1;i<strs.length;i++){
								if(!strs[i].equals(userName)) { jcb.addItem(strs[i]); }
								for(j=0;j<users.size();j++){
									if(users.get(j).name.equals(strs[i]))  break;
								}
								if(j==users.size()&&!strs[i].equals(userName)) users.add(new Users(strs[i]));
							}
							jcb.addItem("群聊");
						}else if(strs[0].equals("传送文件")){
							jbt_data.setText("接收文件");
							filename = strs[1];
						}else{
							String[] str = strs[1].split("\\:");
							if(!str[0].equals(userName+"说")){
								if(strs[0].equals("群聊")){
									jta.setForeground(Color.BLUE);
									for(int i=0;i<users.size();i++){
										if(users.get(i).getName().equals("群聊")){
											users.get(i).setContext(users.get(i).getContext()+strs[1]+"\n");
										}
									}
								}else 
									if(strs[0].equals("系统")){
										jta.setForeground(Color.RED);
										for(int i=0;i<users.size();i++){
											users.get(i).setContext(users.get(i).getContext()+"系统消息："+strs[1]+"\n");
										}
									}else{
										jta.setForeground(Color.BLACK);
										String[] str_temp=strs[1].split("说");
										System.out.println(str_temp[0]);
										for(int i=0;i<users.size();i++){
											if(users.get(i).getName().equals(str_temp[0])){
												users.get(i).setContext(users.get(i).getContext()+strs[1]+"\n");
											}
										}
									}		
							}
						}												
					}catch(Exception e){}
				} 
			}
		}).start();
		//实时获取选择对象
		new Thread(new Runnable() {
			public void run() {
				while(true){
					int i ;
					choice = (String) jcb.getSelectedItem();
					for(i=0;i<users.size();i++){
						if(users.get(i).getName().equals(choice)) break;
					}
					if(i != users.size())
						jta.setText(users.get(i).getContext());
					try{
						Thread.sleep(1000);
					}catch( Exception e){}
				}
			}
		}).start();
}//构造函数完毕
///////////////////发送文件///////////////////////////			
public void send_file(String file_path) throws Exception{
	Socket new_socket = new Socket("127.0.0.1",6666);
	DataOutputStream dos = null;
	FileInputStream fis = null; 
	byte[] sendBytes = null;
	boolean bool;
	double sumL=0;
	int length=0;
	File file = new File(file_path); //要传输的文件路径  
	long file_length = file.length();
	dos = new DataOutputStream(new_socket.getOutputStream());
	fis = new FileInputStream(file);
	sendBytes = new byte[1024];
	while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {  
		sumL += length;    
		System.out.println("已传输："+((sumL/file_length)*100)+"%");  
		dos.write(sendBytes, 0, length);  
		dos.flush();  
	} 
	if(sumL == file_length){ bool = true ;};
	if(dos!=null) 	dos.close();
	if(fis!=null) 	fis.close();
	new_socket.close();
}
////////////////////////////////接收文件////////////////////////////////
public void receiveFile(String filePath) throws IOException {  
	Socket s = new Socket("127.0.0.1",6666);
	byte[] inputByte = null;  
	int length = 0;  
	DataInputStream dis = null;  
 	FileOutputStream fos = null;  
 	try {  
 		dis = new DataInputStream(s.getInputStream());  
 		fos = new FileOutputStream(new File(filePath));      
 		inputByte = new byte[1024];     
 		System.out.println("客户机开始接收数据...");    
 		while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {  
 			fos.write(inputByte, 0, length);  
 			fos.flush();      
 		}  
 		System.out.println("完成接收："+filePath);  
 		}   catch (Exception e) {  e.printStackTrace();  }  
 	if (fos != null)  
 		fos.close();  
 	if (dis != null)  
 		dis.close();  
 	s.close();
}  
//////////////////////////Users类///////////////////////
class Users{
	String context = "";
	String name = "";
	Users(String name){
		this.name = name;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setContext(String context){
		this.context = context;
	}
	public String getName(){
		return this.name;
	}
	public String getContext(){
		return this.context;
	} 
}

public static void main (String[] args)  throws Exception{
	new User();
	}
}
