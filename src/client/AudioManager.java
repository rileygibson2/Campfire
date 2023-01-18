package client;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioManager {
	static AudioFormat format;

	//Infos
	private Map<String, Line.Info> ins;
	private Map<String, Line.Info> outs;
	public Line.Info micLineInfo;
	public Line.Info speakerLineInfo;

	//Audio lines
	AudioInputStream audioStream;
	private TargetDataLine micLine;
	private SourceDataLine speakerLine;
	final static int blockLength = 1024;
	//44100

	public AudioManager() {
		format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		micLineInfo = null;
		speakerLineInfo = null;

		enumerateMicrophones();
		enumerateSpeakers();
		if (getDefaultMic()!=null) micLineInfo = getDefaultMic().getValue();
		if (getDefaultSpeaker()!=null) speakerLineInfo = getDefaultSpeaker().getValue();
	}

	public Map.Entry<String, Line.Info> getDefaultMic() {
		if (ins==null) return null;
		for (Map.Entry<String, Line.Info> m : ins.entrySet()) return m;
		return null;
	}

	public Map.Entry<String, Line.Info> getDefaultSpeaker() {
		if (outs==null) return null;
		for (Map.Entry<String, Line.Info> m : outs.entrySet()) return m;
		return null;
	}

	public void enumerateMicrophones() {
		ins = new HashMap<String, Line.Info>();
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfos){
			Mixer m = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = m.getTargetLineInfo();

			if (lineInfos.length>=1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
				ins.put(info.getName(), lineInfos[0]); //Only add if it's an audio input device
			}
		}
	}

	public void enumerateSpeakers() {
		outs = new HashMap<String, Line.Info>();
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfos){
			Mixer m = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = m.getSourceLineInfo();

			if (lineInfos.length>=1 && lineInfos[0].getLineClass().equals(SourceDataLine.class)) {
				outs.put(info.getName(), lineInfos[0]); //Only add if it's an audio output device
			}
		}
	}

	public LinkedHashMap<String, Line.Info> listMicrophones() {
		enumerateMicrophones();
		LinkedHashMap<String, Line.Info> mics = new LinkedHashMap<>();
		for (Map.Entry<String, Line.Info> m : ins.entrySet()) mics.put(m.getKey(), m.getValue());
		return mics;
	}

	public LinkedHashMap<String, Line.Info> listSpeakers() {
		enumerateSpeakers();
		LinkedHashMap<String, Line.Info> speakers = new LinkedHashMap<>();
		for (Map.Entry<String, Line.Info> m : outs.entrySet()) speakers.put(m.getKey(), m.getValue());
		return speakers;
	}

	public AudioInputStream startInputStream(AudioFormat format) {
		try {
			micLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
			micLine.open();
			System.out.println("TargetLine available: "+micLine.available());
			micLine.start();
			return new AudioInputStream(micLine);

		} catch (LineUnavailableException e) {throw new Error("Error creating input stream from microphone: "+e.toString());}
	}

	public Thread getInputStreamThread(byte[] buffer) {
		return new Thread() {
			@Override
			public void run() {
				try {
					// Get the default microphone
					TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
					microphone.open(format); // Open the microphone
					microphone.start(); // Start capturing audio data

					// Open audio output (e.g. speakers)
					DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
					SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(speakerInfo);
					speakers.open(format);
					speakers.start(); // Start writing audio data to the speakers

					//buffer = new byte[blockLength]; // Create a buffer to hold the audio data

					// Capture audio data and save it to the file
					while (true) {
						int bytesRead = microphone.read(buffer, 0, buffer.length);
						speakers.write(buffer, 0, bytesRead);
					}
				}
				catch (Exception e) {e.printStackTrace();}
			}
		};
	}

	static double[] decode(byte[] buffer, AudioFormat format) {
		int  bits = format.getSampleSizeInBits();
		double max = Math.pow(2, bits - 1);

		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(format.isBigEndian() ?
				ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

		double[] samples = new double[buffer.length * 8 / bits];
		for(int i=0; i<samples.length; ++i) {
			switch(bits) {
			case 8:  samples[i] = ( bb.get()      / max ); break;
			case 16: samples[i] = ( bb.getShort() / max ); break;
			case 32: samples[i] = ( bb.getInt()   / max ); break;
			case 64: samples[i] = ( bb.getLong()  / max ); break;
			}
		}

		return samples;
	}

	public void release() {
		try {
			if (speakerLine!=null) {
				speakerLine.drain();
				speakerLine.close();
				speakerLine = null;
			}
			if (micLine!=null) {
				micLine.drain();
				micLine.close();
				micLine = null;
			}
			if (audioStream!=null) {
				audioStream.close();
				audioStream = null;
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}  

	@Deprecated
	private void runSpectro(boolean useMic, boolean useSpeaker) {

		try {
			//Input stuff
			if (useMic) audioStream = getMicInputStream(format);
			else audioStream = getFileInputStream("lonedigger.wav");

			//Output stuff
			Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
			speakerLine = (SourceDataLine)AudioSystem.getLine(speakerInfo);
			speakerLine.open(format);
			speakerLine.start();

			//Read file
			final byte[] buffer = new byte[blockLength];
			while ((audioStream.read(buffer)) != -1)  {
				if (useSpeaker) speakerLine.write(buffer, 0, blockLength); //Write to speakers

			}

		}
		catch (IOException e) {e.printStackTrace();}
		catch (LineUnavailableException e) {e.printStackTrace();}
	}

	@Deprecated
	public AudioInputStream getFileInputStream(String fileName) {
		try {
			return AudioSystem.getAudioInputStream(new File(fileName));
		} catch (UnsupportedAudioFileException | IOException e) {
			throw new Error("Error with getting audio from file.");
		}
	}

	@Deprecated
	public AudioInputStream getMicInputStream(AudioFormat format) {
		try {
			micLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
			micLine.open();
			System.out.println("TargetLine available: "+micLine.available());
			micLine.start();
			return new AudioInputStream(micLine);

		} catch (LineUnavailableException e) {throw new Error("Error creating input stream from microphone");}
	}

	@Deprecated
	public TargetDataLine getTargetDataLine(String name) {
		Line.Info lineInfo = null;
		for (Map.Entry<String, Line.Info> m : ins.entrySet()) {
			if (m.getKey().equals(name)) lineInfo = m.getValue();
		}
		if (lineInfo==null) throw new Error("Line not found");

		try {return (TargetDataLine) AudioSystem.getLine(lineInfo);}
		catch (LineUnavailableException ex) {ex.printStackTrace(); return null;}
	}
}
