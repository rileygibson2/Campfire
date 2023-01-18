package client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
	final AudioFormat format;

	//Infos
	private Map<String, Line.Info> ins;
	private Map<String, Line.Info> outs;
	public Line.Info micLineInfo;
	public Line.Info speakerLineInfo;

	//Audio lines
	AudioInputStream audioStream;
	private TargetDataLine micLine;
	private SourceDataLine speakerLine;
	private final static int blockLength = 1024;
	//44100

	public AudioManager() {
		format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100*2, 16, 1, 2, 44100, false);
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

	public void release() {
		try {
			if (speakerLine!=null) {
				speakerLine.drain();
				speakerLine.close();
				speakerLine = null;
			}
			if (audioStream!=null) {
				audioStream.close();
				audioStream = null;
			}
			if (micLine!=null) {
				micLine.drain();
				micLine.close();
				micLine = null;
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}

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

	public AudioInputStream getFileInputStream(String fileName) {
		try {
			return AudioSystem.getAudioInputStream(new File(fileName));
		} catch (UnsupportedAudioFileException | IOException e) {
			throw new Error("Error with getting audio from file.");
		}
	}

	public AudioInputStream getMicInputStream(AudioFormat format) {
		try {
			micLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, format));
			micLine.open();
			System.out.println("TargetLine available: "+micLine.available());
			micLine.start();
			return new AudioInputStream(micLine);

		} catch (LineUnavailableException e) {throw new Error("Error creating input stream from microphone");}
	}

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
