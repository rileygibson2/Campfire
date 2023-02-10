package general;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFormat;

import network.messages.Code;

public class Utils {

	public static Code getCode(String c) {
		for (Code code : Code.values()) {
			if (code.c.equals(c)) return code;
		}
		return null;
	}
	
	public static InputStream getInputStream(String path) {
		return Utils.class.getClassLoader().getResourceAsStream(path);
	}
	
	public static URL getURL(String path) {
		return Utils.class.getClassLoader().getResource(path);
	}
	
	public static void setCursor(int c) {
		//GUI.frame.setCursor(Cursor.getPredefinedCursor(c));
	}
	
	/**
	 * Shrinks the input array by a factor of the amound variable, averging to do so.
	 * Makes sure all elements of new averaged array are within bounds and strips
	 * out all values below or equal to 0.
	 * 
	 * @param in
	 * @param amount
	 * @param bounds
	 * @return
	 */
	public static int[] averageAndShrinkAndScale(int[] in, int amount, Point bounds) {
		int[] out = new int[in.length/amount];
		int z=0, total=0, avg;
		
		for (int i=0; i<in.length; i++) {
			if (i%amount==0) {
				if (z<out.length) { //Average and store
					avg = (int) (total/amount);
					out[z] = avg;
				}
				total = 0;
				z++;
			}
			total += in[i];
		}
		
		for (int i=0; i<out.length; i++) {
			out[i] = out[i]/10;
			if (out[i]>bounds.y) out[i] = (int) bounds.y;
			if (out[i]<bounds.x) out[i] = (int) bounds.x;
		}
		return out;
	}
	
	public static int[] decodeAmplitude(AudioFormat format, byte[] audioBytes) {  
		int[] audioData = null;  
		if (format.getSampleSizeInBits() == 16) {  
			int nlengthInSamples = audioBytes.length / 2;  
			audioData = new int[nlengthInSamples];  
			if (format.isBigEndian()) {  
				for (int i = 0; i < nlengthInSamples; i++) {  
					/* First byte is MSB (high order) */  
					int MSB = audioBytes[2 * i];  
					/* Second byte is LSB (low order) */  
					int LSB = audioBytes[2 * i + 1];  
					audioData[i] = MSB << 8 | (255 & LSB);  
				}  
			} else {  
				for (int i = 0; i < nlengthInSamples; i++) {  
					/* First byte is LSB (low order) */  
					int LSB = audioBytes[2 * i];  
					/* Second byte is MSB (high order) */  
					int MSB = audioBytes[2 * i + 1];  
					audioData[i] = MSB << 8 | (255 & LSB);  
				}  
			}  
		} else if (format.getSampleSizeInBits() == 8) {  
			int nlengthInSamples = audioBytes.length;  
			audioData = new int[nlengthInSamples];  
			if (format.getEncoding().toString().startsWith("PCM_SIGN")) {  
				// PCM_SIGNED  
				for (int i = 0; i < audioBytes.length; i++) {  
					audioData[i] = audioBytes[i];  
				}  
			} else {  
				// PCM_UNSIGNED  
				for (int i = 0; i < audioBytes.length; i++) {  
					audioData[i] = audioBytes[i] - 128;  
				}  
			}  
		}
		return audioData;  
	}
}
