package com.desarrollo.proyectocrudg3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import com.desarrollo.proyectocrudg3.database.ConexionDB;
import com.desarrollo.proyectocrudg3.model.Producto;
import com.desarrollo.proyectocrudg3.model.Categoria;
import com.desarrollo.proyectocrudg3.model.Marca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ProductosController {

    @FXML
    private TextField txtCodigo, txtNombre, txtDescripcion,
            txtPrecio, txtStock, txtBusqueda,
            txtPrecioMinimo, txtPrecioMaximo;

    @FXML
    private ComboBox<Categoria> cmbCategoria, cmbFiltroCategoria;

    @FXML
    private ComboBox<Marca> cmbMarca;

    @FXML
    private ComboBox<String> cmbOrden;

    @FXML
    private DatePicker dpFechaCreacion, dpDesde, dpHasta;

    @FXML
    private CheckBox chkExistencias;

    @FXML
    private TableView<Producto> tblProductos;

    @FXML
    private TableColumn<Producto, String> colCodigo, colNombre,
            colCategoria, colMarca;

    @FXML
    private TableColumn<Producto, Double> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colStock;

    @FXML
    private TableColumn<Producto, LocalDate> colFecha;

    private final ObservableList<Producto> listaProductos =
            FXCollections.observableArrayList();

    private final ObservableList<Categoria> listaCategorias =
            FXCollections.observableArrayList();

    private final ObservableList<Marca> listaMarcas =
            FXCollections.observableArrayList();

    private static final String CONSULTA =
            "SELECT p.*, c.nombre AS categoria, m.nombre AS marca "
                    + "FROM catalogos.productos p "
                    + "INNER JOIN catalogos.categorias c "
                    + "ON p.id_categoria = c.id_categoria "
                    + "INNER JOIN catalogos.marcas m "
                    + "ON p.id_marca = m.id_marca ";

    @FXML
    public void initialize() {
        configurarTableView();
        configurarSeleccionTableView();
        cargarCategorias();
        cargarMarcas();
        cargarOpcionesOrden();
        dpFechaCreacion.setDisable(true);
        cargarProductos();
    }

    private void configurarTableView() {
        colCodigo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCodigo()));

        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNombre()));

        colCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategoria()));

        colMarca.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMarca()));

        colPrecio.setCellValueFactory(data ->
                new SimpleDoubleProperty(
                        data.getValue().getPrecio()
                ).asObject());

        colStock.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getStock()
                ).asObject());

        colFecha.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getFechaCreacion()
                ));

        tblProductos.setItems(listaProductos);
    }

    private void configurarSeleccionTableView() {
        tblProductos.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        cargarProductoEnFormulario(newValue);
                    }
                });
    }

    private void cargarProductoEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(
                p.getDescripcion() == null ? "" : p.getDescripcion()
        );
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        dpFechaCreacion.setValue(p.getFechaCreacion());

        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);

        for (Categoria categoria : listaCategorias) {
            if (categoria.getIdCategoria() == p.getIdCategoria()) {
                cmbCategoria.setValue(categoria);
                break;
            }
        }

        for (Marca marca : listaMarcas) {
            if (marca.getIdMarca() == p.getIdMarca()) {
                cmbMarca.setValue(marca);
                break;
            }
        }
    }

    private void cargarCategorias() {
        String sql =
                "SELECT DISTINCT c.id_categoria, c.nombre "
                        + "FROM catalogos.categorias c "
                        + "INNER JOIN catalogos.productos p "
                        + "ON c.id_categoria = p.id_categoria "
                        + "WHERE c.estado = 1 ORDER BY c.nombre";

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ObservableList<Categoria> resultados =
                    FXCollections.observableArrayList();

            while (rs.next()) {
                resultados.add(new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre")
                ));
            }

            listaCategorias.setAll(resultados);

            cmbCategoria.setItems(
                    FXCollections.observableArrayList(listaCategorias)
            );
            cmbFiltroCategoria.setItems(
                    FXCollections.observableArrayList(listaCategorias)
            );

        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudieron cargar las categorías.", e
            );
        }
    }

    private void cargarMarcas() {
        String sql =
                "SELECT DISTINCT m.id_marca, m.nombre "
                        + "FROM catalogos.marcas m "
                        + "INNER JOIN catalogos.productos p "
                        + "ON m.id_marca = p.id_marca "
                        + "WHERE m.estado = 1 ORDER BY m.nombre";

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ObservableList<Marca> resultados =
                    FXCollections.observableArrayList();

            while (rs.next()) {
                resultados.add(new Marca(
                        rs.getInt("id_marca"),
                        rs.getString("nombre")
                ));
            }

            listaMarcas.setAll(resultados);

            cmbMarca.setItems(
                    FXCollections.observableArrayList(listaMarcas)
            );

        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudieron cargar las marcas.", e
            );
        }
    }

    private void cargarOpcionesOrden() {
        cmbOrden.setItems(FXCollections.observableArrayList(
                "Nombre",
                "Precio menor a mayor",
                "Precio mayor a menor",
                "Fecha más reciente",
                "Fecha más antigua",
                "Stock mayor a menor",
                "Stock menor a mayor"
        ));

        cmbOrden.setValue("Nombre");
    }

    private Producto crearProductoDesdeResultSet(ResultSet rs)
            throws SQLException {

        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getDate("fecha_creacion").toLocalDate(),
                rs.getInt("id_categoria"),
                rs.getString("categoria"),
                rs.getInt("id_marca"),
                rs.getString("marca"),
                rs.getInt("estado")
        );
    }

    private void llenarTabla(ResultSet rs) throws SQLException {
        ObservableList<Producto> resultados =
                FXCollections.observableArrayList();

        while (rs.next()) {
            resultados.add(crearProductoDesdeResultSet(rs));
        }

        listaProductos.setAll(resultados);
    }

    public void cargarProductos() {
        String sql = CONSULTA + "ORDER BY p.nombre, p.id_producto";

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            llenarTabla(rs);

        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudieron cargar los productos.", e
            );
        }
    }

    private boolean validarCampos() {
        if (txtCodigo.getText().trim().isEmpty()
                || txtNombre.getText().trim().isEmpty()
                || cmbCategoria.getValue() == null
                || cmbMarca.getValue() == null) {

            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos",
                    "Completa código, nombre, categoría y marca.");
            return false;
        }

        if (txtCodigo.getText().trim().length() > 20
                || txtNombre.getText().trim().length() > 80
                || txtDescripcion.getText().trim().length() > 200) {

            mostrarAlerta(Alert.AlertType.WARNING, "Texto demasiado largo",
                    "Código: máximo 20 caracteres.\n"
                            + "Nombre: máximo 80.\nDescripción: máximo 200.");
            return false;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            Integer.parseInt(txtStock.getText().trim());

            if (!Double.isFinite(precio)
                    || precio < 0.01 || precio > 99999999.99) {

                mostrarAlerta(Alert.AlertType.WARNING, "Precio inválido",
                        "El precio debe estar entre 0.01 y 99999999.99.");
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Formato inválido",
                    "Precio debe ser decimal y stock un número entero.");
            return false;
        }
    }

    @FXML
    public void guardarProducto() {
        guardar(false);
    }

    @FXML
    public void actualizarProducto() {
        guardar(true);
    }

    private void guardar(boolean actualizar) {
        Producto seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (actualizar && seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Selecciona un producto para actualizar.");
            return;
        }

        if (!validarCampos()) return;

        String sql = actualizar
                ? "UPDATE catalogos.productos SET codigo=?, nombre=?, "
                + "descripcion=?, precio=?, stock=?, id_categoria=?, "
                + "id_marca=? WHERE id_producto=?"
                : "INSERT INTO catalogos.productos "
                + "(codigo, nombre, descripcion, precio, stock, "
                + "id_categoria, id_marca) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, txtCodigo.getText().trim());
            ps.setString(2, txtNombre.getText().trim());

            String descripcion = txtDescripcion.getText().trim();
            ps.setString(3, descripcion.isEmpty() ? null : descripcion);

            ps.setDouble(4, Double.parseDouble(txtPrecio.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            ps.setInt(6, cmbCategoria.getValue().getIdCategoria());
            ps.setInt(7, cmbMarca.getValue().getIdMarca());

            if (actualizar) {
                ps.setInt(8, seleccionado.getIdProducto());
            }

            int filas = ps.executeUpdate();

            mostrarAlerta(
                    filas == 0
                            ? Alert.AlertType.WARNING
                            : Alert.AlertType.INFORMATION,
                    filas == 0 ? "Producto no encontrado" : "Éxito",
                    filas == 0 ? "El producto ya no existe."
                            : actualizar
                            ? "Producto actualizado."
                            : "Producto registrado."
            );

            limpiarFormulario();
            buscarProductos();

        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudo guardar el producto.", e
            );
        }
    }

    @FXML
    public void eliminarProducto() {
        Producto seleccionado =
                tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Selecciona un producto para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar producto");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
                "¿Eliminar permanentemente "
                        + seleccionado.getNombre() + "?"
        );

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        String sql =
                "DELETE FROM catalogos.productos WHERE id_producto = ?";

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, seleccionado.getIdProducto());
            int filas = ps.executeUpdate();

            mostrarAlerta(
                    filas == 0
                            ? Alert.AlertType.WARNING
                            : Alert.AlertType.INFORMATION,
                    filas == 0 ? "Producto no encontrado" : "Éxito",
                    filas == 0
                            ? "El producto ya no existe."
                            : "Producto eliminado."
            );

            limpiarFormulario();
            cargarCategorias();
            cargarMarcas();
            buscarProductos();

        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudo eliminar el producto.", e
            );
        }
    }

    @FXML
    public void buscarProductos() {
        ObservableList<String> valores =
                FXCollections.observableArrayList();

        String sql = CONSULTA
                + "WHERE (p.codigo ILIKE ? OR p.nombre ILIKE ?) ";

        String texto = "%" + txtBusqueda.getText().trim() + "%";
        valores.add(texto);
        valores.add(texto);

        String minimo = txtPrecioMinimo.getText().trim();
        String maximo = txtPrecioMaximo.getText().trim();

        try {
            if (!minimo.isEmpty()
                    && !Double.isFinite(Double.parseDouble(minimo))) {
                throw new NumberFormatException();
            }

            if (!maximo.isEmpty()
                    && !Double.isFinite(Double.parseDouble(maximo))) {
                throw new NumberFormatException();
            }

            if (!minimo.isEmpty() && !maximo.isEmpty()
                    && Double.parseDouble(minimo)
                    > Double.parseDouble(maximo)) {

                mostrarAlerta(Alert.AlertType.WARNING, "Rango inválido",
                        "El precio mínimo no puede superar al máximo.");
                return;
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Filtro inválido",
                    "Los precios de búsqueda deben ser números.");
            return;
        }

        if (dpDesde.getValue() != null
                && dpHasta.getValue() != null
                && dpDesde.getValue().isAfter(dpHasta.getValue())) {

            mostrarAlerta(Alert.AlertType.WARNING, "Rango inválido",
                    "La fecha desde no puede ser posterior a hasta.");
            return;
        }

        if (cmbFiltroCategoria.getValue() != null) {
            sql += "AND p.id_categoria = CAST(? AS INTEGER) ";
            valores.add(String.valueOf(
                    cmbFiltroCategoria.getValue().getIdCategoria()
            ));
        }

        if (dpDesde.getValue() != null) {
            sql += "AND p.fecha_creacion >= CAST(? AS DATE) ";
            valores.add(dpDesde.getValue().toString());
        }

        if (dpHasta.getValue() != null) {
            sql += "AND p.fecha_creacion <= CAST(? AS DATE) ";
            valores.add(dpHasta.getValue().toString());
        }

        if (!minimo.isEmpty()) {
            sql += "AND p.precio >= CAST(? AS NUMERIC) ";
            valores.add(minimo);
        }

        if (!maximo.isEmpty()) {
            sql += "AND p.precio <= CAST(? AS NUMERIC) ";
            valores.add(maximo);
        }

        if (chkExistencias.isSelected()) {
            sql += "AND p.stock > 0 ";
        }

        String orden = cmbOrden.getValue();
        String columna = "p.nombre";

        if ("Precio menor a mayor".equals(orden)) {
            columna = "p.precio ASC";
        } else if ("Precio mayor a menor".equals(orden)) {
            columna = "p.precio DESC";
        } else if ("Fecha más reciente".equals(orden)) {
            columna = "p.fecha_creacion DESC";
        } else if ("Fecha más antigua".equals(orden)) {
            columna = "p.fecha_creacion ASC";
        } else if ("Stock mayor a menor".equals(orden)) {
            columna = "p.stock DESC";
        } else if ("Stock menor a mayor".equals(orden)) {
            columna = "p.stock ASC";
        }

        sql += "ORDER BY " + columna + ", p.id_producto";

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            for (int i = 0; i < valores.size(); i++) {
                ps.setString(i + 1, valores.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                llenarTabla(rs);
            }

        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudieron buscar los productos.", e
            );
        }
    }

    @FXML
    public void limpiarFormulario() {
        tblProductos.getSelectionModel().clearSelection();

        txtCodigo.clear();
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        txtStock.clear();

        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        dpFechaCreacion.setValue(null);
        txtCodigo.requestFocus();
    }

    @FXML
    public void limpiarFiltros() {
        txtBusqueda.clear();
        txtPrecioMinimo.clear();
        txtPrecioMaximo.clear();

        cmbFiltroCategoria.setValue(null);
        cmbOrden.setValue("Nombre");
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        chkExistencias.setSelected(false);

        cargarProductos();
    }

    private void mostrarErrorBaseDatos(String mensaje, SQLException e) {
        mostrarAlerta(
                Alert.AlertType.ERROR,
                "Error de base de datos",
                mensaje + "\n" + e.getMessage()
        );
    }

    private void mostrarAlerta(
            Alert.AlertType tipo, String titulo, String mensaje) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}