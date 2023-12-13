package extension;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.ThemedExtensionFormCreator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class AnonChatLauncher extends ThemedExtensionFormCreator {

    @Override
    public ExtensionForm createForm(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnonChat.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("AnonChat v" + AnonChat.class.getAnnotation(ExtensionInfo.class).Version());
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.getIcons().add(new Image("icon.png"));

        return loader.getController();
    }

    @Override
    protected String getTitle() {
        return "Market Sniper v" + AnonChat.class.getAnnotation(ExtensionInfo.class).Version();
    }

    @Override
    protected URL getFormResource() {
        return getClass().getResource("/AnonChat.fxml");
    }

    @Override
    protected void initialize(Stage primaryStage) {
        AnonChat.primaryStage = primaryStage;
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
    }

    public static void main(String[] args) {
        runExtensionForm(args, AnonChatLauncher.class);
    }
}