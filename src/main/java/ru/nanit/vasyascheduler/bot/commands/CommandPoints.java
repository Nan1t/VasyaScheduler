package ru.nanit.vasyascheduler.bot.commands;

import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.util.ImageUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.bot.CommandHandler;
import ru.nanit.vasyascheduler.bot.CommandSender;
import ru.nanit.vasyascheduler.data.chat.Media;
import ru.nanit.vasyascheduler.data.chat.Message;
import ru.nanit.vasyascheduler.data.user.SubscriberPoints;
import ru.nanit.vasyascheduler.services.PointsManager;
import ru.nanit.vasyascheduler.services.SubscribesManager;
import ru.nanit.vasyascheduler.services.conversion.XlsToImage;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class CommandPoints implements CommandHandler {

    private final Language lang;
    private final PointsManager pointsManager;
    private final SubscribesManager subscribes;
    private final Message help;

    public CommandPoints(Language lang, PointsManager pointsManager, SubscribesManager subscribes){
        this.lang = lang;
        this.pointsManager = pointsManager;
        this.subscribes = subscribes;
        this.help = new Message(lang.of("command.points.help"));
    }

    @Override
    public Message execute(CommandSender sender, String command, String... args) throws Exception {
        if(args.length == 0){
            SubscriberPoints subscriber = subscribes.getPointsSubscriber(sender.getBotType(), sender.getId());
            if(subscriber != null){
                return buildPointMessage(subscriber, null);
            }
            return new Message(lang.of("command.points.login.required"));
        }

        if(args.length == 4){
            String lastName = args[0];
            String firstName = args[1];
            String ptr = args[2];
            String password = args[3];

            SubscriberPoints subscriber = subscribes.getPointsSubscriber(sender.getBotType(), sender.getId());
            BufferedImage image;

            if(subscriber == null){
                subscriber = new SubscriberPoints(firstName, lastName, ptr, password);
                subscriber.setBotType(sender.getBotType());
                subscriber.setMessengerId(sender.getId());

                image = pointsManager.getPoints(subscriber.getStudent(), subscriber.getPassword());

                if(image != null){
                    subscribes.addPointsSubscriber(subscriber);
                } else {
                    return new Message(lang.of("command.points.login.error"));
                }
            } else {
                image = pointsManager.getPoints(subscriber.getStudent(), subscriber.getPassword());
            }

            return buildPointMessage(null, image);
        }

        return help;
    }

    private Message buildPointMessage(SubscriberPoints subscriber, BufferedImage image){
        Message message = new Message();

        try{
            if(image == null){
                image = pointsManager.getPoints(subscriber.getStudent(), subscriber.getPassword());
            }

            if(image != null){
                Media media = new Media(Media.Type.IMAGE);
                media.setFileName("points." + XlsToImage.getFileName());
                media.setStream(ImageUtil.createInputStream(image, XlsToImage.getFileName()));
                message.addMedia(media);
                return message;
            }
        } catch (IOException e){
            Logger.error("Cannot read points from web page: ", e);
        }

        message.setMessage(lang.of("command.points.error"));
        return message;
    }
}
