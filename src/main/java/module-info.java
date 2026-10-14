module com.desarrollo.proyectocrudg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.desarrollo.proyectocrudg3 to javafx.fxml;
    opens com.desarrollo.proyectocrudg3.controller to javafx.fxml;
    opens com.desarrollo.proyectocrudg3.model to javafx.base;

    exports com.desarrollo.proyectocrudg3;
}