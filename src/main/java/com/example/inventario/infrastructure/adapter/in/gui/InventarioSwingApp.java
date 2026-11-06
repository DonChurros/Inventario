package com.example.inventario.infrastructure.adapter.in.gui;

import com.example.inventario.application.port.in.RegistrarProductoUseCase;
import com.example.inventario.application.port.in.RegistrarProductoUseCase.RegistrarCommand;
import com.example.inventario.domain.model.Producto;
import com.example.inventario.infrastructure.InventarioApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

public class InventarioSwingApp {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(InventarioApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run(args);
        RegistrarProductoUseCase registrarProducto = context.getBean(RegistrarProductoUseCase.class);

        SwingUtilities.invokeLater(() -> mostrarVentana(registrarProducto, context));
    }

    private static void mostrarVentana(
            RegistrarProductoUseCase registrarProducto,
            ConfigurableApplicationContext context
    ) {
        JFrame ventana = new JFrame("Inventario");
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setSize(520, 620);
        ventana.setLocationRelativeTo(null);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                context.close();
            }
        });

        JLabel titulo = new JLabel("Crear producto", JLabel.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));

        JTextField categoriaId = new JTextField();
        JTextField empleadoId = new JTextField();
        JTextField nombre = new JTextField();
        JTextField slug = new JTextField();
        JTextField descripcion = new JTextField();
        JTextField costo = new JTextField();
        JTextField imagenUrl = new JTextField();
        JTextField existencias = new JTextField("0");
        javax.swing.JCheckBox destacado = new javax.swing.JCheckBox("Producto destacado");

        JPanel formulario = new JPanel(new GridLayout(9, 2, 8, 10));
        formulario.add(new JLabel("ID de categoría:"));
        formulario.add(categoriaId);
        formulario.add(new JLabel("ID de empleado:"));
        formulario.add(empleadoId);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Slug:"));
        formulario.add(slug);
        formulario.add(new JLabel("Descripción:"));
        formulario.add(descripcion);
        formulario.add(new JLabel("Costo:"));
        formulario.add(costo);
        formulario.add(new JLabel("URL de imagen:"));
        formulario.add(imagenUrl);
        formulario.add(new JLabel("Existencias:"));
        formulario.add(existencias);
        formulario.add(new JLabel("Destacado:"));
        formulario.add(destacado);

        JButton botonCrear = new JButton("Crear producto");
        botonCrear.addActionListener(event -> crearProducto(
                ventana, registrarProducto, categoriaId, empleadoId, nombre, slug,
                descripcion, costo, imagenUrl, existencias, destacado, botonCrear
        ));

        JPanel contenido = new JPanel(new BorderLayout(12, 16));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        contenido.add(titulo, BorderLayout.NORTH);
        contenido.add(formulario, BorderLayout.CENTER);
        contenido.add(botonCrear, BorderLayout.SOUTH);

        ventana.setContentPane(contenido);
        ventana.setVisible(true);
    }

    private static void crearProducto(
            JFrame ventana,
            RegistrarProductoUseCase registrarProducto,
            JTextField categoriaId,
            JTextField empleadoId,
            JTextField nombre,
            JTextField slug,
            JTextField descripcion,
            JTextField costo,
            JTextField imagenUrl,
            JTextField existencias,
            javax.swing.JCheckBox destacado,
            JButton botonCrear
    ) {
        final RegistrarCommand command;
        try {
            String nombreProducto = nombre.getText().trim();
            String slugProducto = slug.getText().trim();
            if (nombreProducto.isEmpty() || slugProducto.isEmpty()) {
                throw new IllegalArgumentException("Completa el nombre y el slug.");
            }
            if (imagenUrl.getText().isBlank()) {
                throw new IllegalArgumentException("La URL de imagen es obligatoria.");
            }

            command = new RegistrarCommand(
                    Integer.parseInt(categoriaId.getText().trim()),
                    Integer.parseInt(empleadoId.getText().trim()),
                    nombreProducto,
                    slugProducto,
                    descripcion.getText().isBlank() ? null : descripcion.getText().trim(),
                    new BigDecimal(costo.getText().trim()),
                    imagenUrl.getText().trim(),
                    Integer.parseInt(existencias.getText().trim()),
                    destacado.isSelected()
            );
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    ventana,
                    "Los IDs y las existencias deben ser enteros; el costo debe ser decimal.",
                    "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    ventana, exception.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        botonCrear.setEnabled(false);
        new SwingWorker<Producto, Void>() {
            @Override
            protected Producto doInBackground() {
                return registrarProducto.registrar(command);
            }

            @Override
            protected void done() {
                botonCrear.setEnabled(true);
                try {
                    Producto producto = get();
                    JOptionPane.showMessageDialog(
                            ventana,
                            "Producto creado correctamente. ID: " + producto.id(),
                            "Producto creado",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    categoriaId.setText("");
                    empleadoId.setText("");
                    nombre.setText("");
                    slug.setText("");
                    descripcion.setText("");
                    costo.setText("");
                    imagenUrl.setText("");
                    existencias.setText("0");
                    destacado.setSelected(false);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    mostrarError(ventana, "Se interrumpió la creación del producto.");
                } catch (ExecutionException exception) {
                    Throwable causa = exception.getCause();
                    mostrarError(ventana, causa == null ? exception.getMessage() : causa.getMessage());
                }
            }
        }.execute();
    }

    private static void mostrarError(JFrame ventana, String mensaje) {
        JOptionPane.showMessageDialog(
                ventana,
                "No se pudo crear el producto: " + mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
