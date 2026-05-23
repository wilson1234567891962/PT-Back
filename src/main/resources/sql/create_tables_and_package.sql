-- Script PL/SQL para crear la tabla TASKS y el paquete TASK_PKG
-- Oracle Database

-- 1. Crear tabla TASKS
CREATE TABLE TASKS (
    TASK_ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TITLE VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(1000),
    COMPLETED NUMBER(1) DEFAULT 0 NOT NULL CHECK (COMPLETED IN (0, 1)),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Crear índice para búsquedas por título
CREATE INDEX IDX_TASKS_TITLE ON TASKS(TITLE);

-- Crear índice para búsquedas por estado completado
CREATE INDEX IDX_TASKS_COMPLETED ON TASKS(COMPLETED);

-- 2. Crear paquete TASK_PKG
CREATE OR REPLACE PACKAGE TASK_PKG AS
    
    -- Procedimiento para obtener todas las tareas
    PROCEDURE GET_ALL_TASKS(
        p_cursor OUT SYS_REFCURSOR
    );
    
    -- Procedimiento para obtener una tarea por ID
    PROCEDURE GET_TASK_BY_ID(
        p_task_id IN NUMBER,
        p_cursor OUT SYS_REFCURSOR
    );
    
    -- Procedimiento para crear una nueva tarea
    PROCEDURE CREATE_TASK(
        p_title IN VARCHAR2,
        p_description IN VARCHAR2,
        p_completed IN NUMBER,
        p_new_task_id OUT NUMBER
    );
    
    -- Procedimiento para actualizar una tarea
    PROCEDURE UPDATE_TASK(
        p_task_id IN NUMBER,
        p_title IN VARCHAR2,
        p_description IN VARCHAR2,
        p_completed IN NUMBER
    );
    
    -- Procedimiento para eliminar una tarea
    PROCEDURE DELETE_TASK(
        p_task_id IN NUMBER
    );
    
END TASK_PKG;
/

-- 3. Crear cuerpo del paquete TASK_PKG
CREATE OR REPLACE PACKAGE BODY TASK_PKG AS
    
    PROCEDURE GET_ALL_TASKS(
        p_cursor OUT SYS_REFCURSOR
    ) AS
    BEGIN
        OPEN p_cursor FOR
        SELECT 
            TASK_ID,
            TITLE,
            DESCRIPTION,
            COMPLETED,
            CREATED_AT,
            UPDATED_AT
        FROM TASKS
        ORDER BY CREATED_AT DESC;
    END GET_ALL_TASKS;
    
    PROCEDURE GET_TASK_BY_ID(
        p_task_id IN NUMBER,
        p_cursor OUT SYS_REFCURSOR
    ) AS
    BEGIN
        OPEN p_cursor FOR
        SELECT 
            TASK_ID,
            TITLE,
            DESCRIPTION,
            COMPLETED,
            CREATED_AT,
            UPDATED_AT
        FROM TASKS
        WHERE TASK_ID = p_task_id;
    END GET_TASK_BY_ID;
    
    PROCEDURE CREATE_TASK(
        p_title IN VARCHAR2,
        p_description IN VARCHAR2,
        p_completed IN NUMBER,
        p_new_task_id OUT NUMBER
    ) AS
    BEGIN
        INSERT INTO TASKS (
            TITLE,
            DESCRIPTION,
            COMPLETED,
            CREATED_AT,
            UPDATED_AT
        ) VALUES (
            p_title,
            p_description,
            p_completed,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        RETURNING TASK_ID INTO p_new_task_id;
        
        COMMIT;
    END CREATE_TASK;
    
    PROCEDURE UPDATE_TASK(
        p_task_id IN NUMBER,
        p_title IN VARCHAR2,
        p_description IN VARCHAR2,
        p_completed IN NUMBER
    ) AS
    BEGIN
        UPDATE TASKS
        SET 
            TITLE = p_title,
            DESCRIPTION = p_description,
            COMPLETED = p_completed,
            UPDATED_AT = CURRENT_TIMESTAMP
        WHERE TASK_ID = p_task_id;
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Tarea no encontrada con ID: ' || p_task_id);
        END IF;
        
        COMMIT;
    END UPDATE_TASK;
    
    PROCEDURE DELETE_TASK(
        p_task_id IN NUMBER
    ) AS
    BEGIN
        DELETE FROM TASKS
        WHERE TASK_ID = p_task_id;
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Tarea no encontrada con ID: ' || p_task_id);
        END IF;
        
        COMMIT;
    END DELETE_TASK;
    
END TASK_PKG;
/

-- 4. Insertar datos de prueba
INSERT INTO TASKS (TITLE, DESCRIPTION, COMPLETED) VALUES ('Tarea de ejemplo 1', 'Descripción de la tarea 1', 0);
INSERT INTO TASKS (TITLE, DESCRIPTION, COMPLETED) VALUES ('Tarea de ejemplo 2', 'Descripción de la tarea 2', 1);
INSERT INTO TASKS (TITLE, DESCRIPTION, COMPLETED) VALUES ('Tarea de ejemplo 3', 'Descripción de la tarea 3', 0);

COMMIT;

-- 5. Verificar la creación
SELECT 'Tabla TASKS creada exitosamente' AS MENSAJE FROM DUAL;
SELECT COUNT(*) AS TOTAL_TAREAS FROM TASKS;

-- 6. Probar el paquete
DECLARE
    v_cursor SYS_REFCURSOR;
    v_task_id NUMBER;
    v_title VARCHAR2(200);
    v_description VARCHAR2(1000);
    v_completed NUMBER;
    v_created_at TIMESTAMP;
    v_updated_at TIMESTAMP;
    v_new_task_id NUMBER;
BEGIN
    -- Probar GET_ALL_TASKS
    DBMS_OUTPUT.PUT_LINE('=== Probando GET_ALL_TASKS ===');
    TASK_PKG.GET_ALL_TASKS(v_cursor);
    LOOP
        FETCH v_cursor INTO v_task_id, v_title, v_description, v_completed, v_created_at, v_updated_at;
        EXIT WHEN v_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('Tarea ID: ' || v_task_id || ', Título: ' || v_title);
    END LOOP;
    CLOSE v_cursor;
    
    -- Probar CREATE_TASK
    DBMS_OUTPUT.PUT_LINE('=== Probando CREATE_TASK ===');
    TASK_PKG.CREATE_TASK('Nueva tarea de prueba', 'Descripción de prueba', 0, v_new_task_id);
    DBMS_OUTPUT.PUT_LINE('Nueva tarea creada con ID: ' || v_new_task_id);
    
    -- Probar GET_TASK_BY_ID
    DBMS_OUTPUT.PUT_LINE('=== Probando GET_TASK_BY_ID ===');
    TASK_PKG.GET_TASK_BY_ID(v_new_task_id, v_cursor);
    FETCH v_cursor INTO v_task_id, v_title, v_description, v_completed, v_created_at, v_updated_at;
    IF v_cursor%FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Tarea encontrada: ' || v_title);
    END IF;
    CLOSE v_cursor;
    
    -- Probar UPDATE_TASK
    DBMS_OUTPUT.PUT_LINE('=== Probando UPDATE_TASK ===');
    TASK_PKG.UPDATE_TASK(v_new_task_id, 'Tarea actualizada', 'Descripción actualizada', 1);
    DBMS_OUTPUT.PUT_LINE('Tarea actualizada exitosamente');
    
    -- Probar DELETE_TASK
    DBMS_OUTPUT.PUT_LINE('=== Probando DELETE_TASK ===');
    TASK_PKG.DELETE_TASK(v_new_task_id);
    DBMS_OUTPUT.PUT_LINE('Tarea eliminada exitosamente');
    
    DBMS_OUTPUT.PUT_LINE('=== Todas las pruebas completadas exitosamente ===');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/