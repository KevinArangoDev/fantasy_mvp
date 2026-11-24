# Fantasy Premier League

![CI](https://github.com/KevinArangoDev/fantasy_mvp/actions/workflows/ci.yml/badge.svg)

Aplicación web de fantasy football de la Premier League. Cada usuario arma su equipo dentro de un presupuesto y reglas de plantilla, y suma puntos según el rendimiento real de sus jugadores, que se importa desde una API pública de fútbol.

Es un proyecto de portafolio hecho para practicar desarrollo backend con Java y Spring Boot de punta a punta: modelado de datos, reglas de negocio, seguridad, API REST, pruebas automáticas, Docker e integración continua.

<!-- Aca agregare imagenes cuando mejore la parte visual de la app -->


## Qué hace

- Registro e inicio de sesión con contraseñas cifradas y roles `USER` / `ADMIN`.
- Catálogo de 543 jugadores reales de la Premier, importados desde football-data.org y filtrables por equipo y posición.
- Armado de equipo con reglas de juego: presupuesto, tamaño de plantilla, topes por posición y por club real.
- Venta de jugadores conservando los puntos ya ganados.
- Puntos automáticos por goles y asistencias, con una tarea programada opcional.
- Ranking general público.
- API REST y un panel en JavaScript que la consume (`/app`).

## Stack

| Área | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Validation, Security) |
| Base de datos | MySQL 8.4 |
| Vistas | Thymeleaf y JavaScript (`fetch`) |
| Pruebas | JUnit 5, Mockito, Spring MockMvc y Spring Security Test |
| Infraestructura | Docker, Docker Compose, GitHub Actions |
| Datos externos | [football-data.org](https://www.football-data.org) (plan gratuito) |

## Cómo ejecutarlo con Docker

Requisitos: Docker Desktop y un token gratuito de football-data.org.

1. Clona el repositorio y crea tu archivo de variables:

   ```bash
   git clone https://github.com/KevinArangoDev/fantasy_mvp.git
   cd fantasy_mvp
   cp .env.example .env
   ```

   En Windows PowerShell: `Copy-Item .env.example .env`. Edita `.env` con tu contraseña para MySQL y tu token.

2. Levanta la aplicación y la base de datos:

   ```bash
   docker compose up --build
   ```

3. Abre `http://localhost:8080/registro` y crea tu cuenta.

4. Conviértela en administradora (el primer usuario tiene `id = 1`):

   ```bash
   docker compose exec db mysql -uroot -p fantasy_mvp -e "UPDATE usuarios SET rol='ADMIN' WHERE id=1;"
   ```

5. Importa los jugadores y fija la línea base de puntos:

   ```bash
   curl -u tu-email:tu-contraseña -X POST http://localhost:8080/api/admin/importar-jugadores
   curl -u tu-email:tu-contraseña -X POST "http://localhost:8080/api/admin/sincronizar-puntos?lineaBase=true"
   ```

Después inicia sesión y entra a `/jugadores` o `/app`. Para detener todo, `docker compose down` (los datos se conservan en un volumen).

## Cómo ejecutarlo sin Docker

Requisitos: Java 21 y MySQL en local.

1. Copia `src/main/resources/application.properties.example` a `application.properties` y completa tus credenciales y tu token.
2. Ejecuta:

   ```bash
   ./mvnw spring-boot:run
   ```

   En Windows: `.\mvnw.cmd spring-boot:run`.

## Pruebas

```bash
./mvnw test
```

Son 26 pruebas, y también se ejecutan en cada `push` con GitHub Actions:

- **11 unitarias** de `FichajeService` (JUnit y Mockito): cada regla de fichaje, los casos límite del presupuesto y que vender no borra los puntos.
- **15 de seguridad web** (`@WebMvcTest` con la configuración real): rutas públicas, `401` en la API sin sesión, `403` por rol, protección CSRF, mensajes de error de las reglas y que la API no exponga la contraseña.

## Reglas del juego

| Regla | Valor |
|---|---|
| Presupuesto | 100 |
| Plantilla máxima | 15 jugadores |
| Topes por posición | 2 porteros, 5 defensas, 5 mediocampistas, 3 delanteros |
| Máximo por club real | 3 jugadores |
| Duplicados | No se puede fichar dos veces al mismo jugador |
| Venta | Permitida; los puntos ganados se conservan |

**Puntos:** gol de delantero o mediocampista 4, gol de defensa o portero 6, asistencia 3, portería en cero (defensa o portero) 4, tarjeta amarilla -1.

Los precios son valores base por posición (5, 5, 7 y 8), no valores de mercado.

## API

| Acceso | Método y ruta | Descripción |
|---|---|---|
| Público | `GET /api/jugadores`, `GET /api/jugadores/{id}` | Catálogo de jugadores |
| Público | `GET /api/ranking` | Ranking general |
| Con sesión | `GET /api/me` | Usuario actual |
| Con sesión | `GET /api/mi-equipo` | Mi equipo, puntos y presupuesto restante |
| Con sesión | `POST /api/equipo/fichar` | Fichar un jugador (`{"jugadorId": 1}`) |
| Con sesión | `DELETE /api/equipo/jugadores/{id}` | Vender un jugador |
| Administrador | `POST`, `PUT`, `DELETE /api/jugadores...` | Gestión del catálogo |
| Administrador | `POST /api/admin/importar-jugadores` | Importar jugadores desde la API externa |
| Administrador | `POST /api/admin/sincronizar-puntos` | Sincronizar puntos (`?lineaBase=true` para fijar la base) |

Los errores de las reglas devuelven `409` con un mensaje, por ejemplo `{"error": "Ya tienes a ese jugador"}`.

## Decisiones de diseño

- **Reglas en un solo servicio.** `FichajeService` lo usan tanto las páginas web como la API, así las reglas no pueden diferir entre una y otra.
- **Vender no borra.** Cada fichaje es una fila con un indicador `activo`. Al vender se desactiva, así el usuario conserva los puntos ganados y no recibe los de partidos posteriores.
- **Puntos por diferencia.** La API externa entrega totales de la temporada. Se guarda una línea base por jugador y en cada sincronización solo se reparte lo nuevo, de modo que sincronizar dos veces seguidas no cuenta dos veces el mismo gol.
- **La API no expone entidades.** Se responde con DTOs para no filtrar datos sensibles como el hash de la contraseña.
- **Registro transaccional y a prueba de asignación masiva.** Se usa un objeto de formulario con validaciones, y el rol lo fija el servidor.
- **Secretos fuera del código.** Contraseñas y tokens se leen de variables de entorno (`.env` está ignorado por Git y por Docker).

## Seguridad

Contraseñas con BCrypt, roles, protección CSRF en los formularios y en el panel, sesión con cookie, respuestas `401` en la API y validación de datos en el servidor.

## Limitaciones conocidas

- El plan gratuito de football-data.org solo da goles y asistencias (en la lista de goleadores) y con retraso. Tarjetas y portería en cero se cargan a mano en `/admin/puntos`.
- Dos goleadores de la API no tienen posición y no puntúan.
- Las peticiones con credenciales `Basic` se eximen de CSRF para poder administrar desde la línea de comandos. Es adecuado en local, pero conviene revisarlo antes de un despliegue público.
- La comprobación de duplicados no es atómica ante dos peticiones simultáneas; haría falta una restricción única en la base de datos.

## Próximos pasos

- Despliegue público.
- Frontend con React sobre la misma API.
- Precios dinámicos según rendimiento.
- Pruebas de integración con base de datos real (Testcontainers).

## 👤 Autor

**Kevin Velasquez** · Estudiante de Ingeniería de Sistemas, Universidad del Quindío
[GitHub](https://github.com/KevinArangoDev) · [LinkedIn:(https://www.linkedin.com/in/kevin-arango-454042372/)]
