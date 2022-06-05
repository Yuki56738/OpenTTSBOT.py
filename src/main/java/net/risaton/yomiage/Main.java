package net.risaton.yomiage;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static String TOKEN;

    public static AudioConnection audioConnectionGlobal;
    public static ServerVoiceChannel serverVoiceChannelGlobal;

    public static void playAudio(DiscordApi api, String wavfile) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new LocalAudioSourceManager());
        AudioPlayer player = playerManager.createPlayer();


        AudioSource source = new LavaplayerAudioSource(api, player);
        audioConnectionGlobal.setAudioSource(source);
//        playAudio(playerManager, player);


        playerManager.loadItem(wavfile, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                player.playTrack(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    player.playTrack(track);
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public static void createWavFile(String inputText) {
//        String input_file = "input.txt";
        Path input_file = Paths.get("input.txt");
        if (Files.exists(input_file)) {
            try {
                Files.delete(input_file);
                Files.createFile(input_file);
            } catch (IOException e) {
                System.out.println(e);
            }
        } else {
            try {
                Files.createFile(input_file);
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        File file = new File(input_file.toString());
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(inputText);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        //Call open_jtalk
        String x = "/var/lib/mecab/dic/open-jtalk/naist-jdic";
        String m = "./mei_normal.htsvoice";
        String r = "0.7";
        String ow = "output.wav";
        String command = String.format("/usr/bin/open_jtalk -x %s -m %s -r %s -ow %s %s", x, m, r, ow, input_file);
        System.out.println(command);

        Runtime runtime = Runtime.getRuntime();
        Process p = null;
        try {
            p = runtime.exec(command);
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
//        try {
//            runtime.exec(command);
//        } catch (IOException e) {
//            System.out.println(e);
//        }
    }

    public static void main(String[] args) {
        Path file = Paths.get("token.txt");
        try {
            TOKEN = Files.readString(file);
            System.out.println("discord token read.");
//            System.out.println(TOKEN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DiscordApi api = new DiscordApiBuilder().setToken(TOKEN).login().join();
        System.out.println("discord bot built.");

        api.addMessageCreateListener(event -> {
            System.out.println("Message received.");
            if (event.getMessageAuthor().isBotUser()) {
                return;
            }

            System.out.println(event.getMessageContent());

            createWavFile(event.getMessageContent());
            playAudio(api, "output.wav");
            if (event.getMessageContent().equals(".yuki")) {
                System.out.println("Message yuki received.");
                event.getChannel().sendMessage("こんにちは");
                System.out.println("Sent こんにちは.");

            }

        });
//       Source source = (AudioSource) ;

        //Create slash commands
        Server server = api.getServerById("813401986299854859").get();

        SlashCommand command = SlashCommand.with("join", "connect to voice channel").createForServer(server).join();

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashcommandInteraction = event.getSlashCommandInteraction();
            if (slashcommandInteraction.getCommandName().equals("join")) {


                TextChannel channel = slashcommandInteraction.getChannel().get();
//            channel.sendMessage("Connecting to channel...").join();
//                VoiceChannel voiceChannel = slashcommandInteraction.getUser().getConnectedVoiceChannel(server).get();
//                ServerVoiceChannel serverVoiceChannel = (ServerVoiceChannel) voiceChannel;
                serverVoiceChannelGlobal = slashcommandInteraction.getUser().getConnectedVoiceChannel(server).get();
                serverVoiceChannelGlobal.connect().thenAccept(audioConnection -> {
                    //Do stuff
//                    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
//                    playerManager.registerSourceManager(new LocalAudioSourceManager());
//                    AudioPlayer player = playerManager.createPlayer();
//
//
//                    AudioSource source = new LavaplayerAudioSource(api, player);
//                    audioConnection.setAudioSource(source);
//                    playAudio(playerManager, player);
//                audioConnection = audioConnection;
                    audioConnectionGlobal = audioConnection;
                    playAudio(api, "test.wav");

                    channel.sendMessage("Connected.");
                }).exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
                slashcommandInteraction.createImmediateResponder()
                        .setContent("Connecting...").respond();
            } else if (slashcommandInteraction.getCommandName().equals("leave")) {
                slashcommandInteraction.createImmediateResponder()
                        .setContent("Disconnecting...").respond();
                ServerVoiceChannel serverVoiceChannel = slashcommandInteraction.getUser().getConnectedVoiceChannel(server).get();
                serverVoiceChannel.disconnect().join();
            }
        });
    }
}