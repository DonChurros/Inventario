package com.example.inventario.infrastructure.adapter.in.web;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DetallePedidoStockTrigger implements Trigger {
    @Override
    public void init(
            Connection connection,
            String schemaName,
            String triggerName,
            String tableName,
            boolean before,
            int type
    ) throws SQLException {
    }

    @Override
    public void fire(Connection connection, Object[] oldRow, Object[] newRow) throws SQLException {
        int productoId = ((Number) newRow[1]).intValue();
        int cantidad = ((Number) newRow[2]).intValue();
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE Productos SET Existencias = Existencias - ? WHERE ProductoId = ?")) {
            statement.setInt(1, cantidad);
            statement.setInt(2, productoId);
            statement.executeUpdate();
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void remove() {
    }
}
