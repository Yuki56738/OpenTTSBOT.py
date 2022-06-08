package yomiage;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageActivityType;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static String TOKEN;

//    public static AudioConnection audioConnectionGlobal;
//    public static ServerVoiceChannel serverVoiceChannelGlobal;

    public static Map<Server, AudioConnection> channelsForTTS = new HashMap<>();
    public static Map<Server, TextChannel> textChannelForTTS = new HashMap<>();

    public static void playAudio(DiscordApi api, String wavfile, AudioConnection audioConnection) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new LocalAudioSourceManager());
        AudioPlayer player = playerManager.createPlayer();


        AudioSource source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);
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
//    public static void playAudioRemote(DiscordApi api, String remoteSource, AudioConnection audioConnection){
//        // Create a player manager
//        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
//        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
//        AudioPlayer player = playerManager.createPlayer();
//
//// Create an audio source and add it to the audio connection's queue
//        AudioSource source = new LavaplayerAudioSource(api, player);
//        audioConnection.setAudioSource(source);
//
//// You can now use the AudioPlayer like you would normally do with Lavaplayer, e.g.,
//        playerManager.loadItem(remoteSource, new AudioLoadResultHandler() {
//            @Override
//            public void trackLoaded(AudioTrack track) {
//                player.playTrack(track);
//            }
//
//            @Override
//            public void playlistLoaded(AudioPlaylist playlist) {
//                for (AudioTrack track : playlist.getTracks()) {
//                    player.playTrack(track);
//                }
//            }
//
//            @Override
//            public void noMatches() {
//                // Notify the user that we've got nothing
//            }
//
//            @Override
//            public void loadFailed(FriendlyException throwable) {
//                // Notify the user that everything exploded
//            }
//        });
//    };

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

        //Initialize discord Bot
        DiscordApi api = new DiscordApiBuilder().setToken(TOKEN).login().join();
        System.out.println("discord bot built.");

        //on message received
        api.addMessageCreateListener(event -> {
            System.out.println("Message received.");

            if (event.getMessageAuthor().isBotUser()) {
                return;
            }
            if (event.isPrivateMessage()) {
                System.out.println(String.format("private message received: %s", event.getMessageContent()));
            }
//            if (event.getMessage().getMentionedUsers()){
//                System.out.println("Mentionned.");
//            }
//            System.out.println(String.format("mention: %s", event.getMessage().);

            System.out.println(event.getMessageContent());

            if (event.getMessageContent().equalsIgnoreCase(".debug")) {

                System.out.println(String.format(".debug hit.\nchannelsForTTS: %s\ntextChannelsForTTS: %s", channelsForTTS, textChannelForTTS));
                for (Server x : api.getServers()) {
                    System.out.println(String.format("Now connected to: %s", x));
                }

            }

            Server server = event.getServer().get();
            System.out.println(String.format("in addMessageCreateListener: %s", server));

            AudioConnection audioConnection = channelsForTTS.get(server);
            TextChannel textChannel = textChannelForTTS.get(server);
            if (event.getChannel().equals(textChannel)) {
                String msg = event.getMessageContent();
                Pattern p = Pattern.compile("[0-9]+>");
                Matcher m = p.matcher(msg);
                String msgReplaced;
                msgReplaced = msg.replaceAll("[0-9]+>", "");
                msgReplaced = msgReplaced.replaceAll("\n", " ");
                if (msgReplaced.startsWith("http://")){
                    msgReplaced = "URL省略";
                } else if (msgReplaced.startsWith("https://")) {
                    msgReplaced = "URL省略";
                }
                if (msgReplaced.length() >= 50){
                    return;
                }
                createWavFile(msgReplaced);

                playAudio(api, "output.wav", audioConnection);
            }
            Message message = event.getMessage();
            if (message.getMentionedUsers().contains(api.getYourself())){
                System.out.println("mentiond.");
                TextChannel textChannel1 = event.getChannel();
                event.getMessage().reply(message.getContent());
                if (message.getContent().contains("play")){

                }
            }

        });
        //egister slash commands
        SlashCommand command = SlashCommand.with("join", "VCに接続します。").createGlobal(api).join();
        SlashCommand commandLeave = SlashCommand.with("leave", "切断します。").createGlobal(api).join();
//        SlashCommand commandPlay = SlashCommand.with("play", "音楽を再生します。")
//        List commandList = api.getGlobalSlashCommands().join();

        //on slash command hit
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction slashcommandInteraction = event.getSlashCommandInteraction();

            if (slashcommandInteraction.getCommandName().equals("join")) {
                Server server = slashcommandInteraction.getServer().get();
                TextChannel channel = slashcommandInteraction.getChannel().get();
                ServerVoiceChannel serverVoiceChannel = slashcommandInteraction.getUser().getConnectedVoiceChannel(server).get();

                //connect to vc
                serverVoiceChannel.connect().thenAccept(audioConnection -> {

//                    audioConnectionGlobal = audioConnection;
                    channelsForTTS.put(server, audioConnection);
                    textChannelForTTS.put(server, channel);
                    createWavFile("オープン読み上げボットです！");
                    playAudio(api, "output.wav", audioConnection);
                    channel.sendMessage("Connected.");

                    System.out.println(String.format("on server: %s", serverVoiceChannel));

                    //挨拶メッセージ
                    Path greetingFile = Paths.get("greeting.txt");
                    String greetingMessage;
                    try {
                        greetingMessage = Files.readString(greetingFile);
//                        System.out.println("");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
//                    CompletableFuture<Message> messageBuilder =
                    MessageBuilder message =
                            new MessageBuilder()
                                    .setEmbed(new EmbedBuilder()
                                            .setTitle("オープン読み上げBOTv2")
                                            .setDescription(greetingMessage)
                                            .setColor(Color.MAGENTA));
//                    try {
//                    CompletableFuture<Message> message1 = message.send(channel);



                }).exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });


                //slash command response on /join
                slashcommandInteraction.createImmediateResponder()
                        .setContent("Connecting...").respond();
            } else if (slashcommandInteraction.getCommandName().equals("leave")) {
                slashcommandInteraction.createImmediateResponder()
                        .setContent("Disconnecting...").respond();
                Server server = slashcommandInteraction.getServer().get();
                ServerVoiceChannel serverVoiceChannel = slashcommandInteraction.getUser().getConnectedVoiceChannel(server).get();
                serverVoiceChannel.disconnect().join();
                textChannelForTTS.remove(server);
                channelsForTTS.remove(server);
            }
        });

        //on joined bot server
        api.addServerJoinListener(event -> {
           System.out.println(String.format("Joined to bot server: %s", event.getServer()));
        });

        api.addServerVoiceChannelMemberJoinListener(event -> {
            if (event.getUser().isBot()){
                return;
            }
            AudioConnection audioConnection = channelsForTTS.get(event.getServer());
            TextChannel textChannel= textChannelForTTS.get(event.getServer());
            createWavFile(String.format("%sが参加したよ。", event.getUser().getDisplayName(event.getServer())));
            playAudio(api, "output.wav", audioConnection);
        });
        api.addServerVoiceChannelMemberLeaveListener(event -> {
            if (event.getUser().isBot()){
                return;
            }
            AudioConnection audioConnection = channelsForTTS.get(event.getServer());
            TextChannel textChannel= textChannelForTTS.get(event.getServer());
//            System.out.println();
//            if (audioConnection.getChannel().asServerVoiceChannel()){
//                System.out.println(String.format("ServerVoiceChannel is empty."));
//                return;
//            }
            createWavFile(String.format("%sが退出したよ。", event.getUser().getDisplayName(event.getServer())));
            playAudio(api, "output.wav", audioConnection);
//            VoiceChannel voiceChannel = event.getChannel();
//            System.out.println(audioConnection.getChannel().getConnectedUsers());
//            if (audioConnection.getChannel().getConnectedUsers().remo((Predicate<? super User>) api.getUserById(api.getClientId()))){
//                audioConnection.getChannel().disconnect().join();
//            }
        });

    }
}
