-- Creamos un "Cast Implícito".
-- Esto le dice a PostgreSQL: "Si te llega un texto (varchar) para un campo tipo_mov_mon, conviértelo automáticamente".

CREATE CAST (character varying AS public.tipo_mov_mon) WITH INOUT AS IMPLICIT;