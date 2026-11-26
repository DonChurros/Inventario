# HU-10 — Eliminar existencias de un producto

**Como:** Empleado de Producción  
**Quiero:** eliminar las existencias de un producto  
**Para:** corregir un stock registrado por error sin eliminar el producto del catálogo.

## Criterios de aceptación

1. El empleado de Producción puede eliminar las existencias de un producto con una acción individual en el catálogo.
2. Antes de eliminar las existencias, la interfaz muestra una confirmación con el producto y la cantidad que se pondrá en cero.
3. La operación actualiza `Productos.Existencias` a cero sin eliminar ni alterar el producto del catálogo.
4. El catálogo refleja inmediatamente que el producto quedó sin existencias.
5. Otros roles no pueden usar el endpoint para eliminar existencias.
