module com.example.java_skat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.java_skat to javafx.fxml;
    exports com.example.java_skat;
}