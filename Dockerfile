# Dockerfile para la API REST de Task Manager (Java + Tomcat)
# Usamos Tomcat 9 con Java 8 para compatibilidad con el proyecto

FROM tomcat:9.0-jre8

# Etiquetas informativas
LABEL maintainer="Task Manager Team"
LABEL version="1.0"
LABEL description="API REST para gestión de tareas con Oracle PL/SQL y Java"

# Variables de entorno
ENV CATALINA_HOME=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Xmx512m -Xms256m"
ENV TZ=America/Bogota

# Crear directorio para la aplicación
WORKDIR $CATALINA_HOME

# Copiar el archivo WAR generado por Maven
# Nota: Se asume que el WAR ya está construido (task-api.war)
COPY target/task-api.war $CATALINA_HOME/webapps/

# Exponer el puerto de Tomcat
EXPOSE 8080

# Comando para ejecutar Tomcat
CMD ["catalina.sh", "run"]