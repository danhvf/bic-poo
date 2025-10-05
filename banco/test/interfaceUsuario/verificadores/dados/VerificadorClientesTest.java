import cliente.exceptions.TiposClientes;
import interfaceUsuario.exceptions.ValorInvalido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static interfaceUsuario.verificadores.dados.VerificadorClientes.*;
import static org.junit.jupiter.api.Assertions.*;

class VerificadorClientesTest {

    // Testes para o método verificarEndereco
    @Test
    @DisplayName("Deve retornar true para um endereço válido")
    void verificarEndereco_QuandoValido_DeveRetornarTrue() {
        String[] entradaEnderecoValido = {"12345678", "100"};
        assertTrue(verificarEndereco(entradaEnderecoValido));
    }

    @Test
    @DisplayName("Deve retornar false para um CEP com tamanho inválido")
    void verificarEndereco_QuandoCepInvalido_DeveRetornarFalse() {
        String[] entradaCepInvalido = {"12345", "100"};
        assertFalse(verificarEndereco(entradaCepInvalido));
    }

    @Test
    @DisplayName("Deve retornar false para um endereço com caracteres não numéricos")
    void verificarEndereco_QuandoNaoNumerico_DeveRetornarFalse() {
        String[] entradaNaoNumerica = {"abcdefgh", "10a"};
        assertFalse(verificarEndereco(entradaNaoNumerica));
    }

    // Testes para o método informacoesClientes

    @Test
    @DisplayName("Deve lançar ValorInvalido se algum campo estiver em branco")
    void informacoesClientes_QuandoCampoEmBranco_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome", "email@teste.com", "", "25", "12345678901", "senha123"};
        
        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_PESSOA);
        });

        assertEquals("Nenhum dos campos podem ser vazios. Tente novamente", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValorInvalido para nome com números")
    void informacoesClientes_QuandoNomeInvalido_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome123", "email@teste.com", "21999999999", "25", "12345678901", "senha123"};
        
        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_PESSOA);
        });

        assertEquals("Por favor, o nome nao deve conter numeros ou caracteres invalidos. Tente novamente", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValorInvalido para email inválido")
    void informacoesClientes_QuandoEmailInvalido_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome", "email-invalido", "21999999999", "25", "12345678901", "senha123"};

        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_PESSOA);
        });

        assertEquals("Por favor, Insira um email valido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValorInvalido para idade inválida (Pessoa Física)")
    void informacoesClientes_QuandoIdadePessoaInvalida_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome", "email@teste.com", "21999999999", "17", "12345678901", "senha123"};

        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_PESSOA);
        });

        assertTrue(exception.getMessage().contains("A idade foi inserida incorretamente"));
    }
    
    @Test
    @DisplayName("Deve lançar ValorInvalido para idade inválida (Empresa)")
    void informacoesClientes_QuandoIdadeEmpresaInvalida_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome Empresa", "contato@empresa.com", "21999999999", "2", "12345678901234", "senha123"};

        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_EMPRESA);
        });

        assertTrue(exception.getMessage().contains("A idade foi inserida incorretamente"));
    }

    @Test
    @DisplayName("Deve lançar ValorInvalido para CPF com tamanho incorreto")
    void informacoesClientes_QuandoCpfInvalido_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome", "email@teste.com", "21999999999", "25", "12345", "senha123"};

        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_PESSOA);
        });

        assertEquals("CPF invalida, tente novamente", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValorInvalido para CNPJ com tamanho incorreto")
    void informacoesClientes_QuandoCnpjInvalido_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome Empresa", "contato@empresa.com", "21999999999", "5", "12345", "senha123"};

        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_EMPRESA);
        });

        assertEquals("CNPJ invalida, tente novamente", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValorInvalido para senha muito curta")
    void informacoesClientes_QuandoSenhaInvalida_DeveLancarExcecao() {
        String[] dadosInvalidos = {"Nome", "email@teste.com", "21999999999", "25", "12345678901", "s1"};
        
        ValorInvalido exception = assertThrows(ValorInvalido.class, () -> {
            informacoesClientes(dadosInvalidos, TiposClientes.CLIENTE_PESSOA);
        });

        assertEquals("A senha deve conter pelo menos 3 digitos e nao conter espacos.", exception.getMessage());
    }

    // Casos de Sucesso
    @Test
    @DisplayName("Deve retornar true para dados válidos de Cliente Pessoa")
    void informacoesClientes_QuandoDadosPessoaValidos_DeveRetornarTrue() throws ValorInvalido {
        String[] dadosValidos = {"Nome Sobrenome", "email@valido.com", "21987654321", "25", "12345678901", "senhaValida123"};
        
        boolean resultado = informacoesClientes(dadosValidos, TiposClientes.CLIENTE_PESSOA);
        
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Deve retornar true para dados válidos de Cliente Empresa")
    void informacoesClientes_QuandoDadosEmpresaValidos_DeveRetornarTrue() throws ValorInvalido {
        String[] dadosValidos = {"Nome Empresa LTDA", "contato@empresa.com", "2133334444", "10", "12345678901234", "senhaCorporativa"};

        boolean resultado = informacoesClientes(dadosValidos, TiposClientes.CLIENTE_EMPRESA);

        assertTrue(resultado);
    }
}