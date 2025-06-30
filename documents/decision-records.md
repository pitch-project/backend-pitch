# DR-001: Elección de Framework Backend – Spring Boot

**Fecha:** 2025-06-30

---

## Decisión
Adoptar **Spring Boot** como framework principal para el desarrollo de microservicios backend.

---

## Contexto
- Arquitectura basada en microservicios (user-, customer-, payment-, audit-service)
- Equipo con experiencia en Java y Spring Framework
- Necesidad de rápida productividad, arranque sencillo y convenciones robustas
- Integración con ecosistema Spring (Security, Data, Cloud, Kafka)
- Requisitos de escalabilidad, testeo y despliegue continuo
- Contenerizacion: Docker

---

## Justificación
- **Arranque Rápido (“Convention over Configuration”)**: Dependencias preconfiguradas y autoconfiguración reducen tiempo de setup.
- **Ecosistema Madura**: Módulos oficiales para seguridad (Spring Security + JWT), acceso a datos (Spring Data JPA/MongoDB), mensajería (Spring Kafka) y configuración central (Spring Cloud Config).
- **Productividad del Equipo**: Curva de aprendizaje baja para desarrolladores Java; abundante documentación y comunidad activa.
- **Integración con Spring Cloud**: Facilidad para circuit breakers (Resilience4j), descubrimiento de servicios (Eureka), gateway (Spring Cloud Gateway).
- **Testing Simplificado**: Soporte nativo para tests de unidad y de integración (Spring Test, @WebMvcTest, Embedded Kafka).
- **Despliegue en Kubernetes**: Imágenes ligeras con Spring Native y compatibilidad con GraalVM para startup y footprint reducidos.

---

## Consecuencias

### Positivas
- Desarrollo acelerado y estándar de código homogéneo.
- Fácil integración de cross-cutting concerns (logging, métricas, seguridad).
- Amplio soporte de herramientas de monitoreo (Actuator, Micrometer).
- Alta compatibilidad con plataformas cloud y PaaS.

### Negativas
- Tamaño base de la imagen Docker relativamente mayor.
- Posible sobrecarga de autoconfiguración si no se ajusta adecuadamente.
- Requiere atención al tuning de parámetros para optimizar uso de memoria.

---

## Especificaciones Técnicas

```yaml
Framework: Spring Boot 3.5.3  
Lenguaje: Java 17  
Herramienta de build: Maven 3.9.9
Parent POM: 
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.3</version>
Dependencias clave:
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-data-mongodb
  - spring-boot-starter-security
  - spring-boot-starter-oauth2-resource-server (JWT)
  - spring-kafka
  - spring-cloud-starter-config
  - spring-cloud-starter-netflix-eureka-client
  - resilience4j-spring-boot3
Plugins Maven:
  - spring-boot-maven-plugin
Configuración recomendada:
  - Actuator endpoints habilitados: health, metrics, prometheus  
  - `management.endpoint.*.exposure.include=health,metrics,prometheus`  
  - `spring.profiles.active` por entorno: dev, staging, prod  
```

# DR-002: Arquitectura Basada en Microservicios

**Fecha:** 2025-06-30

---

## Decisión
Adoptar un enfoque de microservicios con cuatro servicios iniciales:
- **user-service**
- **customer-service**
- **payment-service**
- **audit-service**

---

## Contexto
- Plataforma de reserva y gestión de canchas deportivas
- Necesidad de escalabilidad independiente por módulo
- Equipos distribuidos con responsabilidades claras
- Requisitos de alta disponibilidad, seguridad y trazabilidad
- Integración con múltiples pasarelas de pago
- Necesidad de auditar todas las operaciones críticas

---

## Descripción de Servicios

1. **user-service**
    - Gestión de usuarios (registro, login, perfil)
    - Emisión y validación de JWT para autenticación y autorización
    - Persistencia en base de datos relacional (PostgreSQL)

2. **customer-service**
    - Lógica de negocio principal:
        - Gestión de reservas de canchas
        - Administración de inventario de canchas por propietario
    - Exposición de APIs REST/GraphQL para front-end
    - Persistencia en MongoDB para datos de reserva y canchas

3. **payment-service**
    - Orquestación de pasarelas de pago (PayU, etc.)
    - Modelado de transacciones, estados y reembolsos
    - Comunicación con customer-service para validación de reserva antes de pago

4. **audit-service**
    - Recepción de eventos de auditoría desde otros servicios (via Kafka)
    - Normalización y almacenamiento de logs de actividad
    - Persistencia en MongoDB (colección `audit_logs`)
    - Exposición de API para generación de reportes de auditoría

---

## Justificación
- **Desacoplamiento:** Cada dominio evoluciona y escala de manera independiente.
- **Resiliencia:** Fallos en un servicio no afectan al resto; uso de circuit breakers.
- **Autonomía de despliegue:** Equipos pueden desplegar sin coordinar el monolito completo.
- **Trazabilidad:** audit-service centraliza logs, cumple requisitos de compliance.
- **Seguridad:** user-service maneja JWT; otros servicios confían solo en tokens válidos.
- **Flexibilidad:** payment-service puede añadir nuevas pasarelas sin tocar otros servicios.

---

## Consecuencias

### Positivas
- Time-to-market más rápido para nuevas funcionalidades en dominios aislados.
- Escalado horizontal focalizado en servicios con mayor carga (p.ej., payment-service).
- Facilita adopción de nuevas tecnologías por servicio (bases SQL/NoSQL, lenguajes).
- Auditoría centralizada que ayuda en diagnósticos y detecta anomalías.

### Negativas
- Complejidad operativa: orquestación de servicios, descubrimiento, monitoreo.
- Sobrecarga de comunicación interservicios (latencia, tolerancia a fallos).
- Gestión de consistencia eventual y transacciones distribuidas.
- Requiere infraestructura (Kubernetes, Service Mesh, Kafka) y expertise adicional.

---

## Especificaciones Técnicas
```yaml
Infraestructura:
  - Plataforma de contenedores: Kubernetes
  - Service Mesh: Istio (mTLS y circuit breaker)
  - Bus de eventos: Kafka (audit-service)
  - API Gateway: Kong o Spring Cloud Gateway
  - Config Server: Spring Cloud Config
  - Secrets: Vault

user-service:
  - Framework: Spring Boot + Spring Security + JWT
  - DB: PostgreSQL
  - Replicas: 1

customer-service:
  - Framework: Spring Boot
  - DB: PostgreSQL
  - Replicas: 1

payment-service:
  - Framework: Spring Boot
  - Integraciones: Qulqi, Niubiz, Izipay, PayU Latam
  - DB: PostgreSQL (transacciones)
  - Circuit Breaker: Resilience4j

audit-service:
  - Framework: Spring Boot + Spring Kafka
  - DB: MongoDB (colección `audit_logs`)
  - Topic de entrada: `audit.events`
```

# DR-003: Adopción de Arquitectura Hexagonal

**Fecha:** 2025-06-30

---

## Decisión
Estructurar todos los microservicios (user‑service, customer‑service, payment‑service, audit‑service) siguiendo el patrón de **Arquitectura Hexagonal** (Ports & Adapters).

---

## Contexto
- Proyecto de reserva y gestión de canchas deportivas basado en microservicios.
- Necesidad de mantener la lógica de negocio aislada de detalles de infraestructura (bases de datos, mensajería, UI).
- Evolución ágil y despliegues independientes de cada servicio.
- Requisitos de testeo unitario rígido y clara separación de responsabilidades.
- Integraciones con múltiples sistemas externos (JWT, pasarelas de pago, Kafka, MongoDB, PostgreSQL).

---

## Descripción de la Arquitectura

Cada microservicio se divide en tres capas principales:

1. **Dominio**
    - Entidades de negocio y reglas inmutables.
    - Objetos de valor, agregados y casos de uso (services/interactors).
    - Totalmente independiente de frameworks e infraestructura.

2. **Application / Ports**
    - **Inbound Ports**: Interfaces que definen operaciones del dominio (p. ej. `CreateReservationPort`, `AuthenticateUserPort`).
    - **Outbound Ports**: Interfaces para comunicaciones externas (p. ej. `UserRepository`, `PaymentGatewayPort`, `AuditLoggerPort`).

3. **Adapters / Infraestructura**
    - Implementaciones de **Inbound Adapters** (controladores REST).
    - Implementaciones de **Outbound Adapters** (Spring Data JPA/MongoDB, clientes de Kafka, SDKs de pasarelas de pago).
    - Configuración de IoC/DI que enlaza puertos con adaptadores concretos.

---

## Justificación
- **Aislamiento de Dominio:** Lógica de negocio protegida de cambios en infraestructuras.
- **Testabilidad:** Casos de uso pueden probarse sin levantar bases de datos ni colas.
- **Flexibilidad:** Cambiar adaptadores (p.ej. MongoDB a PostgreSQL) sin tocar la capa de dominio.
- **Mantenibilidad:** Separación clara de responsabilidades facilita onboarding y refactoring.
- **Escalabilidad de Equipo:** Múltiples equipos pueden trabajar en adaptadores o dominio sin interferir.

---

## Consecuencias

### Positivas
- Código de dominio altamente cohesivo y desacoplado.
- Ciclo de feedback rápido en tests unitarios de casos de uso.
- Menor riesgo al introducir o migrar componentes de infraestructura.

### Negativas
- Mayor sobrecarga inicial de configuración (más paquetes, interfaces y wiring).
- Curva de aprendizaje para desarrolladores no familiarizados con el patrón.
- Posible boilerplate de adaptadores y puertos.

---

## Especificaciones Técnicas
- **Lenguaje:** Java 17
- **Build:** Maven 3.9.9
- **Framework:** Spring Boot 3.5.3 

# DR-004: PostgreSQL como Base de Datos Principal para Gestión de Canchas Deportivas

**Fecha:** 2025-06-30

---

## Decisión
Usar **PostgreSQL 17** como sistema de gestión de base de datos principal para la aplicación de reserva y gestión de canchas deportivas. En entornos se produccion y testing se usara la imagen de postgres `17.5-alpine3.*` y para desarrollo, `7.5-bookworm`

---

## Contexto
- Plataforma B2C/B2B para gestión y reserva de canchas (fútbol, tenis, pádel, etc.)
- Múltiples instalaciones y sedes, cada una con horarios y tarifas variables
- Necesidad de garantizar consistencia en reservas, pagos y cancelaciones
- Requisitos de consultas analíticas para ocupación, ingresos y tendencias de uso
- Equipo backend con experiencia en SQL y Spring Boot, moderada experiencia previa en NoSQL
- Presupuesto ajustado y preferencia por tecnologías open source

---

## Justificación
- **Consistencia ACID**: Fundamental para evitar dobles reservas y garantizar integridad en cancelaciones y reembolsos.
- **Tipos Geoespaciales (PostGIS)**: Ubicación y búsqueda de canchas por proximidad y áreas de servicio.
- **JSON/JSONB**: Almacenamiento flexible de configuraciones de tarifas, equipamiento y reglas específicas de cada cancha.
- **Extensibilidad y Extensiones**:
    - `pg_stat_statements` para monitoreo de consultas.
    - `pg_trgm` para búsqueda difusa de ubicaciones y nombres de instalaciones.
- **Open Source sin licenciamiento**: Reduce costos operativos; amplia comunidad y herramientas maduras (pgAdmin, ORMs).
- **Rendimiento Analítico**: Excelente para reportes de ocupación, análisis de horas pico y predicción de demanda.

---

## Consecuencias

### Positivas
- Eliminación de costos por licencias de base de datos.
- Transacciones seguras y consistentes (reservas, pagos, cancelaciones).
- Búsqueda geoespacial avanzada para recomendaciones de canchas cercanas.
- Flexibilidad en el esquema para cambios rápidos en reglas de negocio.
- Herramientas de monitoreo y optimización maduras.
- `postgres:17.5-bookworm`: Incluye una suite completa de utilidades y herramientas de depuración estándar de Linux (bash, glibc, apt, coreutils completos). Esto facilita la interacción, el diagnóstico y la resolución de problemas durante el desarrollo.
- `postgres:17.5-alpine3.*`: Imágenes significativamente más pequeñas (menos componentes, menor superficie de ataque), lo que resulta en descargas más rápidas, menor consumo de recursos y mayor seguridad en entornos de despliegue.

### Negativas
- Configuración inicial y tuning más complejos que alternativas como MySQL.
- Poca oferta de hosting gestionado en algunos proveedores locales.
- Curva de aprendizaje para operadores DBA en PostGIS y JSONB.
- `postgres:17.5-bookworm`: El tamaño de la imagen es mayor en comparación con Alpine.
- `postgres:17.5-alpine3.*`: Utiliza `musl libc` en lugar de `glibc` (generalmente no es un problema para PostgreSQL en sí, pero puede serlo para software que depende de características específicas de `glibc`), y su set de herramientas preinstaladas es minimalista, lo que puede dificultar la depuración directa dentro del contenedor si no se instalan utilidades adicionales.

---

## Especificaciones Técnicas
```yaml
Versión: PostgreSQL 17.5
Extensiones:
  - pg_stat_statements
  - pg_trgm
  - postgis
Configuración recomendada (entorno de producción):
  shared_buffers: 512MB
  effective_cache_size: 2GB
  maintenance_work_mem: 128MB
  work_mem: 16MB
  max_connections: 100
  wal_level: replica
  archive_mode: on
  archive_command: 'cp %p /var/lib/postgresql/archive/%f'
```
# DR-005: Uso de Caché con Redis para la Base de Datos Principal

**Fecha:** 2025-06-30

---

## Decisión
Agregar una capa de caché basada en **Redis** delante de la base de datos principal (PostgreSQL) para mejorar rendimiento y reducir carga.

---

## Contexto
- Microservicios basados en Spring Boot (Java 17, Maven)
- Persistencia principal en PostgreSQL para datos críticos (usuarios, reservas, canchas)
- Consultas frecuentes de lectura: disponibilidad de canchas, listados de reservas recientes, configuraciones de tarifas
- Requisitos de baja latencia (< 100 ms) y alta tasa de solicitudes simultáneas
- Infraestructura en Kubernetes, con Redis desplegado en cluster para alta disponibilidad

---

## Justificación
- **Reducción de Latencia**: Responder lecturas de datos inmutables o de baja volatilidad directamente desde memoria.
- **Descarga de la BD**: Disminuir número de consultas a PostgreSQL para operaciones de lectura intensiva.
- **TTL Configurable**: Control fino del tiempo de vida de entradas en caché según criticidad de datos.
- **Patrones de Caché Probados**: Cache Aside (lazy loading), Read-Through, Write-Through para diferentes casos de uso.
- **Alta Disponibilidad**: Redis Cluster con réplicas y failover automático.

---

## Consecuencias

### Positivas
- Respuestas de consultas frecuentes < 10 ms.
- Menor solventación de CPU y I/O en la base de datos principal.
- Escalabilidad horizontal de Redis por demanda de cacheo.
- Control sobre expiración y consistencia eventual de datos en caché.

### Negativas
- Complejidad adicional en la lógica de aplicación para invalidación y refresco de caché.
- Consistencia eventual: lecturas pueden devolver datos ligeramente obsoletos hasta la expiración.
- Sobrecarga de operación de invalidación en eventos de escritura (reservas, cancelaciones).

---

## Especificaciones Técnicas
```yaml
Tecnología de Caché: Redis 8.0.2
Despliegue:
  - 3 shards, cada uno con 1 master + 1 replica
  - Persistence: AOF con appendfsync everysec
  - Configuración de memoria: maxmemory 4GB por shard, política allkeys-lru
Integración (Spring Boot):
  - spring-boot-starter-data-redis
Patrones:
  - **Cache-Aside** para listados (buscar en cache, si falla leer BD y poblar cache)  
  - **Write-Through** para datos críticos (al escribir en BD, también escribir en cache)  
  - **Pub/Sub** para invalidación distribuida en multi-instances  
TTL:
  - Disponibilidad de canchas: 60 segundos  
  - Listado de reservas recientes: 30 segundos  
  - Configuraciones de tarifas: 10 minutos  
```
# DR-006: Uso de MongoDB para la Base de Datos del Audit-Service

**Fecha:** 2025-06-30

---

## Decisión
Adoptar **MongoDB** como base de datos principal para el servicio de auditoría (audit-service).

---

## Contexto
- El audit-service recibe y almacena eventos de auditoría generados por otros microservicios (user-, customer-, payment-service).
- Alta volumetría de logs y registros de actividad, con escritura intensiva y consultas principalmente de lectura histórica.
- Necesidad de buscar y filtrar por múltiples campos (usuario, servicio, tipo de acción, timestamp).
- Requisitos de escalabilidad, retención configurable de datos y tolerancia a fallos.
- Infraestructura en Kubernetes, con MongoDB desplegado en un cluster gestionado.

---

## Justificación
- **Modelo de Documento Flexible**: Permite almacenar cada evento de auditoría con estructura JSON variable (metadatos, contexto, payload).
- **Escalabilidad Horizontal**: Sharding nativo de MongoDB para distribuir carga de escritura y almacenamiento.
- **Índices Compuestos y TTL**: Indexación eficiente por campos de filtrado frecuentes (usuario, servicio, nivel) y TTL Indexes para expiración automática de logs antiguos.
- **Alta Disponibilidad**: Replica Sets con failover automático.
- **Facilidad de Consulta**: Potentes operaciones de agregación para generar reportes y estadísticas.
- **Integración con Spring Data MongoDB**: Adaptador sencillo en audit-service para persistencia y consulta.

---

## Consecuencias

### Positivas
- Escrituras rápidas y escalables de grandes volúmenes de eventos.
- Consultas analíticas con pipelines de agregación nativas.
- Gestión automática de retención de datos mediante TTL.
- Alta disponibilidad garantizada por Replica Sets.

### Negativas
- Consistencia eventual por defecto (writes & reads pueden requerir configuración de write concern / read concern).
- Operaciones de agregación muy complejas pueden requerir tuning de índices y shards.
- Sobrecarga operativa para gestionar sharding y balanceo de datos.

---

## Especificaciones Técnicas
```yaml
Versión: MongoDB 8.0
Replica Set:
  - 3 réplicas (primary + 2 secondaries)
Sharding:
  - Shard Key: { service: 1, timestamp: 1 }
  - Config Servers: 3 miembros
Collections:
  - `audit_logs`
    - Documento ejemplo:
      {
        _id: ObjectId,
        timestamp: ISODate,
        service: "customer-service",
        userId: "abc123",
        action: "RESERVATION_CREATED",
        payload: { … }
      }
Indexes:
  - Compuesto: `{ service: 1, timestamp: -1 }`
  - TTL: `{ timestamp: 1 }` con `expireAfterSeconds: 2592000` (30 días)
  - Single-field: `{ userId: 1 }`, `{ action: 1 }`
Storage Engine: WiredTiger con compresión Snappy
Security:
  - Autenticación SCRAM-SHA-256
  - TLS entre réplicas y clientes
  - Roles mínimos (readWrite en `audit_logs`)
Backup & Restore:
  - Snapshots periódicos con Velero
  - `mongodump` incremental vía cron

Integración (Spring Boot):
  - `spring-boot-starter-data-mongodb`
  - Configuración de `MongoTemplate` y repositorios
  - `WriteConcern.MAJORITY`, `ReadPreference.PRIMARY_PREFERRED`
```