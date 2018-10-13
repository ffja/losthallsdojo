package DiscordBot.Bot;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

public class App extends ListenerAdapter {

    private static AfkCheck currentAfkCheck;

    public static void main(String[] args) {
        try {
            JDA jda = new JDABuilder(AccountType.BOT)
                    .setToken("NDk5NDE3MTc2MTcyNzg5Nzkw.Dp7_Mw.2BqqR4qha_B03dkJCC5bFNLVRhs")
                    .addEventListener(new Handler())// Handles stuff
                    .build().awaitReady(); // There's also an async one

        } catch (LoginException e) {
            // Bad token probably
            e.printStackTrace();
        } catch (InterruptedException e) {
            // buildBlocking() blocks, and blocking can be interrupted
            // Though this will never happen.. ?
            e.printStackTrace();
        }

    }

    // This is where we handle stuff.
    // This should probably be in a separate file.
    static class Handler extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            Member member = event.getMember();
            if (event.getChannelType().isGuild()) {
                if (isAdmin(member))
                    adminMessage(event);
                else if (event.getAuthor().isBot())
                    botMessage(event);
                else if (isLeader(member))
                    leaderMessage(event);
            }

            User author = event.getAuthor();
            MessageChannel channel = event.getChannel(); // TextChannel, PrivateChannel, or Group

            // Basic logging
            Message message = event.getMessage(); // Message object, which *contains* the actual message
            String content = message.getContentDisplay(); // Actual message string
            System.out.printf("[%s#%s]@[#%s]: %s\n", author.getName(), author.getDiscriminator(), channel.getName(),
                    content);
        }
    }

    static void adminMessage(MessageReceivedEvent event) {
        Message message = event.getMessage(); // Message object, which *contains* the actual message
        String content = message.getContentDisplay(); // Actual message string

        if (content.startsWith("!")) {
            if (content.equalsIgnoreCase("!start afk"))
                currentAfkCheck = new AfkCheck(event);
            else if (content.startsWith("!set loc"))
                currentAfkCheck.setLocation(content.substring(9));
            else if (content.equalsIgnoreCase("!end afk"))
                currentAfkCheck.endAfk();
            else if (content.equalsIgnoreCase("!end run"))
                endRun(event);
        }
    }

    static void botMessage(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (currentAfkCheck != null) {
            if (currentAfkCheck.running() && event.getChannel().getName().equalsIgnoreCase("afk-check"))
                currentAfkCheck.addReactions(message);
        } else if (event.getChannel().getName().equalsIgnoreCase("leader-rating")) {
            event.getMessage().addReaction(message.getGuild().getEmoteById(Ref.whitebagID)).queue();
            event.getMessage().addReaction(message.getGuild().getEmoteById(Ref.slimeID)).queue();
        }

    }

    static void leaderMessage(MessageReceivedEvent event) {
        Message message = event.getMessage(); // Message object, which *contains* the actual message
        String content = message.getContentDisplay(); // Actual message string

        if (content.startsWith("!")) {
            if (content.equalsIgnoreCase("!start afk"))
                currentAfkCheck = new AfkCheck(event);
            else if (content.startsWith("!set loc"))
                currentAfkCheck.setLocation(content.substring(9));
            else if (content.equalsIgnoreCase("!end afk"))
                currentAfkCheck.endAfk();
            else if (content.equalsIgnoreCase("!end run"))
                endRun(event);
        }
    }

    static boolean isAdmin(Member member) {
        String topRole = null;
        List<Role> memberRoles = member.getRoles();
        if (memberRoles.size() != 0)
            topRole = memberRoles.get(0).getName();
        if (topRole.equalsIgnoreCase("Sensei") || topRole.equalsIgnoreCase("Hokage"))
            return true;
        return false;
    }

    static boolean isLeader(Member member) {
        String topRole = null;
        List<Role> memberRoles = member.getRoles();
        if (memberRoles.size() != 0)
            topRole = memberRoles.get(0).getName();
        if (topRole.equalsIgnoreCase("Raid Leaders"))
            return true;
        return false;
    }

    static void endRun(MessageReceivedEvent event) {

        VoiceChannel queue = event.getGuild().getVoiceChannelById(Ref.queueID);
        VoiceChannel running = event.getGuild().getVoiceChannelById(Ref.runningID);
        GuildController g = new GuildController(event.getGuild());
        List<Member> members = running.getMembers();
        for (Member i : members) {
            if (!isAdmin(i) || !isLeader(i)) {
                g.moveVoiceMember(i, queue).queue();
            }
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("New Leader Feedback for " + event.getMember().getNickname());
        embed.setThumbnail("https://i.imgur.com/WUWEpuq.png");
        embed.setColor(Color.YELLOW);

        embed.setFooter("Property of ffja",
                "https://previews.123rf.com/images/niyazz/niyazz1309/niyazz130900156/22046029-letters-and-symbols-in-fire-letter-f-.jpg");
        embed.setTimestamp(Instant.now());
        embed.addField("Upvote with " + Ref.whitebag + ". Downvote with " + Ref.slime,
                "Leave a comment as well if you want.", false);
        event.getGuild().getTextChannelById(Ref.ratingChan).sendMessage(embed.build()).queue();

    }
}
