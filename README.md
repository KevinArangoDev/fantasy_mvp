#  Fantasy MVP

Aplicación web de **Fantasy Premier League** donde los usuarios se registran, arman su plantilla fichando jugadores y compiten en una tabla de posiciones según los puntos que suman sus futbolistas.

> Proyecto personal de portafolio, desarrollado de forma incremental por fases.

<!--Aquí agregare imágenes cuando arregle mejor la parte visual -->


##  Funcionalidades (Fase 1 – MVP completo)

- Registro e inicio de sesión de usuarios
- Fichaje de jugadores
- Persistencia de los datos en MySQL
- Menú de navegación condicional según si hay sesión iniciada (fragmentos de Thymeleaf)
- Cierre de sesión
- Cálculo de puntos con reglas de goles y asistencias
- Tabla de posiciones en `/ranking`

## 🧰 Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot |
| Vistas | Thymeleaf |
| Base de datos | MySQL |
| Build | Maven (wrapper incluido) |

## 🗺️ Hoja de ruta

- [x] **Fase 1:** MVP (registro, login, fichajes, puntos, ranking)
- [ ] **Fase 2:** [completar]
- [ ] **Fase 3:** [completar]
- [ ] **Fase 4:** [completar]

## ▶️ Cómo ejecutarlo

**Requisitos:** JDK 21 y MySQL instalados.

1. Clona el repositorio:
   ```bash
   git clone https://github.com/KevinArangoDev/fantasy_mvp.git
   cd fantasy_mvp
   ```
2. Crea la base de datos en MySQL:
   ```sql
   CREATE DATABASE [nombre_de_tu_bd];
   ```
3. Configura la conexión en `src/main/resources/application.properties` (usa tus propios valores y **no subas contraseñas reales al repositorio**):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/[nombre_de_tu_bd]
   spring.datasource.username=[tu_usuario]
   spring.datasource.password=[tu_contraseña]
   ```
4. Ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run
   ```
5. Abre `http://localhost:8080`.

## 📐 Reglas de puntos

| Evento | Puntos |
|---|---|
| Gol | [completar] |
| Asistencia | [completar] |


## 👤 Autor

**Kevin Velasquez** · Estudiante de Ingeniería de Sistemas, Universidad del Quindío
[GitHub](https://github.com/KevinArangoDev) · [LinkedIn:(https://www.linkedin.com/in/kevin-arango-454042372/)]
