package cecs429.indexing.diskIndex;
import static java.lang.Math.log;

import java.io.IOException;
import java.io.RandomAccessFile;

public class VariableByteCode {
    public byte[] encodeNumber(int n) {
        int i;
        if (n == 0) {
            //return new byte[]{0};
            i = (int)(log(1) / log(128)) + 1;
        } else {
            i = (int)(log(n) / log(128)) + 1;
        }
        byte[] rv = new byte[i];
        int j = i - 1;
        do {
            rv[j--] = (byte)(n % 128);
            n /= 128;
        } while (j >= 0);
        rv[i - 1] += 128;
        return rv;
    }
    public int decodeNumber(RandomAccessFile postings) throws IOException
   {
	   int n = 0;
	   byte[] buffer = new byte[1];
		while (postings.read(buffer, 0, buffer.length)>0) {
			if ((buffer[0] & 0xff) < 128) {
				n = 128 * n + buffer[0];
			} else {
				int num = (128 * n + ((buffer[0] - 128) & 0xff));
				n = 0;
				if(num==40495){
					System.out.println();
				}
				return num;
			}
		}	
		return n;
   }
}