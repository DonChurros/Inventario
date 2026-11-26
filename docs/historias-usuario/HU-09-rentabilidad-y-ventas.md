# HU-09 — Inspección de rentabilidad y ventas

**Como:** Jefe  
**Quiero:** una página en la interfaz dedicada únicamente a la inspección de la rentabilidad del negocio y las ganancias.  
**Para:** poder tomar mejores decisiones sobre qué hacer con la producción que vale la pena seguir haciendo y qué no.

## Criterios de aceptación

1. Solo el rol **Jefe** puede ver y consultar la página. El rol Administrador no tiene acceso a ella.
2. La página muestra el número de ventas con productos, las unidades vendidas y el valor bruto registrado por ventas.
3. La página presenta los productos ordenados por valor bruto vendido y muestra sus unidades vendidas y existencias actuales.
4. Si no existen ventas con productos, la página muestra un estado vacío en vez de cifras incorrectas.
5. La página aclara que los valores son ventas brutas históricas, incluyen ventas pendientes y no representan utilidad neta.

## Alcance actual del cálculo

Los ingresos brutos se calculan como `DetallePedido.Costo × DetallePedido.Cantidad`, agrupados por producto. El esquema actual no relaciona el consumo de insumos ni la mano de obra con cada producto, por lo que todavía no permite calcular la utilidad neta.
