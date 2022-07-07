
package bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;

public class LavaplayerAudioSource extends AudioSourceBase {
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    public LavaplayerAudioSource(DiscordApi api, AudioPlayer audioPlayer) {
        super(api);
        this.audioPlayer = audioPlayer;
    }

    public byte[] getNextFrame() {
        return this.lastFrame == null ? null : this.applyTransformers(this.lastFrame.getData());
    }

    public boolean hasFinished() {
        return false;
    }

    public boolean hasNextFrame() {
        this.lastFrame = this.audioPlayer.provide();
        return this.lastFrame != null;
    }

    public AudioSource copy() {
        return new LavaplayerAudioSource(this.getApi(), this.audioPlayer);
    }
}
