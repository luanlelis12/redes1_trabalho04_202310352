/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 19 08 2025
* Ultima alteracao.: 28 10 2025
* Nome.............: ConversorStringBinario
* Funcao...........: Fazer as conversoes de String para binario, de binario para String e imprimir na tela os pacotes de bits
*************************************************************** */

package util;

public class ConversorStringBinario {

  /**********************************************************************
  * Metodo: codificadorDeMensagem
  * Funcao: Converte a String para um array de inteiros com os bits agrupados
  * Parametros: mensagem = String contendo a mensagem a ser enviada
  * Retorno: array de inteiros que representam a sequencia de binarios da mensagem
  ******************************************************************* */
  public static int[] codificadorDeMensagem(String mensagem) {
    char[] arrayChar = mensagem.toCharArray(); // transforma a mensagem num array de caracteres

    // Como cada inteiro armazena 32 bits e para cada 8 bits eh possivel armazenar um caractere
    // serao armazenados quatro caracteres num unico inteiro
    int tamanhoArray = (mensagem.length()+3)/4; // define o tamanho minimo necessario do pacote
    
    int[] pacoteBits = new int[tamanhoArray]; // array que armazena os pacotes de bits
    
    int contadorBits = 0; // conta quantos bits ja foram passados
    
    // loop por letra da mensagem
    for (char letra : arrayChar) {
      // transforma a letra no inteiro correspondente da tabela ASCII
      int asciiChar = (int) letra;

      // loop pra cada bit da letra
      for (int i = 0; i < 8; i++) {

        // 1 mascara usada (0000 0001);
        int bit = (asciiChar >> (7 - i)) & 1; 

        // Se o bit for 1, ele sera posicionado na posicao correta
        if (bit == 1) {
          int indicePacote = contadorBits / 32; // indice do array de inteiros o qual serah armazenado
          int posicaoNoPacote = 31 - (contadorBits % 32); // posicao do bit no inteiro
          pacoteBits[indicePacote] |= (1 << posicaoNoPacote); // faz a manipulacao de bit para ele ir na posicao correta
        } // fim do if

        contadorBits++;
      } // fim do for
    } // fim do for
    return pacoteBits;
  } // fim do metodo codificadorDeMensagem

  /**********************************************************************
  * Metodo: decodificadorDeMensagem
  * Funcao: Converte os binarios para uma String com a mensagem
  * Parametros: quadro = Array contendo a mensagem codificada em binario
  * Retorno: Mensagem original decodificada
  ******************************************************************* */
  public static String decodificadorDeMensagem(int[] quadro) {
    String mensagem = ""; // mensagem final decodificada
    int letraBinario = 0; // corresponde ao binario da letra da mensagem

    int contadorBits = 0; // conta quantos bits ja foram passados

    for (int pacote : quadro) {
      // loop pra cada bit da letra
      for (int i = 0; i < 32; i++) {

         // 1 -> mascara usada (0000 0001)
        int bit = (pacote >> (31 - i)) & 1;

        // Se o bit for 1, ele sera posicionado na posicao correta
        if (bit == 1) {
          int posicaoNaLetra = 7 - (contadorBits % 8);
          // posicaoNaLetra vai de 0 a 7 e eh usado para indicar qual eh a posicao do bit dentro do carectere ainda em ASCII
          letraBinario |= (1 << posicaoNaLetra); // posiciona o bit na posicao correta
        } // fim do if

        contadorBits++;

        if (contadorBits%8 == 0 && contadorBits != 0) { // como cada letra eh representada apenas por 8 bits eh executada as seguintes acoes
          mensagem += (char) letraBinario; // cocatena a letra decodificada a mensagem
          letraBinario = 0; // letraBinario eh "reiniciada" para armazenar uma nova letra
        } // fim do if
      } // fim do for
    } // fim do for
    return mensagem;
  } // fim do metodo decodificadorDeMensagem

  /* ***************************************************************
  * Metodo: exibirBits
  * Funcao: Imprime os pacotes de bits
  * Parametros: pacote = pacote de bits
  * Retorno: void
  *************************************************************** */
  public static void exibirBits(int[] pacotes) {
    // uma mascara com o 1 mais a esquerda possivel
    int marcara = 1 << 31; 

    for (int pacote : pacotes) {
      for (int i = 1; i <= 32; i++) {
        // Imprime os bits no terminal
        System.out.print((pacote & marcara) == 0 ? "0" : "1");
        pacote = pacote << 1;
        if (i % 8 == 0) { // a cada 8 bits um espaco vazio
          System.out.print(" ");
        } // fim do if
      } // fim do for
    } // fim do for
    
    System.out.println();
  } // fim do metodo exibirBits

}