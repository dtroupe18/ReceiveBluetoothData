/**
 * Created by Dave on 8/1/17.
 */
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.*;
import java.io.*;

import com.fazecast.jSerialComm.*;

public class ConnectionWindow extends Application {

    private static boolean connectionEstablished = false;
    public static SerialPort userPort;
    static InputStream in;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Stage connectionWindow = new Stage();
        connectionWindow.setTitle("Connection Screen");

        connectionWindow.initModality(Modality.APPLICATION_MODAL);
        connectionWindow.setMaxWidth(350);

        Label directions = new Label();
        Label result = new Label();
        directions.setText("Press connect to establish a bluetooth connection");

        // create two buttons connect and retry
        Button connectButton = new Button("Connect");
        Scanner input = new Scanner(System.in);

        connectButton.setOnAction(e -> {
            SerialPort ports[] = SerialPort.getCommPorts();
            int i = 1;

            //User port selection
            System.out.println("COM Ports available on machine");

            //iterator to pass through port array
            for(SerialPort port : ports) {
                System.out.println(i++ + ": " + port.getSystemPortName()); //print windows com ports
            }

            System.out.println("Please select COM PORT: 'COM#'");
            SerialPort userPort = SerialPort.getCommPort(input.nextLine());

            //Initializing port
            userPort.openPort();
            if(userPort.isOpen()) {
                result.setText("Port initialized");
                System.out.println("Port initialized!");
                connectionEstablished = true;

                // launch a new window and draw the data based on the input stream
                drawLine();

                //timeout not needed for event based reading
                //userPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
            }

            else {
                System.out.println("Port not available");
                result.setText("Port not available");
                connectionEstablished = false;
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(directions, connectButton, result);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: red");

        Scene scene = new Scene(layout);
        connectionWindow.setScene(scene);
        connectionWindow.showAndWait();
    }

    private static void drawLine() {
        VBox canvasLayout = new VBox(10);
        Scene canvas = new Scene(canvasLayout, 500, 500);
        canvasLayout.setStyle("-fx-background-color: white");

        if (userPort.isOpen()) {
            InputStream in = userPort.getInputStream();
            Stage primeStage = new Stage();
            primeStage.setTitle("Robot Path");
            Group root = new Group();
            Scene scene = new Scene(root, 500, 500, Color.WHITE);
            int c;
            char currentChar;
            String filepath = "/Users/Dave/IdeaProjects/DrawLineFromDirectios/output.txt";

            // starting position
            double x = 50;
            double y = 250;

            try {
                Path path = new Path();
                path.getElements().add(new MoveTo(x, y));
                path.setStrokeWidth(1);
                path.setStroke(Color.BLACK);

                while ((c = in.read()) != -1) {
                    currentChar = (char) c;

                    switch (currentChar){
                        case 's':
                            x += 0.1;
                            path.getElements().add(new LineTo(x, y));
                            System.out.println(currentChar);
                            System.out.println(x + " " + y);
                            break;
                        case 'b':
                            x -= 0.1;
                            path.getElements().add(new LineTo(x, y));
                            System.out.println(currentChar);
                            System.out.println(x + " " + y);
                            break;
                        case 'l':
                            y -= 0.1;
                            path.getElements().add(new LineTo(x, y));
                            System.out.println(currentChar);
                            System.out.println(x + " " + y);
                            break;
                        case 'r':
                            y += 0.1;
                            path.getElements().add(new LineTo(x, y));
                            System.out.println(currentChar);
                            System.out.println(x + " " + y);
                            break;
                        default:
                            break;
                        }
                    }

                    root.getChildren().add(path);
                    primeStage.setScene(scene);
                    primeStage.show();

                    in.close();
                }
            catch (Exception e) {
                e.printStackTrace();
            }
            userPort.closePort();
        }
        else {
            System.out.println("Error: User port is not open...");
        }
    }
}
