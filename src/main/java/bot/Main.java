
package bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.cdimascio.dotenv.Dotenv;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {
    public static String TOKEN;

    public static boolean IsM1Mac = false;
    public static Map<Server, AudioConnection> audioConnectionForTTS = new HashMap();
    public static Map<Server, TextChannel> textChannelForTTS = new HashMap();


    public static void playAudio(DiscordApi api, String wavfile, AudioConnection audioConnection) {
        if (audioConnection.getChannel().getIdAsString().isBlank()) {
            return;
        }
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new LocalAudioSourceManager());
        final AudioPlayer player = playerManager.createPlayer();
        AudioSource source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);
        playerManager.loadItem(wavfile, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack audioTrack) {
                player.playTrack(audioTrack);
            }

            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                Iterator var2 = audioPlaylist.getTracks().iterator();

                while (var2.hasNext()) {
                    AudioTrack track = (AudioTrack) var2.next();
                    player.playTrack(track);
                }

            }

            public void noMatches() {
            }

            public void loadFailed(FriendlyException e) {
            }
        });
    }

    public static void createWavFile(String inputText) {
        Path input_file = Paths.get("input.txt");
        if (Files.exists(input_file, new LinkOption[0])) {
            try {
                Files.delete(input_file);
                Files.createFile(input_file);
            } catch (IOException var15) {
                System.out.println(var15);
            }
        } else {
            try {
                Files.createFile(input_file);
            } catch (IOException var14) {
                System.out.println(var14);
            }
        }

        File file = new File(input_file.toString());

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(inputText);
            fileWriter.close();
        } catch (IOException var13) {
            System.out.println(var13);
        }


        String x = "/var/lib/mecab/dic/open-jtalk/naist-jdic";
        if (IsM1Mac) {
            x = "/opt/homebrew/lib/mecab/dic/ipadic";
        }
        String m = "./mei_normal.htsvoice";
        String r = "0.7";
        String ow = "output.wav";
        String command = String.format("open_jtalk -x %s -m %s -r %s -ow %s %s", x, m, r, ow, input_file);
        System.out.println(command);
        Runtime runtime = Runtime.getRuntime();
        Process p = null;


        try {
            p = runtime.exec(command);
            p.waitFor();
        } catch (InterruptedException var11) {
            throw new RuntimeException(var11);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream is = p.getInputStream(); // プロセスの結果を変数に格納する
        BufferedReader br = new BufferedReader(new InputStreamReader(is)); // テキスト読み込みを行えるようにする

        while (true) {
            String line;
            try {
                line = br.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (line == null) {
                break; // 全ての行を読み切ったら抜ける
            } else {
                System.out.println("line : " + line); // 実行結果を表示
            }
        }

    }

    public static void main(String[] args) {

        Path file;
        file = Paths.get("token.txt");

        Dotenv dotenv;

        dotenv = Dotenv.load();
        TOKEN = dotenv.get("DISCORD_TOKEN");
        System.out.println("discord token read with dotenv-java.");
//        if (dotenv.get("M1Mac").equalsIgnoreCase("true")) {
//            IsM1Mac = true;
//        }


        DiscordApi api = new DiscordApiBuilder().setToken(TOKEN).login().join();
        System.out.println("discord bot built.");
        api.addMessageCreateListener((event) -> {
            if (event.getMessageAuthor().isBotUser()) {
                return;
            }
            if (!event.getMessageAuthor().isBotUser()) {

                if (event.getMessageContent().equalsIgnoreCase(".debug")) {
                    System.out.println(String.format(".debug hit.\naudioConnectionForTTS: %s\ntextChannelsForTTS: %s", audioConnectionForTTS, textChannelForTTS));
                    Iterator var2 = api.getServers().iterator();

                    while (var2.hasNext()) {
                        Server x = (Server) var2.next();
                        System.out.println(String.format("Now connected to: %s", x));
                    }
                }

                Server server = (Server) event.getServer().get();
                System.out.println(String.format("in addMessageCreateListener: %s", server));
                AudioConnection audioConnection = (AudioConnection) audioConnectionForTTS.get(server);
                TextChannel textChannel = (TextChannel) textChannelForTTS.get(server);
                if (event.getChannel().equals(textChannel)) {
                    String msg = event.getMessageContent();
                    Pattern p = Pattern.compile("[0-9]+>");
                    p.matcher(msg);
                    String msgReplaced = msg.replaceAll("[0-9]+>", "");
                    msgReplaced = msgReplaced.replaceAll("\n", " ");
                    if (msgReplaced.startsWith("http://")) {
                        msgReplaced = "URL省略";
                    } else if (msgReplaced.startsWith("https://")) {
                        msgReplaced = "URL省略";
                    }

                    if (msgReplaced.length() >= 50) {
                        return;
                    }

                    createWavFile(msgReplaced);
                    playAudio(api, "output.wav", audioConnection);
                }


            }
        });
        SlashCommand command = (SlashCommand) SlashCommand.with("join", "VCに接続します。").createGlobal(api).join();
        SlashCommand commandLeave = (SlashCommand) SlashCommand.with("leave", "切断します。").createGlobal(api).join();
        api.addSlashCommandCreateListener((event) -> {
            SlashCommandInteraction slashcommandInteraction = event.getSlashCommandInteraction();
            Server server;
            if (slashcommandInteraction.getUser().isBot()) {
                return;
            }
            if (slashcommandInteraction.getCommandName().equals("join")) {
                server = (Server) slashcommandInteraction.getServer().get();
                TextChannel channel = (TextChannel) slashcommandInteraction.getChannel().get();
                ServerVoiceChannel serverVoiceChannel;
                try {
                    serverVoiceChannel = (ServerVoiceChannel) slashcommandInteraction.getUser().getConnectedVoiceChannel(server).get();

                } catch (Exception e) {
//                    System.out.println(e.getMessage());
                    slashcommandInteraction.createImmediateResponder()
                            .setContent("どこのボイスチャンネルにも参加していません！")
                            .respond();

                    throw new RuntimeException(e);
                }

                serverVoiceChannel.connect().thenAccept((audioConnection) -> {
                    audioConnectionForTTS.put(server, audioConnection);
                    textChannelForTTS.put(server, channel);
                    createWavFile("オープン読み上げボットです！");
                    playAudio(api, "output.wav", audioConnection);
                    channel.sendMessage("Connected.");
                    System.out.println(String.format("on server: %s", serverVoiceChannel));
                    Path greetingFile = Paths.get("greeting.txt");

                    String greetingMessage;
                    try {
                        greetingMessage = Files.readString(greetingFile);
                    } catch (IOException var8) {
                        throw new RuntimeException(var8);
                    }

                    MessageBuilder message = (MessageBuilder) (new MessageBuilder()).setEmbed((new EmbedBuilder()).setTitle("Open読み上げBOTv2").setDescription(greetingMessage).setColor(Color.MAGENTA));
                    message.send(channel);
                }).exceptionally((throwable) -> {
                    throwable.printStackTrace();
                    return null;
                });
                ((InteractionImmediateResponseBuilder) slashcommandInteraction.createImmediateResponder().setContent("Connecting...")).respond();
            } else if (slashcommandInteraction.getCommandName().equals("leave")) {
                ((InteractionImmediateResponseBuilder) slashcommandInteraction.createImmediateResponder().setContent("Disconnecting...")).respond();
                server = (Server) slashcommandInteraction.getServer().get();
                ServerVoiceChannel serverVoiceChannel = (ServerVoiceChannel) slashcommandInteraction.getUser().getConnectedVoiceChannel(server).get();
                serverVoiceChannel.disconnect().join();
                textChannelForTTS.remove(server);
                audioConnectionForTTS.remove(server);
            }

        });
        api.addServerJoinListener((event) -> {
            System.out.println(String.format("Joined to bot server: %s", event.getServer()));
        });
        api.addServerVoiceChannelMemberJoinListener((event) -> {
            if (!event.getUser().isBot()) {
                AudioConnection audioConnection = (AudioConnection) audioConnectionForTTS.get(event.getServer());
                TextChannel var10000 = (TextChannel) textChannelForTTS.get(event.getServer());
                Server server = event.getServer();
                String member = event.getUser().getDisplayName(event.getServer());
                createWavFile(String.format("%sが参加したよ。", member));
                playAudio(api, "output.wav", audioConnection);
            }
        });
        api.addServerVoiceChannelMemberLeaveListener((event) -> {
            if (!event.getUser().isBot()) {
                AudioConnection audioConnection = (AudioConnection) audioConnectionForTTS.get(event.getServer());
                TextChannel var10000 = (TextChannel) textChannelForTTS.get(event.getServer());
                createWavFile(String.format("%sが退出したよ。", event.getUser().getDisplayName(event.getServer())));
                playAudio(api, "output.wav", audioConnection);
            }
        });
    }
}
