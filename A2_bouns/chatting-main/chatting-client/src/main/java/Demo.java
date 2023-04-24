
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;


/**
 * @author th
 * @description: TODO
 * @projectName hashdog
 * @date 2020/2/1620:48
 */
public class Demo extends Application {

    public static void main(String[] args) {
        Application.launch(Demo.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        /*
        System.out.println(new Date());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(new Date(System.currentTimeMillis()));
                System.out.println(new Date(scheduledExecutionTime()));
            }
        }, 2000);

         */



        /*
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Group Chat");
        dialog.setHeaderText(null);
        dialog.setContentText("Input the RoomCode\nPlease began with \"room\"");
        Optional<String> input = dialog.showAndWait();
        String getRoomCode = input.get();
        if(getRoomCode.startsWith("room")) {
            System.out.println("YES");
        }
        VBox father = new VBox(10);

        VBox vbox = new VBox(10);
        //多选按钮
        for (int i = 0; i < 10; i++) {
            CheckBox t = new CheckBox("c"+i);
            vbox.getChildren().add(t);
        }

        Button ok = new Button("Ok");

        ok.setOnAction(actionEvent -> {
            List<CheckBox> list = (List<CheckBox>) (Object) vbox.getChildren();
            final StringBuilder str = new StringBuilder("已选择:");
            list.forEach(item -> {
                if (item.isSelected()) {
                    str.append(item.getText() + ",");
                }
            });
            System.out.println(str);
        });


        father.getChildren().addAll(vbox,ok);
        father.setStyle("-fx-background-color: deepskyblue");
        father.setAlignment(Pos.CENTER);
        father.setPadding(new Insets(20, 20, 20, 20));
        Scene s = new Scene(father);
        primaryStage.setScene(s);
        primaryStage.setWidth(100);
        primaryStage.setTitle("hashdog");

        //设置窗口不可拉伸
        primaryStage.show();

        //当点击面板的时候,打印已选择的多选按钮

         */

    }

}
