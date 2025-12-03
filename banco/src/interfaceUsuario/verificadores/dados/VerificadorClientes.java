package interfaceUsuario.verificadores.dados;

import cliente.exceptions.TiposClientes;
import interfaceUsuario.exceptions.ValorInvalido;

import java.util.regex.Matcher;

import static interfaceUsuario.verificadores.dados.VerificadorEntrada.*;


public class VerificadorClientes {

    private VerificadorClientes() {
        throw new IllegalStateException("Utility class");
    }

    private static boolean isAlphabetic(String e) {
        return !NAO_APENAS_ALFABETO.matcher(e).find();
    }

    static boolean isAlphanumeric(String e) {
        return !NAO_APENAS_ALFABETO_DIGITO.matcher(e).find();
    }

    public static boolean verificarEndereco(String[] entradaEndereco) {
        try {
            Integer.parseInt(entradaEndereco[0]);
            Integer.parseInt(entradaEndereco[1]);
            if (entradaEndereco[0].length() != TAMANHO_CEP) {
                throw new ValorInvalido("CEP INVALIDO");
            }
            return true;
        } catch (Exception ignored) {

        }
        return false;
    }


    protected static boolean verificarTelefone(String e) {
        return e.length() <= DIGITOS_MAXIMO_TELEFONE;
    }

    protected static boolean verificarEmail(String e) {
        Matcher matcher = EMAIL_VERIFICADOR.matcher(e);
        return matcher.matches();
    }

    public static boolean verificarIdade(String idade, TiposClientes tiposClientes) {
        if (tiposClientes == TiposClientes.CLIENTE_EMPRESA) {
            return Integer.parseInt(idade) >= 3;
        }
        return Integer.parseInt(idade) >= 18;
    }

    protected static boolean verificarSenha(String s) {
        if (HAS_WHITESPACE.matcher(s).find()) {
            return false;
        }
        return s.length() >= TAMANHO_MINIMO_SENHA;
    }

    private static boolean verificarIdentificacao(String s, TiposClientes tiposClientes) {

        if (tiposClientes == TiposClientes.CLIENTE_PESSOA) {
            if (s.length() != TAMANHO_CPF) {
                return false;
            }
            return !QUALQUER_NAO_DIGITO.matcher(s).find();

        } else if (tiposClientes == TiposClientes.CLIENTE_EMPRESA) {
            if (s.length() != TAMANHO_CNPJ) {
                return false;
            }
            return !QUALQUER_NAO_DIGITO.matcher(s).find();
        }
        return false;
    }

    static boolean verificadorIdentificacao(String e) {
        try {
            Integer.parseInt(e);
        } catch (NumberFormatException ex) {
            return true;
        }
        return false;
    }

    public static boolean verificarIdentidadeGerente(String s) {
        if (s.length() <= QUANTIDADE_IDENTIFICACAO_VALIDA) {
            return VerificadorClientes.verificadorIdentificacao(s);
        }
        return false;
    }

    public static boolean informacoesClientes(String[] entradaGeral, TiposClientes tiposClientes) throws ValorInvalido {
        for (String s : entradaGeral) {
            if (s.isBlank()) {
                throw new ValorInvalido("Nenhum dos campos podem ser vazios. Tente novamente");
            }
            if (s.length() > MAX_CARACTERES_ENTRADA) {
                throw new ValorInvalido("Numero de caracteres excedido");
            }
        }

        if (!isAlphabetic(entradaGeral[0])) {
            throw new ValorInvalido("Por favor, o nome nao deve conter numeros ou caracteres invalidos. Tente novamente");
        }

        if (!verificarEmail(entradaGeral[1])) {
            throw new ValorInvalido("Por favor, Insira um email valido");
        }

        if ((!verificarTelefone(entradaGeral[2]))) {
            throw new ValorInvalido("Telefone Invalido. Tente novamente");
        }

        if (!verificarIdade(entradaGeral[3], tiposClientes)) {
            throw new ValorInvalido("A idade foi inserida incorretamente\n[Voce deve ser maior de 18 anos caso for Pessoa Fisica e Ter pelo menos 3 anos de Empresa," +
                    " caso for Pessoa Juridica]. Tente novamente");
        }

        if (!verificarIdentificacao(entradaGeral[4], tiposClientes)) {
            String tag = (tiposClientes == TiposClientes.CLIENTE_PESSOA) ? "CPF" : "CNPJ";
            throw new ValorInvalido(tag + " invalida, tente novamente");
        }

        if (!verificarSenha(entradaGeral[5])) {
            throw new ValorInvalido("A senha deve conter pelo menos 3 digitos e nao conter espacos.");
        }
        return true;
    }

}
