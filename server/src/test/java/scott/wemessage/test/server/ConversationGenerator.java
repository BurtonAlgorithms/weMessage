package scott.wemessage.test.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import scott.wemessage.commons.connection.json.message.JSONAttachment;
import scott.wemessage.commons.connection.json.message.JSONChat;
import scott.wemessage.commons.connection.json.message.JSONMessage;
import scott.wemessage.commons.connection.security.EncryptedText;
import scott.wemessage.commons.crypto.AESCrypto;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.server.connection.Device;

public final class ConversationGenerator {

    private final String handle = "wemessage.flash@gmail.com";

    private HashMap<String, String> messageMap = new HashMap<>();
    private ArrayList<String> contacts = new ArrayList<>();
    private ArrayList<JSONChat> chats = new ArrayList<>();
    private ArrayList<JSONMessage> messages = new ArrayList<>();

    public ConversationGenerator(){
        generate();
    }

    public void sendConversations(Device device){
        for (JSONMessage message : messages){
            device.sendOutgoingMessage(message);
        }
    }

    private void generate(){
        contacts.add("david-smith@mail.com");
        contacts.add("mary-smith@mail.com");
        contacts.add("rob-smith@mail.com");
        contacts.add("bob-smith@mail.com");
        contacts.add("maria-smith@mail.com");
        contacts.add("emma-smith@mail.com");
        contacts.add("david-garcia@mail.com");
        contacts.add("richard-miller@mail.com");
        contacts.add("mike-perez@mail.com");
        contacts.add("jennifer-gonzalez@mail.com");
        contacts.add("linda-thomas@mail.com");

        ArrayList<String> familyChat = new ArrayList<>();
        ArrayList<String> siblingsChat = new ArrayList<>();
        ArrayList<String> unnamedChat = new ArrayList<>();
        ArrayList<String> friendsChat = new ArrayList<>();

        familyChat.add(contacts.get(0));
        familyChat.add(contacts.get(1));
        familyChat.add(contacts.get(2));
        familyChat.add(contacts.get(3));
        familyChat.add(contacts.get(4));
        familyChat.add(contacts.get(5));

        siblingsChat.add(contacts.get(2));
        siblingsChat.add(contacts.get(3));
        siblingsChat.add(contacts.get(4));
        siblingsChat.add(contacts.get(5));

        unnamedChat.add(contacts.get(6));
        unnamedChat.add(contacts.get(7));
        unnamedChat.add(contacts.get(8));
        unnamedChat.add(contacts.get(9));
        unnamedChat.add(contacts.get(10));

        friendsChat.add(contacts.get(4));
        friendsChat.add(contacts.get(5));
        friendsChat.add(contacts.get(6));
        friendsChat.add(contacts.get(8));
        friendsChat.add(contacts.get(9));
        friendsChat.add(contacts.get(7));

        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "The Family", familyChat));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "My Siblings", siblingsChat));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", unnamedChat));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "Schoolmates", friendsChat));

        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", createListFromSingle(contacts.get(0))));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", createListFromSingle(contacts.get(3))));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", createListFromSingle(contacts.get(2))));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", createListFromSingle(contacts.get(4))));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", createListFromSingle(contacts.get(8))));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", createListFromSingle(contacts.get(9))));
        chats.add(new JSONChat(UUID.randomUUID().toString(), "Mac-" + UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", createListFromSingle(contacts.get(6))));

        generateMessages(0);
        generateMessages(1);
        generateMessages(2);
        generateMessages(3);
        generateMessages(4);
        generateMessages(5);
        generateMessages(6);
        generateMessages(7);
        generateMessages(8);
        generateMessages(9);
        generateMessages(10);
    }

    private void  generateMessages(int chatId){
        List<String> participants = chats.get(chatId).getParticipants();
        String otherHandle = participants.get(0);

        if (chatId == 0){
            messageMap.put("When's our next vacation?", getRandom(participants));
        }else if (chatId == 1){
            messageMap.put("Do you guys want to go to the mall?", handle);
        }else if (chatId == 2){
            messageMap.put("See you guys soon!", getRandom(participants));
        }else if (chatId == 3){
            messageMap.put("Hey guys have you tried Mrs. Linda's new cookies?", handle);
            messageMap.put("Yea! They're amazing!", getRandom(participants));
            messageMap.put("One of the most delicious things I have ever eaten.", getRandom(participants));
            messageMap.put("Truly something to die for", getRandom(participants));
            messageMap.put("They are really inexpensive too! Only a dollar each.", handle);
            messageMap.put("I know right, what a sweet deal!", getRandom(participants));
            messageMap.put("I'll never eat a cookie from another company again", getRandom(participants));
        }else if (chatId == 4){
            messageMap.put("Hey mom, what am I having for dinner?", handle);
            messageMap.put("Macaroni and cheese", otherHandle);
            messageMap.put("What? Come on, I was thinking of stake", handle);
            messageMap.put("No not tonight, maybe tomorrow", otherHandle);
            messageMap.put("Oh ok, sweet!", handle);
            messageMap.put("How was school?", otherHandle);
            messageMap.put("It was fine how was work?", handle);
            messageMap.put("It went well", otherHandle);
        }else if (chatId == 5){
            messageMap.put("I am at the store", otherHandle);
        }else if (chatId == 6){
            messageMap.put("I am coming in one minute", handle);
        }else if (chatId == 7){
            messageMap.put("When will you be there?", otherHandle);
        }else if (chatId == 8){
            messageMap.put("Can't wait to see you when you get back from college!", handle);
        }else if (chatId == 9){
            messageMap.put("I am really excited for that new video game!", otherHandle);
        }else if (chatId == 10){
            messageMap.put("What was the homework for tonight?", handle);
        }

        messages.addAll(parseFromMap(chatId));
    }

    private ArrayList<JSONMessage> parseFromMap(int chatId){
        ArrayList<JSONMessage> messages = new ArrayList<>();

        int date = DateUtils.convertDateTo2001Time(Calendar.getInstance().getTime());
        int minVal = 60;
        int secVal = 1;

        try {
            for (String s : messageMap.keySet()) {
                String key = AESCrypto.keysToString(AESCrypto.generateKeys());

                JSONMessage jsonMessage = new JSONMessage(
                        UUID.randomUUID().toString(),
                        chats.get(chatId),
                        messageMap.get(s),
                        new ArrayList<JSONAttachment>(),
                        new EncryptedText(AESCrypto.encryptString(s, key), key),
                        date,
                        date + secVal,
                        date + (secVal * 10),
                        false,
                        true,
                        true,
                        true,
                        true,
                        messageMap.get(s).equals(handle)
                );
                messages.add(jsonMessage);
                date = date + minVal;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        messageMap.clear();

        return messages;
    }

    private ArrayList<String> createListFromSingle(String string){
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(string);

        return arrayList;
    }

    private String getRandom(List<String> list){
        return list.get(new Random().nextInt(list.size()));
    }
}