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

        // Cố định luôn kích thước 400x400 để form không bị cắt xén
        Scene scene = new Scene(root, 400, 400);

        // ĐÂY CHÍNH LÀ DÒNG BẠN BỊ THIẾU NÈ:
        primaryStage.setScene(scene);

        primaryStage.setResizable(false); // Khóa kích thước màn hình login
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}