package com.mindsoft.abu.abu_mvp.Base;

public class Constantes {


    public static final String TABLA_BEACON="beaconsLeidos";
    public static final String CAMPO_ID="id";
    public static final String CAMPO_NOMBRE="nombre";
    public static final String CAMPO_FECHA="fecha";

    public static final String CREAR_TABLA_BEACON= "CREATE TABLE "+TABLA_BEACON+" ("
                                                                                    +CAMPO_ID+" TEXT, "+
                                                                                    CAMPO_NOMBRE+" TEXT, "+
                                                                                    CAMPO_FECHA+" DATETIME)";

}
