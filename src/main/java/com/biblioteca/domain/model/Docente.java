package com.biblioteca.domain.model;

/**
 * Representa a un docente universitario registrado en el sistema de biblioteca.
 * Un docente puede tener hasta 5 ejemplares en préstamo simultáneamente,
 * reflejando una política de préstamo más amplia que la del estudiante.
 */
public class Docente extends Usuario {

    /** Departamento académico al que pertenece el docente. */
    private String departamento;

    /**
     * Tipo de contrato del docente (e.g., "TIEMPO_COMPLETO", "CATEDRA", "OCASIONAL").
     */
    private String tipoContrato;

    /** Constructor sin argumentos requerido por frameworks. */
    public Docente() {
        super();
    }

    /**
     * Constructor con todos los campos del Docente.
     *
     * @param idUsuario    identificador único del usuario
     * @param nombre       nombre completo
     * @param departamento departamento académico
     * @param tipoContrato tipo de contrato laboral
     */
    public Docente(String idUsuario, String nombre, String departamento, String tipoContrato) {
        super(idUsuario, nombre);
        this.departamento = departamento;
        this.tipoContrato = tipoContrato;
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }

    // ─── Regla de negocio polimórfica ─────────────────────────────────────────

    /**
     * Los docentes tienen un límite de 5 préstamos simultáneos.
     *
     * @return 5
     */
    @Override
    public int getLimitePrestamos() {
        return 5;
    }

    @Override
    public String toString() {
        return "Docente[id=" + getIdUsuario()
             + ", nombre=" + getNombre()
             + ", departamento=" + departamento
             + ", tipoContrato=" + tipoContrato + "]";
    }
}
