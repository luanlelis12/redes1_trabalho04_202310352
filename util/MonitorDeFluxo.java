/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 28 10 2025
* Ultima alteracao.: 02 11 2025
* Nome.............: MonitoDeFluxo
* Funcao...........: Faz o tratamento dos 0 no quadro transmitido
*************************************************************** */

package util;

public class MonitorDeFluxo {

  // Tipos de protocolos da camada fisica
  final String BINARIA = "Binaria";
  final String MANCHESTER = "Manchester";
  final String MANCHESTER_DIFERENCIAL = "Manchester Diferencial";

  // Tipos de protocolos de enquadramento
  final String CONTAGEM_DE_CARACTERE = "Contagem de caractere";
  final String INSERCAO_DE_BYTES = "Insercao de bytes";
  final String INSERCAO_DE_BITS = "Insercao de bits";

  // Onde o fluxo completo de sera armazenado
  private StringBuilder fluxoDeBits = new StringBuilder();

  private final int FLAG_BITS = 0x7E; // 01111110

  private final int FLAG_BYTES = 126; // ~

  private final int ESCAPE_BYTES = 125; // }

  private final int MANCHESTER_FLAG_BITS = 0x6AA9;

  // Tipo de protocolo na transmissao usado na mensagem
  private String tipoProtocoloFisica; 
  // Tipo de enquadramento utilizado na mensagem
  private String tipoProtocoloEnquadramento;

  /* ***************************************************************
  * Metodo: setTipoProtocolo
  * Funcao: Define o tipo de enquadramento sendo usado
  * Parametros: tipoProtocolo = enquadramento usado na mensagem
  * Retorno: void
  *************************************************************** */
  public void setTipoProtocoloFisica(String tipoProtocoloFisica) {
    this.tipoProtocoloFisica = tipoProtocoloFisica;
  } // fim do metodo setTipoProtocoloFisica

  /* ***************************************************************
  * Metodo: setTipoProtocoloEnquadramento
  * Funcao: Define o tipo de enquadramento sendo usado
  * Parametros: tipoProtocolo = enquadramento usado na mensagem
  * Retorno: void
  *************************************************************** */
  public void setTipoProtocoloEnquadramento(String tipoProtocoloEnquadramento) {
    this.tipoProtocoloEnquadramento = tipoProtocoloEnquadramento;
  } // fim do metodo setTipoProtocoloEnquadramento

  /* ***************************************************************
  * Metodo: limparFluxo
  * Funcao: Limpa o fluxo de bits transmitido
  * Parametros:
  * Retorno: void
  *************************************************************** */
  public void limparFluxo() {
    this.fluxoDeBits.setLength(0);
  } // fim do metodo limparFluxo

  /* ***************************************************************
  * Metodo: getFluxoFormatado
  * Funcao: Retorna os bits transmitidos
  * Parametros:
  * Retorno: Fluxo de bits com os zeros tratados
  *************************************************************** */
  public String getFluxoFormatado() {
    return this.fluxoDeBits.toString();
  } // fim do metodo getFluxoFormatado

   /* ***************************************************************
  * Metodo: adicionarQuadroAoFluxo
  * Funcao: Adiciona o quadro sendo transmitidos a transmissao total dos bits
  * Parametros: quadro = quadro sendo enviado pelo meioDeComunicacao
  * Retorno: void
  *************************************************************** */
  public void adicionarQuadroAoFluxo(int[] quadro) {

    int bitsValidos = 0; // O "tamanho real" do quadro

    // --- LOGICA DE TRATAMENTO DE ZEROS ---

    if (tipoProtocoloFisica.equals(BINARIA)) {
      // --- CASO 1: FISICA E BINARIA ---
      switch (tipoProtocoloEnquadramento) {
        case CONTAGEM_DE_CARACTERE:
          // Le o primeiro byte (que esta "limpo")
          bitsValidos = ((quadro[0] >>> 24) & 0xFF) * 8;
          break;
        case INSERCAO_DE_BITS:
          bitsValidos = encontrarFimDaFlagDeBits(quadro);
          break;
        case INSERCAO_DE_BYTES:
          bitsValidos = encontrarFimDaFlagDeBytes(quadro) * 8;
          break;
        default:
          // Se nao souber, imprime o buffer todo
          bitsValidos = encontrarUltimoBit(quadro);
      } // fim do switch/case
    } else {
      // --- CASO 2: FISICA E MANCHESTER (OU DIFERENCIAL) ---
      switch (tipoProtocoloEnquadramento) {
        case CONTAGEM_DE_CARACTERE:
          bitsValidos = encontrarUltimoBit(quadro);
          break;
        case INSERCAO_DE_BITS:
        case INSERCAO_DE_BYTES:
          // Procura pela FLAG *codificada* em Manchester
          bitsValidos = encontrarFimDaFlagManchester(quadro);
          break;
        default:
          bitsValidos = encontrarUltimoBit(quadro);
      } // fim do switch/case
    } // fim do if

    // Formata e anexa os bits validos
    for (int i = 0; i < bitsValidos; i++) {
      // Adiciona um espaco a cada 8 bits para legibilidade
      if (i % 8 == 0 && i != 0) {
        fluxoDeBits.append(" ");
      } // fim do if

      // Le o bit 'i' do array de int[]
      int indiceArray = i / 32;
      // Protecao contra ArrayOutOfBounds
      if (indiceArray >= quadro.length)
        break;

      int bitPos = 31 - (i % 32);
      int bit = (quadro[indiceArray] >> bitPos) & 1;

      fluxoDeBits.append(bit);
    } // fim do for

    fluxoDeBits.append(" "); // Espaco entre quadros
  } // fim do metodo adicionarQuadroAoFluxo

  /* ***************************************************************
  * Metodo: encontrarFimDaFlagDeBits
  * Funcao: Procura a flag final do enquadramento de bits para fazer o tratamento dos 0 nao "transmitidos"
  * Parametros: quadro = quadro com enquadramentos de bits
  * Retorno: A quantidade real de bits sendo transmitidos
  *************************************************************** */
  private int encontrarFimDaFlagDeBits(int[] quadro) {
    int flagBuffer = 0;
    boolean achouPrimeiraFlag = false;
    // Itera bit a bit pelo array
    for (int i = 0; i < quadro.length * 32; i++) {
      int bitLido = (quadro[i / 32] >>> (31 - (i % 32))) & 1;
      // Adiciona o bit ao buffer e mantem so os ultimos 8
      flagBuffer = ((flagBuffer << 1) | bitLido) & 0xFF;
      if (flagBuffer == FLAG_BITS) {
        if (!achouPrimeiraFlag) {
          achouPrimeiraFlag = true;
        } else {
          return i + 1;
        } // fim do if
      } // fim do if
    } // fim do for
    return encontrarUltimoBit(quadro);
  } // fim do metodo encontrarFimDaFlagDeBits

  /* ***************************************************************
  * Metodo: encontrarFimDaFlagDeBytes
  * Funcao: Procura a flag final do enquadramento de bytes para fazer o tratamento dos 0 nao "transmitidos"
  * Parametros: quadro = quadro com enquadramentos de bits
  * Retorno: A quantidade real de bits sendo transmitidos
  *************************************************************** */
  private int encontrarFimDaFlagDeBytes(int[] quadro) {
    boolean achouPrimeiraFlag = false;
    boolean achouUmEscape = false;
    // Itera byte a byte pelo conteiner
    for (int i = 0; i < quadro.length * 4; i++) {
      int indiceInt = i / 4;
      int posByte = 3 - (i % 4);
      int byteLido = (quadro[indiceInt] >> (posByte * 8)) & 0xFF;
      // Se encontrar um escape ele ira ignorar o proximo possivel flag
      if (byteLido == ESCAPE_BYTES && !achouUmEscape) {
        achouUmEscape = true;
        continue;
      } // fim do if
      if (byteLido == FLAG_BYTES && !achouUmEscape) {
        if (!achouPrimeiraFlag) {
          achouPrimeiraFlag = true;
        } else {
          return i + 1;
        } // fim do if
      } else {
        achouUmEscape = false;
      } // fim do if
    } // fim do for
    return encontrarUltimoBit(quadro) / 8;
  } // fim do metodo encontrarFimDaFlagDeBytes

  /* ***************************************************************
  * Metodo: encontrarFimDaFlagManchester
  * Funcao: Procura a SEGUNDA flag MANCHESTER (0x6AA9) no fluxo de bits.
  * Parametros: quadro = quadro com enquadramentos de bits
  * Retorno: A quantidade real de bits sendo transmitidos
  *************************************************************** */
  private int encontrarFimDaFlagManchester(int[] quadro) {
    int flagBuffer = 0; // Buffer de 16 bits
    boolean achouPrimeiraFlag = false;
    // Itera bit a bit pelo array (Manchester dobra o tamanho)
    for (int i = 0; i < quadro.length * 32; i++) {
      int bitLido = (quadro[i / 32] >>> (31 - (i % 32))) & 1;
      // Adiciona o bit ao buffer e mantem so os ultimos 16
      flagBuffer = ((flagBuffer << 1) | bitLido) & 0xFFFF;
      if (flagBuffer == MANCHESTER_FLAG_BITS) {
        if (!achouPrimeiraFlag) {
          achouPrimeiraFlag = true;
          // Manchester Dif. pode comecar com meia-flag,
          // entao resetamos o buffer para pegar a proxima.
          flagBuffer = 0;
        } else {
          // Achou a segunda flag, o quadro termina aqui
          return i + 1;
        } // fim do if
      } // fim do if
    } // fim do for
    // Se nao achou a 2a flag, usa o "chute"
    return encontrarUltimoBit(quadro);
  } // fim do metodo encontrarFimDaFlagManchester

  /* ***************************************************************
  * Metodo: encontrarUltimoBit
  * Funcao: Realiza um "chute" para Contagem de Caracteres. Encontra o indice do ULTIMO bit '1' no buffer.
  * Parametros: quadro = quadro com enquadramentos de bits
  * Retorno: A quantidade real de bits sendo transmitidos
  *************************************************************** */
  private int encontrarUltimoBit(int[] quadro) {
    int ultimoBit = 0;
    for (int i = 0; i < quadro.length * 32; i++) {
      int indiceArray = i / 32;
      int bitPos = 31 - (i % 32);
      int bit = (quadro[indiceArray] >> bitPos) & 1;
      if (bit == 1) {
        ultimoBit = i + 1; // Guarda o indice do ultimo '1'
      } // fim do if
    } // fim do for
    return ultimoBit;
  } // fim do metodo encontrarUltimoBit
}