package org.graniteds.tutorial.chat.client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.Producer;
import org.granite.client.messaging.TopicMessageListener;
import org.granite.client.messaging.channel.ChannelType;
import org.granite.client.messaging.events.TopicMessageEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.*;


public class ChatClient extends Application {

    /**
     * Main method which lauches the JavaFX application
     */
    public static void main(String[] args) {
        Application.launch(ChatClient.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // tag::client-setup[]
        Context context = new SimpleContextManager(new JavaFXApplication()).getContext(); // <1>
        final ServerSession serverSession = context.set(
                new ServerSession("/chat", "localhost", 8080)); // <2>
        serverSession.start(); // <3>

        final Consumer chatConsumer = serverSession.getConsumer(ChannelType.LONG_POLLING, "chatTopic", "room"); // <4>
        final Producer chatProducer = serverSession.getProducer(ChannelType.LONG_POLLING, "chatTopic", "room");
        // end::client-setup[]

        // tag::client-ui[]
        GridPane grid = new GridPane();
        grid.setMaxHeight(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 10, 25, 10));
        grid.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(50));

        Text title = new Text("Chat Example");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(title, 0, 0, 2, 1);

        final TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setText("");
        chatArea.setMinHeight(250);
        grid.add(chatArea, 0, 1, 2, 1);

        final TextField textField = new TextField();
        textField.setMinWidth(250);
        grid.add(textField, 0, 2);

        final Button sendButton = new Button("Send");
        sendButton.setMinWidth(50);
        grid.add(sendButton, 1, 2);

        Scene scene = new Scene(grid, 350, 350);
        stage.setTitle("GraniteDS Chat Tutorial");
        stage.setScene(scene);
        stage.show();

        textField.requestFocus();
        // end::client-ui[]

        // tag::client-consume[]
        chatConsumer.addMessageListener(new TopicMessageListener() {
            @Override
            public void onMessage(TopicMessageEvent event) {
                chatArea.appendText(event.getData() + "\n");
            }
        }); // <1>

        chatConsumer.subscribe().get(); // <2>
        // end::client-consume[]

        // tag::client-publish[]
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            private int messageNumber = 1;

            @Override
            public void handle(ActionEvent actionEvent) {
                chatProducer.publish("#" + (messageNumber++) + ": " + textField.getText()); // <1>
                textField.clear();
                textField.requestFocus();
            }
        });
        textField.setOnAction(sendButton.getOnAction());
        // end::client-publish[]

        // tag::client-close[]
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    serverSession.stop();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // end::client-close[]
    }
}