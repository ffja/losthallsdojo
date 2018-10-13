package DiscordBot.Bot;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;

public class AfkCheck {

    private TextChannel chan;
    private boolean running;
    private Message message;
    private String location;

    public AfkCheck(MessageReceivedEvent event) {
        message = null;
        running = true;
        location = "Location hasn't been set yet.";
        chan = event.getGuild().getTextChannelById(Ref.afkChan);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("New AFK Check for a Lost Halls!");
        embed.setThumbnail("https://i.imgur.com/WUWEpuq.png");
        embed.setColor(Color.red);

        embed.setFooter("Property of ffja",
                "https://previews.123rf.com/images/niyazz/niyazz1309/niyazz130900156/22046029-letters-and-symbols-in-fire-letter-f-.jpg");
        embed.setTimestamp(Instant.now());

        embed.addField("React with " + Ref.kendo + " if you want to participate.",
                "If you do not react you will not be coming.", false);
        embed.addField("React with " + Ref.key + " if you have a key, and a " + Ref.vial + " if you have a vial.",
                "We appreciate everyone who pops for us", false);
        embed.addField(
                "React with " + Ref.knight + "  " + Ref.pally + "  " + Ref.priest + "  " + Ref.war
                        + " if you are bringing one of these classes.",
                "Bringing melee classes are highly encouraged.", false);

        chan.sendMessage(embed.build()).queue();

    }

    public void addReactions(Message message) {
        this.message = message;
        Guild g = chan.getGuild();
        message.addReaction(g.getEmoteById(Ref.keyID)).queue();
        message.addReaction(g.getEmoteById(Ref.vialID)).queue();
        message.addReaction(g.getEmoteById(Ref.kendoID)).queue();
        message.addReaction(g.getEmoteById(Ref.knightID)).queue();
        message.addReaction(g.getEmoteById(Ref.warID)).queue();
        message.addReaction(g.getEmoteById(Ref.pallyID)).queue();
        message.addReaction(g.getEmoteById(Ref.priestID)).queue();
    }

    public void endAfk() {
        if (running) {
            running = false;
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("AFK Check is Now Over.");
            embed.addField("Get in queue and wait for the next run.",
                    "If the run hasn't started yet, leaders might drag you in.", false);
            embed.setColor(Color.blue);
            embed.setFooter("Property of ffja",
                    "https://previews.123rf.com/images/niyazz/niyazz1309/niyazz130900156/22046029-letters-and-symbols-in-fire-letter-f-.jpg");
            embed.setTimestamp(Instant.now());

            chan.sendMessage(embed.build()).queue();

            String id = message.getId();
            Guild guild = message.getGuild();
            GuildController g = new GuildController(guild);
            List<Member> queueMembers = guild.getVoiceChannelById(Ref.queueID).getMembers();
            List<MessageReaction> reactions = chan.getMessageById(id).complete().getReactions();

            List<User> usersReact = reactions.get(getKendoIndex(reactions)).getUsers().complete();
            for (Member i : queueMembers) {
                if (usersReact.contains(i.getUser())) {
                    if (queueMembers.contains(i)) {
                        g.moveVoiceMember(i, guild.getVoiceChannelById(Ref.runningID)).queue();

                    }
                }
            }

        } else
            chan.sendMessage("There is no afk check up.").queue();
    }

    public boolean running() {
        return running;
    }

    private void sendVialsMessage() {
        List<MessageReaction> reactions = chan.getMessageById(message.getId()).complete().getReactions();
        List<User> vials = reactions.get(getVialIndex(reactions)).getUsers().complete();

        for (User i : vials) {
            if (!i.isBot()) {
                i.openPrivateChannel().queue((channel) -> {
                    channel.sendMessage(location).queue();
                });
            }
        }

        List<User> key = reactions.get(getKeyIndex(reactions)).getUsers().complete();
        chan.getMessageById(message.getId()).complete().getReactions().get(getKeyIndex(reactions)).removeReaction()
                .queue();
        for (User i : key) {
            if (!i.isBot()) {
                i.openPrivateChannel().queue((channel) -> {
                    channel.sendMessage(location).queue();
                });
                break;
            }
        }

    }

    public void setLocation(String location) {
        this.location = "Location is" + location.toUpperCase() + ". Please head to location.";
        sendVialsMessage();
        sendStaffMessage();
    }

    private int getKendoIndex(List<MessageReaction> reactions) {
        for (int i = 0; i < reactions.size(); i++) {
            if (reactions.get(i).getReactionEmote().getId().equals(Ref.kendoID))
                return i;
        }
        return 0;
    }

    private int getVialIndex(List<MessageReaction> reactions) {
        for (int i = 0; i < reactions.size(); i++) {
            if (reactions.get(i).getReactionEmote().getId().equals(Ref.vialID))
                return i;
        }
        return 0;
    }

    private int getKeyIndex(List<MessageReaction> reactions) {
        for (int i = 0; i < reactions.size(); i++) {
            if (reactions.get(i).getReactionEmote().getId().equals(Ref.keyID))
                return i;
        }
        return 0;
    }

    private void sendStaffMessage() {
        chan.getGuild().getTextChannelById(Ref.staffChat).sendMessage(location + " (If you want to joing the run)")
                .queue();
    }
}
