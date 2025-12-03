package interfaceUsuario.verificadores.dados;

import cliente.exceptions.TiposClientes;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static interfaceUsuario.verificadores.dados.VerificadorClientes.*;
import static org.junit.jupiter.api.Assertions.*;

class SistemaClienteCadastroTest {


    static class ClienteTeste {
        String nome;
        String cpf;
        boolean cadastrado;

        public ClienteTeste(String nome, String cpf) {
            this.nome = nome;
            this.cpf = cpf;
            this.cadastrado = true;
        }
    }

    @Test
    @DisplayName("SYS-01: Fluxo de Cadastro Completo (Caminho Feliz)")
    void sistema_CadastroRealizadoComSucesso() {

        String[] dadosFormulario = {"Maria Silva", "maria@email.com", "21999998888", "30", "12345678901", "senha123"};


        ClienteTeste novoClienteNoBanco = null;

        try {
            // 1. O Verificador valida
            boolean dadosValidos = informacoesClientes(dadosFormulario, TiposClientes.CLIENTE_PESSOA);

            // 2. Se validou, o Sistema cria o cliente
            if (dadosValidos) {
                novoClienteNoBanco = new ClienteTeste(dadosFormulario[0], dadosFormulario[4]);
            }

        } catch (ValorInvalido e) {
            fail("Não deveria falhar com dados válidos");
        }

        // Validação de Sistema
        assertNotNull(novoClienteNoBanco, "O cliente deveria ter sido instanciado.");
        assertTrue(novoClienteNoBanco.cadastrado, "O cliente deve constar como cadastrado no sistema.");
        assertEquals("Maria Silva", novoClienteNoBanco.nome);
    }

    @Test
    @DisplayName("SYS-02: Bloqueio de Regra de Negócio (Idade Insuficiente)")
    void sistema_BloqueioCadastroMenorIdade() {
        // Tentativa de cadastro de menor de idade
        String[] dadosMenor = {"Enzo Gabriel", "enzo@email.com", "21999998888", "10", "12345678901", "senha123"};

        ClienteTeste novoClienteNoBanco = null;

        try {
            // 1. O Verificador tenta validar
            informacoesClientes(dadosMenor, TiposClientes.CLIENTE_PESSOA);


            novoClienteNoBanco = new ClienteTeste(dadosMenor[0], dadosMenor[4]);

        } catch (ValorInvalido e) {

            System.out.println("Bloqueio de sistema acionado corretamente: " + e.getMessage());
        }

        // Validação de Sistema: O cliente NÃO deve existir
        assertNull(novoClienteNoBanco, "O sistema não deve criar objetos de clientes inválidos.");
    }

    @Test
    @DisplayName("SYS-03 [CRÍTICO]: Cadastro de Cliente com CPF Corrompido (Bug)")
    void sistema_BugCriticoCadastroCpfInvalido() {


        String cpfCorrompido = "1234567890a"; // Tem letra
        String[] dadosBugados = {"Hacker", "hacker@email.com", "21999998888", "30", cpfCorrompido, "senha123"};

        ClienteTeste novoClienteNoBanco = null;

        try {

            boolean dadosValidos = informacoesClientes(dadosBugados, TiposClientes.CLIENTE_PESSOA);




            if (dadosValidos) {
                novoClienteNoBanco = new ClienteTeste(dadosBugados[0], dadosBugados[4]);
            }

        } catch (ValorInvalido e) {
            fail("O teste falhou porque o bug foi corrigido (o verificador lançou erro). Se o bug existe, aqui não entra.");
        }


        assertNotNull(novoClienteNoBanco, "FALHA GRAVE: O sistema permitiu cadastrar um cliente com CPF inválido.");
        assertEquals("1234567890a", novoClienteNoBanco.cpf, "O banco de dados agora contém um CPF sujo/corrompido.");
    }
}