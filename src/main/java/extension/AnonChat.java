package extension;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import entities.Player;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.*;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.WebUtils;

import javax.swing.*;
import javax.swing.Timer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ExtensionInfo(
        Title = "AnonChat",
        Description = "Make Habbo don't see what you are typing!",
        Version = "1.4",
        Author = "Thauan"
)

public class AnonChat extends ExtensionForm {
    public static AnonChat RUNNING_INSTANCE;
    public static Stage primaryStage;
    public int habboId = -1;
    public boolean enabled = false;
    public List<Player> users = new LinkedList<>();
    public String habboName;
    public int habboIndex;
    public TextField anonKeyTextField;
    public CheckBox toggleCheckbox;
    public Label labelLog;
    public ListView<String> usersInChatListView;
    public Label labelStatus;
    public String host;
    public int roomId = -1;
    public Label labelLanguage;
    public ChoiceBox<String> languageChoiceBox;
//    public CheckBox noQuoteCheckbox;

    @Override
    protected void onStartConnection() {
    }

    @Override
    protected void onShow() {

        if(Objects.equals(this.host, "")) {
            WebUtils.codeToLangMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(o -> {
                        languageChoiceBox.getItems().add(o.getKey());
                    });
            languageChoiceBox.setValue("en");
        }else {
            languageChoiceBox.setVisible(false);
            labelLanguage.setVisible(false);
        }

        new Thread(() -> {
            sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
            sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 0));
            sendToServer(new HPacket("GetHeightMap", HMessage.Direction.TOSERVER));
            waitAFckingSec(1000);
            if(roomId != -1) {
                sendToServer(new HPacket("OpenFlatConnection", HMessage.Direction.TOSERVER, roomId, "", -1));
            }
        }).start();
    }

    Timer anonChatUsers = new Timer(5000, e -> {
        if (habboIndex != -1 && roomId != -1 && !Objects.equals(anonKeyTextField.getText(), "") && enabled) {
            try {
                JsonObject result = WebUtils.generateChat(anonKeyTextField.getText(), String.valueOf(habboIndex), roomId);

                JsonArray usersOnChat = result.get("users").getAsJsonArray();

                for (JsonElement user : usersOnChat) {
                    Player player = users.stream().filter(u -> Objects.equals(String.valueOf(u.getIndex()), user.getAsString())).findFirst().orElse(null);
                    if (player != null) {
                        player.setWithKey(true);
                        if (!usersInChatListView.getItems().contains(player.getPlayerName()))
                            usersInChatListView.getItems().add(player.getPlayerName());
                    }
                }

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    });

    // TODO: NEW FEATURE TO DON'T QUOTE
//    Timer noQuote = new Timer(500, e -> {
//
//    });


    @Override
    protected void initExtension() {
        RUNNING_INSTANCE = this;

        intercept(HMessage.Direction.TOSERVER, "OpenFlatConnection", hMessage -> {
            HPacket hPacket = hMessage.getPacket();
            roomId = hPacket.readInteger();
            usersInChatListView.getItems().clear();
        });

        onConnect((host, port, APIVersion, versionClient, client) -> {
            try {
                this.host = host.substring(5, 7);
            }catch (Exception err) {
                this.host = "";
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "RoomEntryInfo", hMessage -> {
            HPacket hPacket = hMessage.getPacket();
            roomId = hPacket.readInteger();
        });

        intercept(HMessage.Direction.TOCLIENT, "UserObject", hMessage -> {
            habboId = hMessage.getPacket().readInteger();
            habboName = hMessage.getPacket().readString();
        });

        intercept(HMessage.Direction.TOCLIENT, "Users", this::checkUsers);

        intercept(HMessage.Direction.TOCLIENT, "Chat", this::processIncomingChat);

        intercept(HMessage.Direction.TOCLIENT, "Shout", this::processIncomingChat);

        intercept(HMessage.Direction.TOCLIENT, "Whisper", this::processIncomingChat);

        intercept(HMessage.Direction.TOSERVER, "Chat", hMessage -> {
            processOutgoingChat(hMessage, "Chat");
        });

        intercept(HMessage.Direction.TOSERVER, "Shout", hMessage -> {
            processOutgoingChat(hMessage, "Shout");
        });

        intercept(HMessage.Direction.TOSERVER, "Whisper", hMessage -> {
            processOutgoingChat(hMessage, "Whisper");
        });

    }

    public void processIncomingChat(HMessage hMessage) {
        if (enabled) {
            HPacket hPacket = hMessage.getPacket();
            int userIndex = hPacket.readInteger();
            String userMessage = hPacket.readString(StandardCharsets.UTF_8);
            String expression = hPacket.toExpression();

            hMessage.setBlocked(true);
            if (userIndex != habboIndex) {

                new Thread(() -> {
                    Player player = users.stream().filter(u -> u.getIndex() == userIndex).findFirst().orElse(null);

                    String message;
                    HPacket newPacket = new HPacket(expression);

                    if (player != null && player.isWithKey()) {
                        try {
                            message = WebUtils.fetchMessage(anonKeyTextField.getText(), userMessage, roomId);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        newPacket.replaceString(10, message + " ª ANONCHAT");
                    }

                    sendToClient(newPacket);


                }).start();
            }
        }
    }

    public void processOutgoingChat(HMessage hMessage, String type) {
        if (enabled) {
            HPacket hPacket = hMessage.getPacket();
            String userMessage = hPacket.readString(StandardCharsets.UTF_8);
            int bubble = hPacket.readInteger();

            String expression = hPacket.toExpression();

            hMessage.setBlocked(true);
            new Thread(() -> {

                sendToClient(new HPacket(type, HMessage.Direction.TOCLIENT, habboIndex, userMessage + " ª ANONCHAT", 0, bubble, 0, -1));

                String fakeMessage = null;
                try {
                    fakeMessage = WebUtils.getRandomQuote(10, userMessage.length() <= 10 ? 30 : 90);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                try {
                    WebUtils.createMessage(anonKeyTextField.getText(), userMessage, fakeMessage, roomId);
                    HPacket newPacket = new HPacket(expression);
                    newPacket.replaceString(6, fakeMessage, StandardCharsets.UTF_8);
                    sendToServer(newPacket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }




            }).start();
        }
    }

    public void toggleStatus() {
        if (anonKeyTextField.getText().isEmpty()) {
            toggleCheckbox.setSelected(false);
            return;
        }

        if (toggleCheckbox.isSelected()) {
            enabled = true;
            Player player = users.stream().filter(u -> Objects.equals(u.getPlayerName(), habboName)).findFirst().orElse(null);
            if (player != null)
                player.setWithKey(true);
            anonChatUsers.start();
            anonKeyTextField.setDisable(true);
            Platform.runLater(() -> {
                labelStatus.setText("Online");
                labelStatus.setTextFill(Color.GREEN);
            });
        } else {
            anonKeyTextField.setDisable(false);
            enabled = false;
            Player player = users.stream().filter(u -> Objects.equals(u.getPlayerName(), habboName)).findFirst().orElse(null);
            if (player != null)
                player.setWithKey(false);
            anonChatUsers.stop();
            Platform.runLater(() -> {
                labelStatus.setText("Offline");
                labelStatus.setTextFill(Color.RED);
            });
        }
    }

    public void checkUsers(HMessage hMessage) {
        try {
            HEntity[] roomUsersList = HEntity.parse(hMessage.getPacket());
            for (HEntity hEntity : roomUsersList) {
                Player player = users.stream().filter(u -> u.getPlayerId() == hEntity.getId()).findFirst().orElse(null);
                if (hEntity.getId() == habboId) {
                    habboIndex = hEntity.getIndex();
                }else if (player != null) {
                    player.setPlayerId(hEntity.getId());
                    player.setIndex(hEntity.getIndex());
                    player.setPlayerName(hEntity.getName());
                } else {
                    Player user = new Player(hEntity.getId(), hEntity.getName(), hEntity.getIndex());
                    users.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void waitAFckingSec(int millisecActually) {
        try {
            Thread.sleep(millisecActually);
        } catch (InterruptedException ignored) {
        }
    }

}
