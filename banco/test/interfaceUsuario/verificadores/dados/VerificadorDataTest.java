package interfaceUsuario.verificadores.dados;

import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static interfaceUsuario.verificadores.dados.VerificadorData.verificarData;
import static interfaceUsuario.verificadores.dados.VerificadorData.verificarDataDebitoAuto;
import static org.junit.jupiter.api.Assertions.*;

class VerificadorDataTest {


    // A declaração 'throws ValorInvalido' precisa estar aqui para compilar
    @Test
    @DisplayName("Aresta 1.1: Falha na estrutura (Não contém '/')")
    void verificarData_quandoNaoContemBarra_deveLancarValorInvalido() {
        // O assertThrows lida com a exceção, mas o método deve declará-la
        assertThrows(ValorInvalido.class, () -> verificarData("31-12-2025"));
    }

    @Test
    @DisplayName("Aresta 1.2: Falha de formatação (Contém texto no meio)")
    void verificarData_quandoComponenteNaoNumerico_deveLancarValorInvalido() {
        assertThrows(ValorInvalido.class, () -> verificarData("31/DEZ/2025"));
    }

    @Test
    @DisplayName("Aresta 1.3: Falha de Valor (Componente zero ou negativo)")
    void verificarData_quandoComponenteNegativo_deveLancarValorInvalido() {
        assertThrows(ValorInvalido.class, () -> verificarData("01/-02/2025"));
    }

    // --- RAMIFICAÇÕES DE LÓGICA (Deve retornar true/false) ---

    // ESTES MÉTODOS DE SUCESSO PRECISAM DECLARAR O THROWS para compilar
    @Test
    @DisplayName("Aresta 1.4: Sucesso (Todos os limites OK)")
    void verificarData_quandoDataValida_deveRetornarTrue() throws ValorInvalido {
        boolean resultado = verificarData("25/08/2024");
        assertTrue(resultado, "A data válida deveria retornar true.");
    }

    @Test
    @DisplayName("Aresta 1.5: Falha no Limite do Mês (Month Boundary)")
    void verificarData_quandoMesInvalido_deveRetornarFalse() throws ValorInvalido {
        boolean resultado = verificarData("25/13/2024");
        assertFalse(resultado, "Mês 13 deveria retornar false.");
    }

    @Test
    @DisplayName("Aresta 1.6: Falha no Limite do Dia (Day Boundary)")
    void verificarData_quandoDiaInvalido_deveRetornarFalse() throws ValorInvalido {
        boolean resultado = verificarData("32/12/2024");
        assertFalse(resultado, "Dia 32 deveria retornar false.");
    }

    @Test
    @DisplayName("Aresta 1.7: Falha no Limite do Ano (Year Boundary)")
    void verificarData_quandoAnoAntigo_deveRetornarFalse() throws ValorInvalido {
        boolean resultado = verificarData("15/12/1999");
        assertFalse(resultado, "Ano 1999 deveria retornar false.");
    }

    // =========================================================================
    // GRUPO 2: TESTES FUNCIONAIS para verificarDataDebitoAuto (Não usam throws)
    // =========================================================================

    @Test
    @DisplayName("Funcional 2.1: Débito Automático - Sucesso (Dentro dos limites)")
    void verificarDataDebitoAuto_quandoValido_deveRetornarTrue() {
        assertTrue(verificarDataDebitoAuto("5"), "Dia 5 deveria estar dentro do limite (1 a 10).");
        assertTrue(verificarDataDebitoAuto("1"), "Dia 1 (limite mínimo) deveria ser aceito.");
        assertTrue(verificarDataDebitoAuto("10"), "Dia 10 (limite máximo) deveria ser aceito.");
    }

    @Test
    @DisplayName("Funcional 2.2: Débito Automático - Fora dos Limites")
    void verificarDataDebitoAuto_quandoForaDoLimite_deveRetornarFalse() {
        assertFalse(verificarDataDebitoAuto("0"), "Dia 0 deveria ser rejeitado.");
        assertFalse(verificarDataDebitoAuto("11"), "Dia 11 deveria ser rejeitado.");
    }

    @Test
    @DisplayName("Funcional 2.3: Débito Automático - Falha de Formato (Texto)")
    void verificarDataDebitoAuto_quandoTexto_deveRetornarFalse() {
        assertFalse(verificarDataDebitoAuto("texto"), "Texto deveria falhar na conversão e retornar false.");
    }
}