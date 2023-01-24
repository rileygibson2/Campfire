package client;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import cli.CLI;
import general.Utils;
import threads.ThreadController;

public class AudioManager {

	private static AudioManager singleton;
	static AudioFormat format;
	private Semaphore micLock;
	private Semaphore speakerLock;

	//Infos
	private LinkedHashMap<String, Mixer.Info> inMixers;
	private LinkedHashMap<String, Mixer.Info> outMixers;
	public Mixer.Info micLineInfo;
	public Mixer.Info speakerLineInfo;

	//Audio lines
	private TargetDataLine micLine;
	private SourceDataLine speakerLine;
	final static int blockLength = 1024;

	//Controls
	private boolean mute;
	private double volume;

	private AudioManager() {
		format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		micLineInfo = null;
		speakerLineInfo = null;
		micLock = new Semaphore(1);
		speakerLock = new Semaphore(1);
		mute = false;
		volume = 75;

		enumerateMicrophones();
		enumerateSpeakers();
		if (getDefaultMic()!=null) micLineInfo = getDefaultMic().getValue();
		if (getDefaultSpeaker()!=null) speakerLineInfo = getDefaultSpeaker().getValue();
	}

	public static AudioManager getInstance() {
		if (singleton==null) singleton = new AudioManager();
		return singleton;
	}

	public void mute() {mute = true;}
	public void unmute() {mute = false;}
	public boolean isMuted() {return mute;}

	public double getVolume() {return volume;}

	public void setVolume(double v) {
		volume = v;
		if (volume<0) volume = 0;
		if (volume>100) volume = 100;
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
		if (m==null) {
			CLI.error("Cannot set micline info to null!");
			return;
		}
		this.micLineInfo = m;
		CLI.debug("New Microphone Line: "+m.getName());
	}

	public void setSpeakerLineInfo(Mixer.Info m) {
		if (m==null) {
			CLI.error("Cannot set speakerline info to null!");
			return;
		}
		this.speakerLineInfo = m;
		CLI.debug("New Speaker Line: "+m.getName());
	}

	public TargetDataLine getTargetLine(Mixer.Info info) {
		try {
			//CLI.debug("Obtaining target line from: "+info.getName());
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
			//CLI.debug("Obtaining source line from: "+info.getName());
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

	public void applyVolume(SourceDataLine speakerLine) {
		if (speakerLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl volumeControl = (FloatControl) speakerLine.getControl(FloatControl.Type.MASTER_GAIN);

			float newVolume = (float) (volume/100);
			float dB = (float) (Math.log(newVolume) / Math.log(10.0) * 20.0);
			volumeControl.setValue(dB);
		}
		else CLI.error("Could not change line volume as gain not supported");
	}

	public SourceDataLine getSpeakerWriter() {
		try {
			speakerLock.acquire();
			if (speakerLineInfo==null) {
				CLI.error("Speaker Line Info is null");
				return null;
			}
			speakerLine = getSourceLine(speakerLineInfo);
			speakerLine.open(format);
			speakerLine.start();
			applyVolume(speakerLine);
			return speakerLine;
		}
		catch (LineUnavailableException | InterruptedException e) {CLI.error("Error getting speaker writer - "+e.getMessage());}

		return null;
	}

	public void releaseSpeakerWriter() {
		speakerLock.release();
		if (speakerLine!=null) {
			speakerLine.close();
			speakerLine = null;
		}
	}

	public ThreadController getMicrophoneReader(byte[] buffer, Runnable updateAction) {
		return new ThreadController() {
			@Override
			public void run() {
				try {
					micLock.acquire();

					if (micLineInfo==null) {
						CLI.error("Mic Line Info is null");
						return;
					}
					micLine = getTargetLine(micLineInfo);
					micLine.open(format);
					micLine.start();

					// Capture audio data and save it to the file
					while (isRunning()) {
						if (micLine==null) {
							if (!Client.isShuttingdown()) CLI.error("Mic became null while running");
							break;
						}
						micLine.read(buffer, 0, buffer.length);
						if (updateAction!=null) updateAction.run();
					}

					if (micLine!=null) {
						micLine.drain();
						micLine.close();
						micLine = null;
					}
				}
				catch (Exception e) {if (!Client.isShuttingdown()) CLI.error("Error reading microphone - "+e.getMessage());}

				micLock.release();
			}
		};
	}

	public ThreadController getSoundWriter(String fileName, boolean loop) {
		return new ThreadController() {
			@Override
			public void run() {
				try {
					speakerLock.acquire();

					if (speakerLineInfo==null) throw new Error("Speaker Line Info is null");
					speakerLine = getSourceLine(speakerLineInfo);
					speakerLine.open(format);
					speakerLine.start(); // Start writing audio data to the speakers
					applyVolume(speakerLine);

					boolean initialPlay = true;

					while (isRunning()&&(initialPlay||loop)) {
						if (initialPlay) initialPlay = false;
						
						// Open the wav file
						InputStream in = Utils.getInputStream("audio/"+fileName); 
						InputStream bufferedIn = new BufferedInputStream(in);
						AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

						AudioFormat baseFormat = audioStream.getFormat();
						AudioFormat doubleSpeedFormat = new AudioFormat(
								baseFormat.getEncoding(),
								(int) (baseFormat.getSampleRate()*0.9), // double the sample rate
								baseFormat.getSampleSizeInBits(),
								baseFormat.getChannels(),
								baseFormat.getFrameSize(),
								(int) (baseFormat.getFrameRate()*0.9), // double the frame rate
								baseFormat.isBigEndian());
						audioStream = AudioSystem.getAudioInputStream(doubleSpeedFormat, audioStream);
						audioStream.mark(Integer.MAX_VALUE);

						// Read the file and play it
						int bytesRead = 0;
						byte[] buffer = new byte[1024];

						while (bytesRead != -1 && isRunning()) {
							if (speakerLine==null) {
								if (!Client.isShuttingdown()) CLI.error("Speakerline became null while playing file");
								break;
							}
							bytesRead = audioStream.read(buffer, 0, buffer.length);
							if (bytesRead >= 0) speakerLine.write(buffer, 0, bytesRead);
						}
						audioStream.close();
					}

					if (speakerLine!=null) {
						speakerLine.drain();
						speakerLine.close();
					}
				}
				catch (Exception e) {if (!Client.isShuttingdown()) CLI.error("Error playing sound - "+e.getMessage());}

				speakerLock.release();
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
