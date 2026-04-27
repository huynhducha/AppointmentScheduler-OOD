import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Trỏ vào màn hình Đăng nhập đầu tiên
        Parent root = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));

        primaryStage.setTitle("Đăng nhập Hệ thống Quản lý Lịch hẹn");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.setResizable(false); // Khóa kích thước màn hình login cho đẹp
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}