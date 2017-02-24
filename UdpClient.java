import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class UdpClient
{
	static InputStream is;
	static OutputStream os; 
	static InputStreamReader isr; 
	static BufferedReader br; 
	public static void main (String [] args ){
		try(Socket socket = new Socket("codebank.xyz", 38005)){
			 is = socket.getInputStream();
			 isr = new InputStreamReader(is, "UTF-8");
			 br = new BufferedReader(isr);
			 os = socket.getOutputStream();
			 handshake(); 
		}
		catch(Exception e){
			System.out.println("Error");
		}
	}
	private static int handshake() throws IOException {
		byte[] array = new byte [24];
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
		//Protocol TCP 
		array[9] = 6; 
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
		array[20] = (byte) 0xDE; 
		array[21] = (byte) 0xAD; 
		array[22] = (byte) 0xBE; 
		array[23] = (byte) 0xEF; 
		byte [] temp = new byte[20];
		for (int i =0; i <20; i++){
			temp[i] = array[i];
		}
		short check =checksum (temp);
		array[10]=(byte) (check>>8);
		array[11] =(byte) check; 
		os.write(array);
		byte[] handArray = new byte [4];
		is.read(handArray);
		System.out.print("geting info from handshake: ");
		System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(handArray));
		System.out.println("");
//		byte [] bArray = new byte[2];
//		is.read(bArray);
//		short[] portvalue = toShortArray(bArray);
//		System.out.println("portVal" + portvalue[0]);
//		return portvalue[0];
		return 0;
	 
		
	}
	private static short[] toShortArray(byte[] bArray) {
		short[] shortArray = new short[(bArray.length + 1) / 2];
        for (int i = 0, j = 0; j < bArray.length - 1; i++, j += 2) 
        {
            shortArray[i] |= (bArray[j] & 0xFF);
            shortArray[i] <<= 8;
            shortArray[i] |= (bArray[j + 1] & 0xFF);
	}
	return shortArray;
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



