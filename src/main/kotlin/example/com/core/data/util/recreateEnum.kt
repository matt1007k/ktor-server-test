package example.com.core.data.util

import org.jetbrains.exposed.sql.transactions.transaction

fun createOrUpdateEnum(enumName: String, values: List<String>) {
    transaction {
        // 🔹 1. Crear el ENUM si no existe
        exec("""
            DO $$ 
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = '$enumName') THEN
                    CREATE TYPE "$enumName" AS ENUM (${values.joinToString { "'$it'" }});
                END IF;
            END $$;
        """.trimIndent())

        // 🔹 2. Agregar valores nuevos al ENUM
        values.forEach { value ->
            exec("""
                DO $$ 
                BEGIN
                    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = '$enumName') AND enumlabel = '$value') THEN
                        ALTER TYPE "$enumName" ADD VALUE '$value';
                    END IF;
                END $$;
            """.trimIndent())
        }
    }
}
