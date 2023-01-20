package client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import general.CLI;
import general.Utils;
import threads.ThreadController;

public class AudioManager {
	
	private static AudioManager singleton;
	
	static AudioFormat format;

	//Infos
	private LinkedHashMap<String, Mixer.Info> inMixers;
	private LinkedHashMap<String, Mixer.Info> outMixers;
	public Mixer.Info micLineInfo;
	public Mixer.Info speakerLineInfo;

	//Audio lines
	private TargetDataLine micLine;
	private SourceDataLine speakerLine;
	final static int blockLength = 1024;
	//44100

	private AudioManager() {
		format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		micLineInfo = null;
		speakerLineInfo = null;

		enumerateMicrophones();
		enumerateSpeakers();
		CLI.debug("INS: "+inMixers);
		CLI.debug("OUTS: "+outMixers);
		if (getDefaultMic()!=null) micLineInfo = getDefaultMic().getValue();
		if (getDefaultSpeaker()!=null) speakerLineInfo = getDefaultSpeaker().getValue();
	}
	
	public static AudioManager getInstance() {
		if (singleton==null) singleton = new AudioManager();
		return singleton;
	}

	public Map.Entry<String, Mixer.Info> getDefaultMic() {
		if (inMixers==null) return null;
		for (Map.Entry<String, Mixer.Info> m : inMixers.entrySet()) return m;
		return null;
	}

	public Map.Entry<String, Mixer.Info> getDefaultSpeaker() {
		if (outMixers==null) return null;
		for (Map.Entry<String, Mixer.Info> m : outMixers.entrySet()) return m;
		return null;
	}

	public void enumerateMicrophones() {
		inMixers = new LinkedHashMap<String, Mixer.Info>();
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfos){
			Mixer m = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = m.getTargetLineInfo();

			if (lineInfos.length>=1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
				inMixers.put(info.getName(), info); //Only add if it's an audio input device
			}
		}
	}

	public void enumerateSpeakers() {
		outMixers = new LinkedHashMap<String, Mixer.Info>();
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfos){
			Mixer m = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = m.getSourceLineInfo();

			if (lineInfos.length>=1 && lineInfos[0].getLineClass().equals(SourceDataLine.class)) {
				outMixers.put(info.getName(), info); //Only add if it's an audio output device
			}
		}
	}

	public LinkedHashMap<String, Mixer.Info> listMicrophones() {
		enumerateMicrophones();
		return inMixers;
	}

	public LinkedHashMap<String, Mixer.Info> listSpeakers() {
		enumerateSpeakers();
		return outMixers;
	}
	
	public void setMicLineInfo(Mixer.Info m) {
		this.micLineInfo = m;
		CLI.debug("New Microphone Line: "+m.getName());
	}
	
	public void setSpeakerLineInfo(Mixer.Info m) {
		this.speakerLineInfo = m;
		CLI.debug("New Speaker Line: "+m.getName());
	}
	
	public TargetDataLine getTargetLine(Mixer.Info info) {
		try {
			CLI.debug("Obtaining target line from: "+info.getName());
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
			
			if (targetLineInfos.length==0) {
				CLI.error("Mixer "+info.getName()+" was requested for a target line and has none.");
				return null;
			}
			return (TargetDataLine) mixer.getLine(targetLineInfos[0]);
		}
		catch (LineUnavailableException ex) {ex.printStackTrace(); return null;}
	
	}
	
	public SourceDataLine getSourceLine(Mixer.Info info) {
		try {
			CLI.debug("Obtaining source line from: "+info.getName());
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();
			
			if (sourceLineInfos.length==0) {
				CLI.error("Mixer "+info.getName()+" was requested for a source line and has none.");
				return null;
			}
			return (SourceDataLine) mixer.getLine(sourceLineInfos[0]);
			//return (SourceDataLine) mixer.getLine(new DataLine.Info(SourceDataLine.class, format));
		}
		catch (LineUnavailableException ex) {ex.printStackTrace(); return null;}
	}

	public ThreadController getMicrophoneReader(byte[] buffer) {
		return new ThreadController() {
			@Override
			public void run() {
				try {
					//TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
					if (micLineInfo==null) throw new Error("Mic Line Info is null");
					micLine = getTargetLine(micLineInfo);
					micLine.open(format);
					micLine.start();

					//DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
					//SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(speakerInfo);
					
					if (speakerLineInfo==null) throw new Error("Speaker Line Info is null");
					speakerLine = getSourceLine(speakerLineInfo);
					speakerLine.open(format);
					speakerLine.start(); 
					
					// Capture audio data and save it to the file
					while (isRunning()) {
						if (micLine==null) {
							CLI.error("Mic became null while running");
							return;
						}
						int bytesRead = micLine.read(buffer, 0, buffer.length);
						if (speakerLine!=null) speakerLine.write(buffer, 0, bytesRead);
					}
					
					//Release and reset
					speakerLine.close();
					speakerLine = null;
					micLine.drain();
					micLine.close();
					micLine = null;
				}
				catch (Exception e) {e.printStackTrace();}
			}
		};
	}
	
	public ThreadController getSoundWriter(String fileName, boolean loop) {
		return new ThreadController() {
			@Override
			public void run() {
				try {
		            if (speakerLineInfo==null) throw new Error("Speaker Line Info is null");
					speakerLine = getSourceLine(speakerLineInfo);
					speakerLine.open(format);
					speakerLine.start(); // Start writing audio data to the speakers
		            
					
					// Open the wav file
		            AudioInputStream audioStream = AudioSystem.getAudioInputStream(Utils.getInputStream("audio/"+fileName));
		            audioStream.mark(Integer.MAX_VALUE);
		            
		            boolean initialPlay = true;
		            
		            while (isRunning()&&(initialPlay||loop)) {
		            	if (initialPlay) initialPlay = false;
		            	else audioStream.reset();
		            	
			            // Read the file and play it
			            int bytesRead = 0;
			            byte[] buffer = new byte[1024];
			            while (bytesRead != -1 && isRunning()) {
			                bytesRead = audioStream.read(buffer, 0, buffer.length);
			                if (bytesRead >= 0) speakerLine.write(buffer, 0, bytesRead);
			            }
		            }
		            
		            audioStream.close();
		            speakerLine.drain();
		            speakerLine.close();
		        }
				catch (Exception e) { e.printStackTrace();}
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
		if (speakerLine!=null) {
			speakerLine.close();
			speakerLine = null;
		}
		if (micLine!=null) {
			micLine.drain();
			micLine.close();
			micLine = null;
		}
	}
}
