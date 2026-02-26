package com.mycompany.todoultra;

public class TareaUI {
    private String texto;
    private boolean completada = false;

    public TareaUI(String texto){
        this.texto = texto;
    }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}
