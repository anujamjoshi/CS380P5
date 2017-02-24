import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class UdpClient
{
	static InputStream is;
	static OutputStream os; 
	static InputStreamReader isr; 
	static BufferedReader br; 
	static int UdpPort;
	public static void main (String [] args ){
		try(Socket socket = new Socket("codebank.xyz", 38005)){
			is = socket.getInputStream();
			isr = new InputStreamReader(is, "UTF-8");
			br = new BufferedReader(isr);
			os = socket.getOutputStream();
			 UdpPort= handshake(); 
			System.out.println("Port number received:" + UdpPort);
			for (int i=1; i <=12; i++){
				int dataSize = (int) Math.pow(2, i);
				System.out.println("Sending packet with "+dataSize+ "bytes of data");
				byte[] UDPpacket = createUDP(dataSize);
				createPacket(UDPpacket);
				byte[] handArray = new byte [4];
				is.read(handArray);
				System.out.print("Handshake response: ");
				System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(handArray));
				
			}
		}
		catch(Exception e){
			System.out.println("Error");
		}
	}
	private static int handshake() throws IOException {
		byte[] data = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
		createPacket(data);
		byte[] handArray = new byte [4];
		is.read(handArray);
		System.out.print("Handshake response: ");
		System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(handArray));
		int port = is.read() << 8 | is.read();
		return port; 
	}
	public static byte[] createUDP(int dataSize ){
		byte [] udp = new byte[8+dataSize];
		//srcPort 
		udp[0]=0;
		udp[1]= 0; 
		//destination port = UdpPort 
		udp[2] = (byte) (UdpPort>>8 & 0xFF);
		udp[3] = (byte) (UdpPort & 0xFF);
		//length 
		udp[4] = (byte) (udp.length>>8 & 0xFF);
		udp[5] = (byte) (udp.length & 0xFF);
		//checksum 
		udp[6] = 0; 
		udp [7] =0; 
		byte [] data = new byte[dataSize];
		new Random().nextBytes(data);
		for (int i = 8; i <udp.length; i++){
			udp[i] = data[i-8];
		}
		short check = checksum(udp);
		udp[6]=(byte) (check>>8);
		udp[7] =(byte) check; 
		return udp; 
		
	}
	
	private static void createPacket(byte[] data) throws IOException {
		byte[] array = new byte [20+data.length];
		// Version + HLen 
		array [0] = 0b01000101; 
		// TOS
		array [1] = 0b00000000; 
		//Length 
		short length = (short) (4+20); 
		array [2] = (byte) (length >> 8); 
		array [3] = (byte) length; 
		//Indent
		array[4] = 0; 
		array[5] =0; 
		//Flag +Offset  
		array [6] = 0b01000000;
		array [7] =0;
		//TTL
		array [8] = 0b00110010;
		//Protocol UDP 
		array[9] = 17; 
		//Temp checksum
		array[10]=0;
		array[11] =0; 
		//Src Adder 

		array[12] = 0b01111111;
		array[13] = 0;
		array[14] = 0;
		array[15]= 0b00000001;
		// dest Address 
		//		String destination=socket.getInetAddress().getHostAddress();
		//		System.out.println(destination); prints out 52.33.181.114
		// note I got the address of the xyz server and i'm now hardcoding it 
		array[16] = 0b00110100;
		array[17] = 0b00100001;
		array[18] = (byte)0xB5;
		array[19]= 0b1110010;
		// 
	
		for (int i =20; i < array.length; i++){
			array[i] = data[i-20];
		}
		byte [] temp = new byte[20];
		for (int i =0; i <20; i++){
			temp[i] = array[i];
		}
		short check =checksum (temp);
		array[10]=(byte) (check>>8);
		array[11] =(byte) check; 
		os.write(array);
		
	}
	private static short checksum(byte[] inputArray) {
		long sum =0; 
		int listSize = inputArray.length;
		int index = 0;
		while (index < inputArray.length-1){
			byte first = inputArray[index];
			byte second = inputArray[index+1];
			sum+= ((first<<8 & 0xFF00)|(second& 0xFF)); 
			if ((sum & 0xFFFF0000) >0){
				sum &= 0xFFFF; 
				sum++;
			}
			index+=2; 
		}


		if (listSize%2 == 1){

			byte first = inputArray[index];
			sum+= ((first<<8 & 0xFF00)); 
			if ((sum & 0xFFFF0000) >0){
				sum &= 0xFFFF; 
				sum++;
			}
		}
		return (short) ~(sum & 0xFFFF); 
	}



}



